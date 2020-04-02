/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journalcouchdb.action;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.Action;
import javax.swing.Icon;
import org.apache.commons.lang3.StringUtils;
import org.ektorp.CouchDbConnector;
import org.ektorp.DbAccessException;
import org.netbeans.api.progress.*;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.StatusDisplayer;
import org.openide.loaders.DataObject;
import org.openide.util.Cancellable;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.thespheres.betula.RecordId;
import org.thespheres.betula.Unit;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.couchdb.CouchDBProvider;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.journal.model.EditableParticipant;
import org.thespheres.betula.journal.model.EditableRecord;
import org.thespheres.betula.journal.model.JournalEditor;
import org.thespheres.betula.journal.JournalConfiguration;
import org.thespheres.betula.journal.xml.XmlRecordNote;
import org.thespheres.betula.journalcouchdb.action.FastLessonsImpl3.ReturnRecord;
import org.thespheres.betula.journalcouchdb.config.ConfigSupport;
import org.thespheres.betula.journalcouchdb.model.Note;
import org.thespheres.betula.journalcouchdb.model.TimeDoc2.Journal;
import org.thespheres.betula.services.scheme.spi.LessonId;
import org.thespheres.betula.ui.util.LogLevel;
import org.thespheres.ical.CalendarComponent;
import org.thespheres.ical.CalendarComponentProperty;
import org.thespheres.ical.ICalendar;
import org.thespheres.ical.InvalidComponentException;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"SyncTask2.ProgressHandle.name=Synchronisiere {0}"})
class SyncTask2 implements Runnable, Cancellable {

    private final Unit unit;
    private final JournalEditor editor;
    private final ICalendar iCalendar;
    private final CouchDBProvider couchdb;
    private long taskStart;
    private final ProgressHandle progress;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
//    private final DocumentId targetBase;
    private final LessonId lesson;

    @SuppressWarnings("LeakingThisInConstructor")
    SyncTask2(final Unit unit, final LessonId lesson, final JournalEditor je, final ICalendar ical, final CouchDBProvider conn) {
        this.unit = unit;
        this.lesson = lesson;
        this.editor = je;
        this.iCalendar = ical;
        this.couchdb = conn;
        final String name = NbBundle.getMessage(SyncTask2.class, "SyncTask2.ProgressHandle.name", findName());
        this.progress = ProgressHandleFactory.createHandle(name, this, (Action) null);
    }

    @Override
    public void run() {
        try {
            runImpl();
        } catch (IOException | DbAccessException ex) {
            notifyError(ex);
        }
    }

    private String findName() {
        DataObject dob = editor.getLookup().lookup(DataObject.class);
        if (dob != null) {
            return dob.getNodeDelegate().getDisplayName();
        }
        return null;
    }

    @NbBundle.Messages({"SyncTask2.error.title=Fehler beim Synchronisieren",
        "SyncTask2.error.message=Beim Synchronisieren von {0} (Gruppe: {1}) ist ein Fehler (Typ: {2}) aufgetreten (siehe sphairas-Log)."})
    private void notifyError(Exception ex) {
        progress.finish();
        Logger.getLogger(SyncTask2.class.getCanonicalName()).log(LogLevel.INFO_WARNING, ex.getMessage(), ex);
        Icon ic = ImageUtilities.loadImageIcon("org/thespheres/betula/ui/resources/exclamation-red-frame.png", true);
        String title = NbBundle.getMessage(SyncTask2.class, "SyncTask2.error.title");
        final String name = findName();
        String message = NbBundle.getMessage(SyncTask2.class, "SyncTask2.error.message", unit.getUnitId().getId(), name, ex.getClass().getSimpleName());
        NotificationDisplayer.getDefault()
                .notify(title, ic, message, null, NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.WARNING);
    }

    @NbBundle.Messages({"SyncTask2.success=Berichtsheft {0} erfolgreich in {1}ms synchroniziert."})
    private void logSuccess() {
        long dur = System.currentTimeMillis() - taskStart;
        final String name = findName();
        final String msg = NbBundle.getMessage(SyncTask2.class, "SyncTask2.success", name, Long.toString(dur));
        Logger.getLogger(SyncTask2.class.getCanonicalName()).log(Level.INFO, msg);
        StatusDisplayer.getDefault().setStatusText(msg);
    }

    @NbBundle.Messages({"SyncTask2.cancelled=Synchronisation von Berichtsheft {0} abgebrochen."})
    private void logCancelled() {
        final String name = findName();
        final String msg = NbBundle.getMessage(SyncTask2.class, "SyncTask2.cancelled", name);
        Logger.getLogger(SyncTask2.class.getCanonicalName()).log(Level.INFO, msg);
        StatusDisplayer.getDefault().setStatusText(msg);
    }

    @Override
    public boolean cancel() {
        cancelled.set(true);
        return cancelled.get();
    }

