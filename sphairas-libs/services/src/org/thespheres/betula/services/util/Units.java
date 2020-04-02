/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.util;

import java.awt.EventQueue;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.event.ChangeListener;
import javax.xml.ws.WebServiceException;
import org.openide.util.*;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.*;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Envelope;
import org.thespheres.betula.document.Template;
import org.thespheres.betula.document.model.DocumentsModel;
import org.thespheres.betula.document.util.DateTimeUtil;
import org.thespheres.betula.document.util.DocumentUtilities;
import org.thespheres.betula.document.util.UnitEntry;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.NoProviderException;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.ws.BetulaWebService;
import org.thespheres.betula.services.ws.Paths;
import org.thespheres.betula.services.ws.ServiceException;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.util.ContainerBuilder;

/**
 *
 * @author boris.heithecker
 */
@Messages({"Units.getParticipants.unlistedUnit.error=Unit {0} is not listed in {1}.",
    "Units.status.retry=Enqueuing {0}. retry to load units from provider {1}.",
    "Units.fetchParticipants.status.retry=Enqueuing {0}. retry to load unit participants {2} from provider {1}."})
public class Units {

    public static final int RP_THROUGHPUT = 1;
    private final String providerUrl;
    private final static HashMap<String, Object> INSTANCES = new HashMap<>();
    private final RequestProcessor.Task reload;
    private final Set<UnitId>[] units = new Set[]{new HashSet<>()};
    private final WebServiceProvider service;
    private final ChangeSupport cSupport = new ChangeSupport(this);
    private final RequestProcessor cSupportExecutor = new RequestProcessor();
    private final RequestProcessor uiProcessor = new RequestProcessor("units-unitinfo", RP_THROUGHPUT);
    private final Load load;

    private Units(String p, final WebServiceProvider wsp) {
        this.load = new Load();
        this.providerUrl = p;
        this.service = wsp;
        this.reload = executor().create(load);
    }

    public static Optional<Units> get(final String providerUrl) {
        Object s;
        synchronized (INSTANCES) {
            s = INSTANCES.get(providerUrl);
            if (s == null) {
                try {
                    s = create(providerUrl);
                } catch (IOException ex) {
                    s = ex;
                }
                INSTANCES.put(providerUrl, s);
            }
            Units sig = s instanceof Units ? (Units) s : null;
            return Optional.ofNullable(sig);
        }
    }

    public Set<UnitId> getUnits() {
        if (!reload.isFinished()) {
            if (EventQueue.isDispatchThread()) {
                try {
                    Logger.getLogger(Units.class.getName()).log(Level.WARNING, "Units.getUnits should not be called from AWT while signees are still being loaded.");
                    final long maxWait = ServiceConfiguration.getInstance().getMaxWaitTimeInEDT();
                    reload.waitFinished(maxWait);
                } catch (InterruptedException ex) {
                    final String msg = "Interrupted getUnits() in EDT for " + getProviderUrl();
                    throw new UITimeoutException(msg, ex);
                }
            } else {
                reload.waitFinished();
            }
        }
        return Collections.unmodifiableSet(units[0]);
    }

    public boolean hasUnit(UnitId unit) {
        return getUnits().contains(unit);
    }

    public String getProviderUrl() {
        return providerUrl;
    }

    public WebServiceProvider getWebServiceProvider() {
        return service;
    }

    public void forceReload() {
        this.load.numTrial = 0;
        reload();
    }

    public UnitInfo fetchUnitInfo(final UnitId unit, final LocalDateTime asOf) throws IOException {
        return fetch(unit, asOf, UnitInfo::new, executor(), true); //TODO: better solution, this can be used to fetch outside service.defaultRequestProcesso
    }

    public UnitInfo fetchUnitInfo(final UnitId unit, final LocalDateTime asOf, final RequestProcessor executor) throws IOException {
        return fetch(unit, asOf, UnitInfo::new, executor, true);
    }

