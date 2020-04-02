/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.netbeans.api.project.Project;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.Unit;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.journal.model.EditableJournal;
import org.thespheres.betula.RecordId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.journal.module.JournalDataObject;
import org.thespheres.betula.journal.xml.XmlJournalRecord;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.journal.JournalConfiguration;
import org.thespheres.betula.journal.actions.InsertRecordVisualPanel.InsertRecordPanel;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.scheme.spi.LessonId;
import org.thespheres.betula.ui.util.ActionLink;
import org.thespheres.ical.CalendarComponent;
import org.thespheres.ical.CalendarComponentProperty;
import org.thespheres.ical.ICalendar;
import org.thespheres.ical.InvalidComponentException;
import org.thespheres.ical.util.IComponentUtilities;

/**
 *
 * @author @author boris.heithecker
 */
@ActionID(category = "Betula", id = "org.thespheres.betula.journal.actions.InsertRecordAction")
@ActionReferences({
    @ActionReference(path = "Menu/betula-project-local", position = 55000),
    @ActionReference(path = "Loaders/text/betula-journal-file+xml/Actions", position = 3500)
})
@ActionRegistration(displayName = "#InsertRecordAction.displayName", lazy = true)
@Messages("InsertRecordAction.displayName=Stunde einfügen")
public final class InsertRecordAction implements ActionListener {

    private final JournalDataObject context;

    public InsertRecordAction(JournalDataObject context) {
        this.context = context;
    }

    @ActionLink(category = "Betula", id = "org.thespheres.betula.journal.actions.InsertRecordAction")
    public static ActionListener after(JournalDataObject context) {
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final Project prj = context.getLookup().lookup(Project.class);
        final EditableJournal ecal = context.getLookup().lookup(EditableJournal.class);
        if (prj != null && ecal != null) {
            final Unit unit = prj.getLookup().lookup(Unit.class);
            final LocalProperties lp = prj.getLookup().lookup(LocalProperties.class);
            final String btid = lp.getProperty("baseTarget.documentId");
            final String auth = lp.getProperty("authority");
            final String btauth = lp.getProperty("baseTarget.authority", auth);
            if (btid != null && btauth != null && unit != null) {
                final DocumentId targetBase = new DocumentId(btauth, btid, DocumentId.Version.LATEST);

                final WizardDescriptor wiz = new WizardDescriptor(new WizardDescriptor.Panel[]{new InsertRecordPanel()});
                wiz.putProperty(InsertRecordPanel.DATE_TIME, LocalDateTime.now());
//                PlatformUtil.getCodeNameBaseLogger(AddUnitTicketAction.class).log(Level.FINE, ex.getLocalizedMessage(), ex);
                if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
                    final LocalDateTime date = (LocalDateTime) wiz.getProperty(InsertRecordPanel.DATE_TIME);
                    if (date != null) {
                        generateEvent(ecal, date, unit, targetBase);
                    }
                }

            }
        }
    }

    private void generateEvent(final EditableJournal ecal, final LocalDateTime date, final Unit unit, final DocumentId targetBase) {
        final String authority = targetBase.getAuthority();
        final RecordId recordId = new RecordId(authority, date);

        final XmlJournalRecord dr = new XmlJournalRecord();

        final Grade udef = JournalConfiguration.getInstance().getJournalUndefinedGrade();
        unit.getStudents().stream()
                .forEach(s -> dr.putStudentEntry(s, udef, new Timestamp(0l)));

        ecal.updateRecord(recordId, dr);
    }

    private void generateEvents(final EditableJournal ecal, final ICalendar iCalendar, final Unit unit, final LessonId lesson) throws InvalidComponentException {

        unit.getStudents().stream()
                .forEach(ecal::updateParticipant);

        final UnitId uid = unit.getUnitId();

        final List<CalendarComponent> ical = iCalendar.getComponents().stream()
                .filter(get -> {
                    final CalendarComponentProperty up = get.getAnyProperty("X-LESSON");
                    return up != null && new LessonId(up.getAnyParameter("x-authority").get(), up.getValue()).equals(lesson);
                })
                .filter(get -> {
                    CalendarComponentProperty up = get.getAnyProperty("X-UNIT");
                    return up != null && new UnitId(up.getAnyParameter("x-authority").get(), up.getValue()).equals(uid);
                })
                //                .filter(get -> {
                //                    if (targetBase != null) {
                //                        CalendarComponentProperty up = get.getAnyProperty("X-TARGET-BASE");
                //                        return new DocumentId(up.getAnyParameter("x-authority").get(), up.getValue(), DocumentId.Version.LATEST).equals(targetBase);
                //                    }
                //                    return true;
                //                })
                .collect(Collectors.toList());

        if (ecal.getJournalStart() == null || ecal.getJournalEnd() == null || ecal.getJournalEnd().isBefore(ecal.getJournalStart())) {
            return;
        }
        final LocalDateTime js = ecal.getJournalStart().atStartOfDay();
        final LocalDateTime je = ecal.getJournalEnd().plusDays(1l).atStartOfDay();

        for (CalendarComponent cc : ical) {
            Date end = Date.from(je.atZone(ZoneId.systemDefault()).toInstant());
            for (CalendarComponent component : cc.inflate(end).getComponents()) {

                LocalDateTime date = IComponentUtilities.parseLocalDateTimeProperty(component, CalendarComponentProperty.DTSTART);
                if (date.isBefore(js) || date.isAfter(je)) {
                    continue;
                }

//                final String authority = targetBase == null ? uid.getAuthority() : targetBase.getAuthority();
                final RecordId recordId = new RecordId(lesson.getAuthority(), date);

                XmlJournalRecord dr = new XmlJournalRecord(); //recordId);

                final CalendarComponentProperty summary = component.getAnyProperty(CalendarComponentProperty.SUMMARY);
                if (summary != null) {
                    summary.getAnyParameter("x-generated-summary")
                            .map(sp -> sp.equals("true"))
                            .ifPresent(b -> {
                                if (!b) {
                                    dr.setListing(summary.getValue(), null);
                                }
                            });

                }
                final Grade udef = JournalConfiguration.getInstance().getJournalUndefinedGrade();
                unit.getStudents().stream()
                        .forEach(s -> dr.putStudentEntry(s, udef, new Timestamp(0l)));

                ecal.updateRecord(recordId, dr);
            }
        }
    }
}
