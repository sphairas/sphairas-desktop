/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminreports;

import java.io.IOException;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.TermId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.document.util.TextAssessmentEntry;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.ws.BetulaWebService;
import org.thespheres.betula.services.ws.ServiceException;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.util.ContainerBuilder;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.utilities.AbstractUpdater;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;

/**
 *
 * @author boris.heithecker
 * @param <I>
 */
@Messages({"TextKursUpdater.message.start=Start Textkurs-Import",
    "TextKursUpdater.message.info=... {1}/{0} ...",
    "TextKursUpdater.message.network=... Netzwerk ...",
    "TextKursUpdater.message.finish=Es wurden {0} Textkurs(e) in {1}ms importiert."})
public abstract class TextKursUpdater<I extends TextKursImportItem> extends AbstractUpdater<I> {

    public static final String[] TERMGRADESPATH = new String[]{"units", "termtexts"};
    private final TermId term;
    private final WebServiceProvider provider;
//    private final Timestamp gradeTime;
    private final ConfigurableImportTarget importTarget;

    public TextKursUpdater(I[] kurse, WebServiceProvider provider, Term current, ConfigurableImportTarget cit) {
        super(kurse);
        this.term = current.getScheduledItemId();
        this.provider = provider;
        this.importTarget = cit;
//        this.gradeTime = new Timestamp(current.getBegin());
    }

    @Override
    public void run() {
        ImportUtil.getIO().select();
        final String start = NbBundle.getMessage(TextKursUpdater.class, "TextKursUpdater.message.start");
        ImportUtil.getIO().getOut().println(start);
        final ContainerBuilder builder = new ContainerBuilder();
        int numImport = 0;
        long timeStart = System.currentTimeMillis();
        for (I kurs : items) {
            final Signee signee = kurs.getSignee();
//            final String defGrade = "leer";
            final DocumentId target = kurs.getTargetDocument();
            final UnitId unit = kurs.getUnit();
            final String info = NbBundle.getMessage(TextKursUpdater.class, "TextKursUpdater.message.info", target.getId(), unit.getId());
            ImportUtil.getIO().getOut().println(info);
            final TextAssessmentEntry tae = builder.createTextAssessmentAction(unit, kurs.getTargetDocument(), TERMGRADESPATH, null, Action.FILE, false);
            configureTextAssessmentEntry(tae, kurs);
//            final Marker[] sections = Optional.ofNullable(kurs.getPreferredSectionConvention())
//                    .map(MarkerConvention::getAllMarkers)
//                    .orElse(null);
//            for (final StudentId stud : kurs.students()) {
//                if (sections == null) {
//                    tae.submit(stud, term, null, defGrade, gradeTime);
//                } else {
//                    for (final Marker s : sections) {
//                        tae.submit(stud, term, s, defGrade, gradeTime);
//                    }
//                }
//            }
            kurs.entries().forEach(te -> tae.submit(te.getStudent(), term, te.getSection(), te.getText(), te.getTimestamp(), Action.FILE));
            //Signee
            if (signee != null) {
                tae.getValue().addSigneeInfo("entitled.signee", signee);
            }
            ++numImport;
        }

        BetulaWebService service;
        try {
            service = provider.createServicePort();
        } catch (IOException ex) {
            ex.printStackTrace(ImportUtil.getIO().getErr());
            return;
        }
        final Container ret;
        try {
            String message = NbBundle.getMessage(TextKursUpdater.class, "TextKursUpdater.message.network");
            ImportUtil.getIO().getOut().println(message);
            ret = service.solicit(builder.getContainer());
            long dur = System.currentTimeMillis() - timeStart;
            message = NbBundle.getMessage(TextKursUpdater.class, "TextKursUpdater.message.finish", numImport, dur);
            ImportUtil.getIO().getOut().println(message);
        } catch (ServiceException ex) {
            ex.printStackTrace(ImportUtil.getIO().getErr());
            return;
        }
        AbstractUpdater.handleExceptions(ret);
    }

    protected abstract void configureTextAssessmentEntry(TextAssessmentEntry tae, I k);
}
