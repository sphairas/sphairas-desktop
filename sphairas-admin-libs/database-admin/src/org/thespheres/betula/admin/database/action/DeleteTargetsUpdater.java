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
import org.openide.windows.OutputWriter;
import org.thespheres.betula.TermId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.document.ExceptionMessage;
import org.thespheres.betula.document.Template;
import org.thespheres.betula.document.util.TargetAssessmentEntry;
import org.thespheres.betula.services.ws.BetulaWebService;
import org.thespheres.betula.services.ws.Paths;
import org.thespheres.betula.services.ws.ServiceException;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.util.ContainerBuilder;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.utilities.AbstractUpdater;
import org.thespheres.betula.xmlimport.utilities.TargetDocumentProperties;

/**
 *
 * @author boris.heithecker
 * @param <I>
 */
@Messages({"DeleteTargetsUpdater.message.start=Starte Löschen auf {0}.",
    "DeleteTargetsUpdater.message.network=Senden über das Netzwerk ...",
    "DeleteTargetsUpdater.message.finish=Es wurden {0} Kurs(e) in {1} ms auf {2} gelöscht.",
    "DeleteTargetsUpdater.message.remote.error.header====== Server-Fehler =====",
    "DeleteTargetsUpdater.message.remote.error.finisher====== Ende ====="})
public class DeleteTargetsUpdater extends AbstractUpdater<DeleteTargetsImportTargetsItem> {

    protected final WebServiceProvider provider;
    protected final ThreadLocal<Long> timeStart = new ThreadLocal();
    protected final ThreadLocal<Integer> numImport = new ThreadLocal();
    protected OutputWriter err = ImportUtil.getIO().getErr();
    protected Exception exception;

    DeleteTargetsUpdater(DeleteTargetsImportTargetsItem[] impKurse, WebServiceProvider provider) {
        super(impKurse);
        this.provider = provider;
    }

    public WebServiceProvider getProvider() {
        return provider;
    }

    @Override
    public OutputWriter getErrorWriter() {
        return err;
    }

    public void setErrorWriter(OutputWriter err) {
        this.err = err;
    }

    public Exception getException() {
        return exception;
    }

    @Override
    public void run() {
        final String msg = NbBundle.getMessage(DeleteTargetsUpdater.class, "DeleteTargetsUpdater.message.start", provider.getInfo().getDisplayName());
        ImportUtil.getIO().getOut().println(msg);
        final ContainerBuilder builder = new ContainerBuilder();
        numImport.set(0);
        timeStart.set(System.currentTimeMillis());
        Arrays.stream(items)
                .forEach(importTargetsItem -> {
                    UnitId unitId = importTargetsItem.getUnitId();
                    for (final TargetDocumentProperties td : importTargetsItem.getImportTargets()) {
                        final TargetAssessmentEntry<TermId> tae = builder.createTargetAssessmentAction(unitId, td.getDocument(), Paths.UNITS_TARGETS_PATH, null, Action.ANNUL, td.isFragment());
                        tae.getHints().putAll(td.getProcessorHints());
                    }
                    numImport.set(numImport.get() + 1);
                });
        exception = callService(builder);
    }

    protected Exception callService(ContainerBuilder builder) throws MissingResourceException {
        BetulaWebService service;
        try {
            service = provider.createServicePort();
        } catch (IOException ex) {
            if (err != null) {
                ex.printStackTrace(ImportUtil.getIO().getErr());
            }
            return ex;
        }
        final Container ret;
        try {
            final String msg2 = NbBundle.getMessage(DeleteTargetsUpdater.class, "DeleteTargetsUpdater.message.network");
            ImportUtil.getIO().getOut().println(msg2);
            ret = service.solicit(builder.getContainer());
            long dur = System.currentTimeMillis() - timeStart.get();
            final String msg3 = NbBundle.getMessage(DeleteTargetsUpdater.class, "DeleteTargetsUpdater.message.finish", numImport.get(), dur, provider.getInfo().getDisplayName());
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
        sj.add(NbBundle.getMessage(DeleteTargetsUpdater.class, "DeleteTargetsUpdater.message.remote.error.header"));
        sj.add(pre.getUserMessage());
        sj.add(pre.getLogMessage());
        sj.add(pre.getStackTraceElement());
        sj.add(NbBundle.getMessage(DeleteTargetsUpdater.class, "DeleteTargetsUpdater.message.remote.error.finisher"));
        ImportUtil.getIO().getErr().println(sj.toString());
    }

}
