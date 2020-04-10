/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.config;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.netbeans.spi.project.LookupProvider;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.*;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.admin.units.RemoteSignees;
import org.thespheres.betula.adminconfig.Configuration;
import org.thespheres.betula.adminconfig.Configurations;
import org.thespheres.betula.services.jms.AbstractDocumentEvent;
import org.thespheres.betula.services.client.jms.JMSListener;
import org.thespheres.betula.services.client.jms.JMSTopicListenerService;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.document.DocumentEntry;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Entry;
import org.thespheres.betula.document.Envelope;
import org.thespheres.betula.document.ExceptionMessage;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.document.util.DocumentUtilities;
import org.thespheres.betula.niedersachsen.zeugnis.NdsReportBuilderFactory;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.jms.JMSTopic;
import org.thespheres.betula.services.util.ServiceConfiguration;
import org.thespheres.betula.services.util.Signees;
import org.thespheres.betula.services.ws.CommonDocuments;
import org.thespheres.betula.services.ws.Paths;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.ui.util.LogLevel;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.util.ContainerBuilder;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"Klassenlehrer.status.retry=Enqueuing {0}. retry to load ticket from {1}.",
    "Klassenlehrer.status.initError.title=Fehler beim Initialisierungen der Klassenlehrer"})
public class Klassenlehrer {

    private final Listener listener = new Listener();
    private final static Map<String, Object> INSTANCES = new HashMap<>();
    private final Signees signees;
    private final WebServiceProvider service;
    private final Map<UnitId, Set<Signee>> map = new HashMap<>();
    private final ChangeSupport cSupport = new ChangeSupport(this);
    private final Set<String> ignoreEvents = new HashSet<>();
    private final RequestProcessor.Task reload;
    private final DocumentId klDocument;

    private Klassenlehrer(final Signees signees) {
        this.signees = signees;
        this.service = WebProvider.find(signees.getProviderUrl(), WebServiceProvider.class);
        this.klDocument = initKlassenlehrerDocument();
        reload = service.getDefaultRequestProcessor().post(this::reload);
    }

