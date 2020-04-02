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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.netbeans.api.project.Project;
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
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.journal.JournalConfiguration;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.scheme.spi.LessonId;
import org.thespheres.betula.services.scheme.spi.SchemeProvider;
import org.thespheres.betula.services.scheme.spi.TermSchedule;
import org.thespheres.ical.CalendarComponent;
import org.thespheres.ical.CalendarComponentProperty;
import org.thespheres.ical.ICalendar;
import org.thespheres.ical.InvalidComponentException;
import org.thespheres.ical.util.IComponentUtilities;

/**
 *
 * @author @author boris.heithecker
 */
@ActionID(category = "Betula", id = "org.thespheres.betula.journal.actions.UpdateCalendar2")
@ActionReferences({
    @ActionReference(path = "Menu/betula-project-local", position = 52000, separatorBefore = 50000, separatorAfter = 99999),
    @ActionReference(path = "Loaders/text/betula-journal-file+xml/Actions", position = 3100, separatorBefore = 3000)
})
@ActionRegistration(displayName = "#UpdateCalendar2.displayName", lazy = true)
@Messages("UpdateCalendar2.displayName=Berichtsheft(e) aktualisieren")
public final class UpdateCalendar2 implements ActionListener {

    private final JournalDataObject context;

    public UpdateCalendar2(JournalDataObject context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Project prj = context.getLookup().lookup(Project.class);
        if (prj != null) {
            final ICalendar ical = prj.getLookup().lookup(ICalendar.class);
            final Unit unit = prj.getLookup().lookup(Unit.class);
            LessonId targetBase = null;
            final LocalProperties lp = prj.getLookup().lookup(LocalProperties.class);
            final String btid = lp.getProperty("baseTarget.documentId");
            final String auth = lp.getProperty("authority");
            final String btauth = lp.getProperty("baseTarget.authority", auth);
            final String termProvider = lp.getProperty("termSchedule.providerURL");
            if (btid != null && btauth != null) {
                targetBase = new LessonId(btauth, btid);
            }
            EditableJournal ecal = context.getLookup().lookup(EditableJournal.class);
            if (ical != null && unit != null && ecal != null && termProvider != null) {
                final TermSchedule ts = SchemeProvider.find(termProvider).getScheme(TermSchedule.DEFAULT_SCHEME, TermSchedule.class);
                try {
                    generateEvents(ecal, ical, targetBase, unit, ts);
                } catch (InvalidComponentException ex) {
                    Logger.getLogger(UpdateCalendar2.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
                }
            }
        }
    }

    private void generateEvents(final EditableJournal ecal, final ICalendar iCalendar, final LessonId lesson, final Unit unit, final TermSchedule ts) throws InvalidComponentException {

        unit.getStudents().stream()
                .forEach(ecal::updateParticipant);

        final UnitId uid = unit.getUnitId();

        final List<CalendarComponent> ical = iCalendar.getComponents().stream()
                .filter(get -> {
                    final CalendarComponentProperty up = get.getAnyProperty("X-LESSON");
                    return up != null && new LessonId(up.getAnyParameter("x-authority").get(), up.getValue()).equals(lesson);
                })
                .filter(get -> {
                    final CalendarComponentProperty up = get.getAnyProperty("X-UNIT");
                    return up != null && new UnitId(up.getAnyParameter("x-authority").get(), up.getValue()).equals(uid);
                })
                .collect(Collectors.toList());

        if (ecal.getJournalStart() == null || ecal.getJournalEnd() == null || ecal.getJournalEnd().isBefore(ecal.getJournalStart())) {
            return;
        }
        final LocalDateTime js = ecal.getJournalStart().atStartOfDay();
        final LocalDateTime je = ecal.getJournalEnd().plusDays(1l).atStartOfDay();

        final Grade udef = JournalConfiguration.getInstance().getJournalUndefinedGrade();
        for (CalendarComponent cc : ical) {
            final Date end = Date.from(je.atZone(ZoneId.systemDefault()).toInstant());
            for (CalendarComponent component : cc.inflate(end).getComponents()) {

                LocalDateTime date = IComponentUtilities.parseLocalDateTimeProperty(component, CalendarComponentProperty.DTSTART);
                if (date.isBefore(js) || date.isAfter(je)) {
                    continue;
                }

//                final String authority = targetBase == null ? uid.getAuthority() : targetBase.getAuthority();
                final RecordId recordId = new RecordId(lesson.getAuthority(), date);

                XmlJournalRecord dr = new XmlJournalRecord();

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

                final Timestamp time = new Timestamp(ts.termOf(date.toLocalDate()).getBegin());

                unit.getStudents().stream()
                        .forEach(s -> dr.putStudentEntry(s, udef, time));

                ecal.updateRecord(recordId, dr);
            }
        }
    }

}