    public UnitInfo fetchParticipants(UnitId unit, LocalDateTime asOf) throws IOException {
        return fetchParticipants(unit, asOf, UnitInfo::new);
    }

    public <U extends UnitInfo> U fetchParticipants(UnitId unit, LocalDateTime asOf, Supplier<U> loader) throws IOException {
        return fetch(unit, asOf, loader, executor(), false);
    }

    public <U extends UnitInfo> U fetchParticipants(UnitId unit, LocalDateTime asOf, Supplier<U> loader, final RequestProcessor executor) throws IOException {
        return fetch(unit, asOf, loader, executor, false);
    }

    private <U extends UnitInfo> U fetch(final UnitId unit, final LocalDateTime asOf, final Supplier<U> loader, final RequestProcessor rp, final boolean noList) throws IOException {
        if (!hasUnit(unit)) {
            String msg = NbBundle.getMessage(Units.class, "Units.getParticipants.unlistedUnit.error", unit.toString(), getProviderUrl());
            throw new IllegalArgumentException(msg);
        }
        final DocumentsModel dm = new DocumentsModel();
        dm.initialize(LocalProperties.find(providerUrl).getProperties());
        final U l = loader.get();
        final RequestProcessor.Task[] t = new RequestProcessor.Task[1];
        class Fetch implements Runnable {

            private Exception ioex;
            private int numTrial;

            private Fetch() {
                this.numTrial = 0;
            }

            @Override
            public void run() {
                final ContainerBuilder builder = new ContainerBuilder();
                final String[] path = Paths.UNITS_PARTICIPANTS_PATH;
                final DocumentId udoc = dm.convertToUnitDocumentId(unit);
                final UnitEntry uEntry = builder.updateUnitAction(udoc, unit, null, path, null, null, false, true);
                if (asOf != null) {
                    final String val = DateTimeUtil.DATEFORMAT.format(asOf);
                    uEntry.getHints().put("version.asOf", val);
                }
                if (noList) {
                    uEntry.getHints().put("request-completion.no-children", "true");
                }
                l.beforeSolicit(uEntry, unit);
                try {
                    final BetulaWebService port = service.createServicePort();
                    final Container response = NetworkSettings.suppressAuthenticationDialog(() -> port.solicit(builder.getContainer()));
                    l.extractResponseUnitEntry(response);
                    l.extractStudentIdsFormResponseUnitEntry();
                } catch (IOException | ServiceException ex) {
                    ioex = ex;
                } catch (WebServiceException wsex) {
                    Logger.getLogger(Units.class.getName()).log(Level.INFO, "A WebServiceException occurred loading unit participants from provider " + service.getInfo().getDisplayName(), wsex);
                    WebUtil.resetProvider(service, wsex);
                    final int num = numTrial;
                    if (numTrial++ < ServiceConfiguration.getInstance().getRetryTimes().length) {
                        final int wait = ServiceConfiguration.getInstance().getRetryTimes()[num];
                        String message = NbBundle.getMessage(Units.class, "Units.fetchParticipants.status.retry", numTrial, service.getInfo().getDisplayName(), unit.getId());
                        Logger.getLogger(Units.class.getCanonicalName()).log(Level.INFO, message);
                        final RequestProcessor.Task task = t[0];
                        if (task != null) {
                            task.schedule(wait);
                        } else {
                            ioex = wsex;
                        }
                    } else {
                        ioex = wsex;
                    }
                } catch (Exception ex) {
                    throw (RuntimeException) ex;
                }
            }

            private U get() throws IOException {
                if (ioex != null) {
                    throw new IOException(ioex);
                }
                return l;
            }

        }
        final Fetch fetch = new Fetch();
        if (rp != null) {
            t[0] = rp.post(fetch);
            t[0].waitFinished();
        } else {
            fetch.run();
        }
        return fetch.get();
    }