    private DocumentId initKlassenlehrerDocument() {
        final Configurations cfgs = Configurations.find(signees.getProviderUrl());
        if (cfgs != null) {
            final Configuration<NdsReportBuilderFactory> cfg;
            try {
                cfg = cfgs.readConfiguration("schulvorlage.xml", NdsReportBuilderFactory.class);
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
            if (cfg != null) {
                final NdsReportBuilderFactory nrbf = cfg.get();
                final DocumentId d = nrbf.forName(CommonDocuments.PRIMARY_UNIT_HEAD_TEACHERS_DOCID);
                if (d != null) {
                    return d;
                }
            }
        }
        throw new IllegalStateException("No Klassenlehrer document found for " + this.signees.getProviderUrl());
    }

    static Klassenlehrer find(final Signees signees) {
        final Object i;
        synchronized (INSTANCES) {
            i = INSTANCES.computeIfAbsent(signees.getProviderUrl(), key -> {
                try {
                    return new Klassenlehrer(signees);
                } catch (IllegalStateException e) {
                    return e;
                }
            });
        }
        if (i instanceof IllegalStateException) {
            return null;
        }
        return (Klassenlehrer) i;
    }

    private static void load(PrimaryUnitOpenSupport puos) {
        final String url;
        try {
            url = puos.findBetulaProjectProperties().getProperty("providerURL");
        } catch (IOException ex) {
            notifyError(ex, ex.getLocalizedMessage());
            return;
        }
        JMSTopicListenerService jms = null;
        try {
            jms = puos.findJMSTopicListenerService(JMSTopic.DOCUMENTS_TOPIC.getJmsResource());
        } catch (IOException ex) {
            PlatformUtil.getCodeNameBaseLogger(Klassenlehrer.class).log(Level.WARNING, "No JMS Provider", ex);
        }
        final Klassenlehrer kl;
        if (url != null) {
            kl = Signees.get(url)
                    .map(Klassenlehrer::find)
                    .orElse(null);
            if (kl == null) {
                PlatformUtil.getCodeNameBaseLogger(Klassenlehrer.class).log(Level.WARNING, "No Klassenlehrer for {0}.", url);
            } else if (jms != null) {
                kl.setJMSProvider(jms);
            }
        } else {
            PlatformUtil.getCodeNameBaseLogger(Klassenlehrer.class).log(Level.WARNING, "No providerURL property.");
        }
    }

    private void setJMSProvider(JMSTopicListenerService jms) {
        jms.registerListener(AbstractDocumentEvent.class, listener);
    }

    UnitId[] getUnits() throws IOException {
        if (EventQueue.isDispatchThread()) {
            final long maxWait = ServiceConfiguration.getInstance().getMaxWaitTimeInEDT();
            try {
                reload.waitFinished(maxWait);
            } catch (InterruptedException ex) {
                throw new IOException(ex);
            }
        } else {
            reload.waitFinished();
        }
        return map.keySet().stream()
                .toArray(UnitId[]::new);
    }

    void post(final Signee signee, final UnitId pu) {
        service.getDefaultRequestProcessor().post(() -> callService(pu, signee));
    }

    private void reload() {
        callService(null, null);
    }

    private void callService(final UnitId pu, final Signee signee) {
        final ContainerBuilder builder = new ContainerBuilder();
        final DocumentEntry de = new DocumentEntry(null, klDocument);
        builder.add(de, Paths.PRIMARY_UNITS_SIGNEES_PATH);
        final boolean reloadAll;
        if (signee != null && pu != null) {
            final Entry<UnitId, ?> ue = new Entry<>(null, pu);
            de.getChildren().add(ue);
            final Entry<Signee, ?> se = new Entry<>(Action.FILE, signee);
            ue.getChildren().add(se);
            reloadAll = false;
        } else if (signee != null) {
            final Entry<Signee, ?> se = new Entry<>(Action.ANNUL, signee);
            de.getChildren().add(se);
            reloadAll = false;
        } else {
            de.setAction(Action.REQUEST_COMPLETION);
            reloadAll = true;
        }
        if (!reloadAll) {
            final String pid = generatePropagationId();
            de.getHints().put("event-propagation-id", pid);
            synchronized (ignoreEvents) {
                ignoreEvents.add(pid);
            }
        }
        Container response = null;
        try {
            response = NetworkSettings.suppressAuthenticationDialog(() -> service.createServicePort().solicit(builder.getContainer()));
        } catch (Exception ex) {
            if (ex instanceof IOException) {
//                lastException = (IOException) ex;
            } else {
//                lastException = new IOException(ex);
            }
//            state = UIExceptions.handleServiceException(Targets.class.getName(), providerUrl, ex);
        }
        if (response == null) {
            return;
        }
        final List<Envelope> env = DocumentUtilities.findEnvelope(response, Paths.PRIMARY_UNITS_SIGNEES_PATH);
        final Map<UnitId, List<Signee>> m = env.stream()
                .peek(e -> {
                    final ExceptionMessage em;
                    if ((em = e.getException()) != null) {
                        notifyError(em);
                    }
                })
                .filter(node -> DocumentUtilities.isEntryIdentity(node, DocumentId.class) && ((DocumentId) ((Entry) node).getIdentity()).equals(klDocument))
                .flatMap(node -> node.getChildren().stream())
                .filter(node -> DocumentUtilities.isEntryIdentity(node, UnitId.class))
                .map(node -> (Entry<UnitId, ?>) node)
                .collect(Collectors.toMap(Entry::getIdentity, e -> DocumentUtilities.extractIdentityListChildren(e, Signee.class, !reloadAll ? Action.CONFIRM : null)));
        synchronized (map) {
            if (reloadAll) {
                map.clear();
            }
            m.forEach((u, l) -> map.computeIfAbsent(u, uid -> new HashSet<>()).addAll(l));
        }
        cSupport.fireChange();
        m.forEach((punit, sl) -> sl.stream()
                .forEach(s -> RemoteSignees.find(signees, s).putClientProperty("primaryUnit", punit)));
    }

    private static String generatePropagationId() {
        String ret = RandomStringUtils.randomAlphanumeric(20);
        return ret.toLowerCase();
    }

    static void notifyError(Exception ex, String message) {
        PlatformUtil.getCodeNameBaseLogger(Klassenlehrer.class).log(LogLevel.INFO_WARNING, ex.getMessage(), ex);
        Icon ic = ImageUtilities.loadImageIcon("org/thespheres/betula/ui/resources/exclamation-red-frame.png", true);
        String title = NbBundle.getMessage(Klassenlehrer.class, "Klassenlehrer.status.initError.title");
        NotificationDisplayer.getDefault().notify(title, ic, message, null, NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.WARNING);
    }

    static void notifyError(ExceptionMessage em) {
        PlatformUtil.getCodeNameBaseLogger(Klassenlehrer.class).log(LogLevel.INFO_WARNING, em.getLogMessage());
        PlatformUtil.getCodeNameBaseLogger(Klassenlehrer.class).log(LogLevel.INFO_WARNING, em.getStackTraceElement());
        Icon ic = ImageUtilities.loadImageIcon("org/thespheres/betula/ui/resources/exclamation-red-frame.png", true);
        String title = NbBundle.getMessage(Klassenlehrer.class, "Klassenlehrer.status.initError.title");
        NotificationDisplayer.getDefault().notify(title, ic, StringUtils.defaultIfBlank(em.getUserMessage(), ""), null, NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.WARNING);
    }

    public void addChangeListener(ChangeListener listener) {
        cSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        cSupport.removeChangeListener(listener);
    }

    private class Listener implements JMSListener<AbstractDocumentEvent> {

        @Override
        public void onMessage(AbstractDocumentEvent event) {
            if (event.getSource().equals(klDocument)) {
                final String pid = event.getPropagationId();
                final boolean update;
                if (pid != null) {
                    synchronized (ignoreEvents) {
                        update = !ignoreEvents.contains(pid);
                        ignoreEvents.remove(pid);
                    }
                } else {
                    update = true;
                }
                if (update) {
                    reload.schedule(0);
                }
            }
        }

        @Override
        public void addNotify() {
        }

        @Override
        public void removeNotify() {
        }

    }

    @ServiceProvider(service = LookupProvider.class, path = "Loaders/application/betula-unit-data/Lookup")
    public static final class ReportsModelRegistration implements LookupProvider {

        @Override
        public Lookup createAdditionalLookup(Lookup base) {
            PrimaryUnitOpenSupport puos = base.lookup(PrimaryUnitOpenSupport.class);
            if (puos != null) {
                puos.getRP().post(() -> Klassenlehrer.load(puos));
            }
            return Lookup.EMPTY;
        }
    }

}