    private void runImpl() throws IOException {
        progress.start();
        progress.switchToIndeterminate();
        taskStart = System.currentTimeMillis();

        final UnitId uid = unit.getUnitId();
        List<CalendarComponent> cc = iCalendar.getComponents().stream()
                .filter(get -> {
                    final CalendarComponentProperty up = get.getAnyProperty("X-LESSON");
                    return up != null && new LessonId(up.getAnyParameter("x-authority").get(), up.getValue()).equals(lesson);
                })
                .filter(get -> {
                    final CalendarComponentProperty up = get.getAnyProperty("X-UNIT");
                    return up != null && new UnitId(up.getAnyParameter("x-authority").get(), up.getValue()).equals(uid);
                })
                //                .filter(get -> {
                //                    final CalendarComponentProperty up = get.getAnyProperty("X-TARGET-BASE");
                //                    return targetBase == null || (up != null && new DocumentId(up.getAnyParameter("x-authority").get(), up.getValue(), DocumentId.Version.LATEST).equals(targetBase));
                //                })
                .collect(Collectors.toList());

        if (cancelled.get()) {
            progress.finish();
            logCancelled();
            return;
        }

        CouchDbConnector connector = couchdb.getUserDatabase();

        if (cancelled.get()) {
            progress.finish();
            logCancelled();
            return;
        }

        ConfigSupport config = new ConfigSupport(connector);
        config.updateProperties(null);

        if (cancelled.get()) {
            progress.finish();
            logCancelled();
            return;
        }

        FastLessonsImpl3 fli;
        try {
            fli = new FastLessonsImpl3(connector, editor.getEditableJournal().getJournalStart(), editor.getEditableJournal().getJournalEnd(), cc, unit);
        } catch (InvalidComponentException ex) {
            throw new IOException(ex);
        }

        Map<RecordId, ReturnRecord> records;
        try {
            records = fli.run(progress, cancelled);
        } catch (InvalidComponentException ex) {
            throw new IOException(ex);
        }

        if (cancelled.get()) {
            progress.finish();
            logCancelled();
            return;
        }

        records.forEach((rid, tr) -> updateRecord(rid, tr));

        progress.finish();
        logSuccess();
    }

    private void updateRecord(final RecordId rid, ReturnRecord recl) {
        if (recl.journal != null) {
            editor.getEditableJournal().getEditableRecords().stream()
                    .filter(er -> rid.equals(er.getRecordId()))
                    .forEach(er -> updateJournal(er, recl.journal));
        }
        boolean changed = recl.list.stream().reduce(false, (res, rec) -> {
            final EditableParticipant ep = editor.getEditableJournal().findParticipant(rec.getStudent());
            final Grade grade = rec.getGrade();
            final String note = StringUtils.trimToNull(rec.getNote());
            final List<Note> notes = rec.getNotes().stream()
                    //                    .map(n -> n.getText())
                    .filter(Objects::nonNull)
                    .filter(n -> !StringUtils.isBlank(n.getText()))
                    .collect(Collectors.toList());
            if (note != null) {
                notes.add(0, new Note(note, null));
            }
            boolean ret = false;
            if (grade != null && ep != null && rec.getTimestamp() != null) {
                final Timestamp ts = new Timestamp(rec.getTimestamp());
                editor.getEditableJournal().getEditableRecords().stream()
                        .filter(er -> rid.equals(er.getRecordId()))
                        .forEach(er -> er.setGradeAt(ep.getIndex(), grade, ts));
                ret = true;
            }
            if (!notes.isEmpty() && ep != null) {
                notes.stream()
                        .forEach(n -> editor.getEditableJournal().getEditableRecords().stream()
                        .filter(er -> rid.equals(er.getRecordId()))
                        .forEach(er -> updateNote(er, ep, n)));
                ret = true;
            }
            return ret;
        }, (res, v) -> v);
        if (changed) {
            editor.getEditableJournal().getEditableRecords().stream()
                    .filter(er -> rid.equals(er.getRecordId()))
                    .forEach(this::updateUnset);
        }
    }

    private void updateNote(final EditableRecord<?> er, final EditableParticipant ep, final Note note) {
        boolean contained = er.getEditableNotes().stream()
                .filter(en -> ep == null || en.getStudent().equals(ep.getStudentId()))
                .filter(en -> en.getText() != null)
                .filter(en -> "default".equals(en.getScope()))
                .anyMatch(en -> en.getText().trim().equals(note.getText()));
        if (!contained) {
            final XmlRecordNote rn = new XmlRecordNote("default", ep != null ? ep.getStudentId() : null);
            Timestamp ts = null;
            if (note.getTimestamp() != null) {
                ts = new Timestamp(new Date(note.getTimestamp()));
            }
            rn.setCause(note.getText(), ts);
            editor.getEditableJournal().updateNote(rn, er.getIndex());
        }
    }

    private void updateJournal(EditableRecord<?> er, Journal j) {
        final Timestamp orig = er.getListing().map(l -> l.getTimestamp()).orElse(null);
        if (orig == null || j.getTimestamp() == null || j.getTimestamp() > orig.getValue().getTime()) {
            er.setListing(j.getText(), new Timestamp(j.getTimestamp()));
        }
    }

    private void updateUnset(EditableRecord<?> er) {
        er.getEditableJournal().getEditableParticipants().stream()
                .forEach(ep -> {
                    Grade g = er.getGradeAt(ep.getIndex());
                    if (g != null && g.equals(JournalConfiguration.getInstance().getJournalUndefinedGrade())) {
                        er.setGradeAt(ep.getIndex(), JournalConfiguration.getInstance().getJournalDefaultGrade(), null);
                    }
                });
    }

}