    private static Units create(final String providerUrl) throws IOException {
        final WebServiceProvider wsp = findWebServiceProvider(providerUrl);
        if (wsp == null) {
            return null;
        }
        final Units ret = new Units(providerUrl, wsp);
        ret.reload();
        return ret;
    }

    public void addChangeListener(ChangeListener listener) {
        cSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        cSupport.removeChangeListener(listener);
    }

    private void reload() {
//        reload = executor().post(new Load(), 0, Thread.NORM_PRIORITY);
        this.reload.setPriority(Thread.NORM_PRIORITY);
        this.reload.schedule(0);
    }

    private static WebServiceProvider findWebServiceProvider(final String providerUrl1) {
        try {
            return WebProvider.find(providerUrl1, WebServiceProvider.class);
        } catch (NoProviderException e) {
            Logger.getLogger(Units.class.getName()).log(Level.WARNING, e.getMessage());
            return null;
        }
    }

    private RequestProcessor executor() {
        final boolean useDefault = NbPreferences.forModule(Units.class).getBoolean("Units.executor.serialize", false);
        return useDefault ? service.getDefaultRequestProcessor() : uiProcessor;
    }

    private static Set<UnitId> loadUnits(BetulaWebService service) throws IOException {
        final ContainerBuilder builder = new ContainerBuilder();
        //signees
        final Action action = Action.REQUEST_COMPLETION;
        final Template t = builder.createTemplate(Paths.UNITS_PATH, null, action);
        t.getHints().put("preferred-security-role", "unitadmin");
        t.getHints().put("request-completion.no-value", "true");
        final Container response;
        try {
            response = NetworkSettings.suppressAuthenticationDialog(() -> service.solicit(builder.getContainer()));
        } catch (ServiceException ex) {
            throw new IOException(ex);
        } catch (Exception ex) {
            throw (RuntimeException) ex;
        }
        //units
        final List<Envelope> l = DocumentUtilities.findEnvelope(response, Paths.UNITS_PATH);
        return l.stream()
                .filter(node -> (node.getAction() != null && node.getAction().equals(Action.RETURN_COMPLETION)))
                .map(UnitUtilities::extractUnitList)
                .flatMap(List::stream)
                .collect(Collectors.toSet());
    }

    class Load implements Runnable {

        private int numTrial;

        private Load() {
            this.numTrial = 0;
        }

        @Override
        public void run() {
            try {
                final Set<UnitId> ls = loadUnits(service.createServicePort());
                synchronized (this) {
                    Units.this.units[0] = ls;
                }
                postFireChange();
            } catch (IOException ex) {
                finalException(ex);
            } catch (WebServiceException wsex) {
                Logger.getLogger(Units.class.getName()).log(Level.INFO, "A WebServiceException occurred loading Units from provider " + service.getInfo().getDisplayName(), wsex);
                WebUtil.resetProvider(service, wsex);
                final int num = numTrial;
                if (numTrial++ < ServiceConfiguration.getInstance().getRetryTimes().length) {
                    final int wait = ServiceConfiguration.getInstance().getRetryTimes()[num];
                    final String message = NbBundle.getMessage(Units.class, "Units.status.retry", numTrial, Units.this.service.getInfo().getDisplayName());
                    Logger.getLogger(Units.class.getCanonicalName()).log(Level.INFO, message);
                    reload.schedule(wait);
                } else {
                    finalException(wsex);
                }
            }
        }

        private void finalException(Exception ex) {
            Logger.getLogger(Units.class.getName()).log(Level.INFO, "An final error occurred loading Units from provider ." + Units.this.service.getInfo().getDisplayName(), ex);
            synchronized (INSTANCES) {
                INSTANCES.put(Units.this.providerUrl, ex);
            }
            postFireChange();
        }

        private void postFireChange() {
            Units.this.cSupportExecutor.post(() -> Units.this.cSupport.fireChange());
        }
    }
}
