/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.database.action;

import java.io.IOException;
import java.util.Arrays;
import java.util.MissingResourceException;
import java.util.StringJoiner;
import javax.xml.ws.WebServiceException;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.document.ExceptionMessage;
import org.thespheres.betula.document.Template;
import org.thespheres.betula.document.util.UnitEntry;
import org.thespheres.betula.services.util.Units;
import org.thespheres.betula.services.ws.BetulaWebService;
import org.thespheres.betula.services.ws.Paths;
import org.thespheres.betula.services.ws.ServiceException;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.util.ContainerBuilder;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.utilities.AbstractUpdater;

/**
 *
 * @author boris.heithecker
 * @param <I>
 */
@Messages({"DestroyUnitsUpdater.message.start=Starte Löschen auf {0}.",
    "DestroyUnitsUpdater.message.network=Senden über das Netzwerk ...",
    "DestroyUnitsUpdater.message.finish=Es wurden {0} Kurs(e) in {1} ms auf {2} gelöscht.",
    "DestroyUnitsUpdater.message.remote.error.header====== Server-Fehler =====",
    "DestroyUnitsUpdater.message.remote.error.finisher====== Ende ====="})
public class DestroyUnitsUpdater extends AbstractUpdater<DestroyUnitsImportItem> {

    protected final WebServiceProvider provider;
    protected final ThreadLocal<Long> timeStart = new ThreadLocal();
    protected final ThreadLocal<Integer> numImport = new ThreadLocal();
    protected Exception exception;
//    private final DocumentsModel dm;

    DestroyUnitsUpdater(DestroyUnitsImportItem[] impKurse, WebServiceProvider provider) {
        super(impKurse);
        this.provider = provider;
    }

    public WebServiceProvider getProvider() {
        return provider;
    }

    public Exception getException() {
        return exception;
    }

    @Override
    public void run() {
        final String msg = NbBundle.getMessage(DestroyUnitsUpdater.class, "DestroyUnitsUpdater.message.start", provider.getInfo().getDisplayName());
        ImportUtil.getIO().getOut().println(msg);
        final ContainerBuilder builder = new ContainerBuilder();
        numImport.set(0);
        timeStart.set(System.currentTimeMillis());
        Arrays.stream(items)
                .forEach(item -> {
                    final UnitId unitId = item.getUnitId();
                    final Template root = builder.createTemplate(null, unitId, null, Paths.UNITS_PARTICIPANTS_PATH, null, null);
                    final UnitEntry ret = new UnitEntry(item.getTargetDocumentIdBase(), unitId, Action.ANNUL, true);
                    root.getChildren().add(ret);
                    numImport.set(numImport.get() + 1);
                });
        exception = callService(builder);
        Units.get(provider.getInfo().getURL())
                .ifPresent(Units::forceReload);
    }

    protected Exception callService(ContainerBuilder builder) throws MissingResourceException {
        BetulaWebService service;
        try {
            service = provider.createServicePort();
        } catch (IOException ex) {
            if (getErrorWriter() != null) {
                ex.printStackTrace(getErrorWriter());
            }
            return ex;
        }
        final Container ret;
        try {
            final String msg2 = NbBundle.getMessage(DestroyUnitsUpdater.class, "DestroyUnitsUpdater.message.network");
            ImportUtil.getIO().getOut().println(msg2);
            ret = service.solicit(builder.getContainer());
            long dur = System.currentTimeMillis() - timeStart.get();
            final String msg3 = NbBundle.getMessage(DestroyUnitsUpdater.class, "DestroyUnitsUpdater.message.finish", numImport.get(), dur, provider.getInfo().getDisplayName());
            ImportUtil.getIO().getOut().println(msg3);
        } catch (ServiceException | WebServiceException ex) {
            if (err != null) {
                ex.printStackTrace(ImportUtil.getIO().getErr());
            }
            return ex;
        }
        if (ret != null) {
            ret.getEntries().stream()
                    .filter(t -> t.getException() != null)
                    .forEach(this::processException);
        }
        return null;
    }

    protected void processException(Template<?> t) {
        final ExceptionMessage pre = t.getException();
        final StringJoiner sj = new StringJoiner("/n");
        sj.add(NbBundle.getMessage(DestroyUnitsUpdater.class, "DestroyUnitsUpdater.message.remote.error.header"));
        sj.add(pre.getUserMessage());
        sj.add(pre.getLogMessage());
        sj.add(pre.getStackTraceElement());
        sj.add(NbBundle.getMessage(DestroyUnitsUpdater.class, "DestroyUnitsUpdater.message.remote.error.finisher"));
        ImportUtil.getIO().getErr().println(sj.toString());
    }

}
