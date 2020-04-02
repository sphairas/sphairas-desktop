/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.model;

import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.time.ZoneOffset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.RecordId;
import org.thespheres.betula.Student;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.journal.JournalRecord;
import org.thespheres.betula.journal.JournalRecord.Listing;
import org.thespheres.betula.journal.RecordNote;
import org.thespheres.betula.journal.analytics.RecordMean;
import org.thespheres.betula.journal.JournalConfiguration;
import org.thespheres.betula.journal.table.FormatUtil;
import org.thespheres.betula.util.Grades;
import org.thespheres.betula.util.GradeEntry;

/**
 *
 * @author boris.heithecker
 * @param <R>
 */
public abstract class EditableRecord<R extends JournalRecord> implements JournalAnnotatable, Comparable<EditableRecord> {

    public static final String PROP_GRADE = "record-grade";
    public static final String PROP_TEXT = "record-text";
    public static final String PROP_WEIGHT = "record-weight";
    private final RecordId date;
    private R record;
    protected final EditableJournal<R, ?> journal;
    private int index;
//    protected final PropertyChangeSupport pSupport = new PropertyChangeSupport(this);
    private RecordMean mitarbeitMean;
    private List<EditableNote> editableNotes;
    private String annotation;

    protected EditableRecord(RecordId date, R record, EditableJournal<R, ?> ecal) {
        this.date = date;
        this.record = record;
        this.journal = ecal;
    }

    public RecordId getRecordId() {
        return date;
    }

    public abstract Node getNodeDelegate();

    @Override
    public EditableRecord getAnnotatableRecord() {
        return this;
    }

    private void initEditableNotes() {
        this.editableNotes = new ArrayList<>();
        for (RecordNote rn : record.getNotes()) {
            EditableNote note = new EditableNote(rn, journal, this);
            editableNotes.add(note);
        }
    }

    public Date getDate() {
        return Date.from(date.getLocalDateTime().toInstant(ZoneOffset.UTC));
    }

    public EditableJournal<R, ?> getEditableJournal() {
        return journal;
    }

    public List<EditableNote> getEditableNotes() {
        return Collections.unmodifiableList(getEditableNotesImpl());
    }

    private List<EditableNote> getEditableNotesImpl() {
        if (editableNotes == null) {
            initEditableNotes();
        }
        return editableNotes;
    }

    EditableNote updateNote(RecordNote rNote) {
        record.getNotes().add(rNote);
        final EditableNote ret = new EditableNote(rNote, journal, this);
        getEditableNotesImpl().add(ret);
        return ret;
    }

    void updateRecord(JournalRecord record) {
        if (!getListing().isPresent() && record.getListing() != null) {
            setListing(record.getListing().getText(), record.getListing().getTimestamp());
        }
    }

    boolean removeNote(EditableNote note) {
        if (getEditableNotesImpl().remove(note)) {
            getEditableJournal().noteRemoved(note);
            return true;
        } else {
            return false;
        }
    }

    public Grade getGradeAt(int participant) {
        Student s = journal.getEditableParticipants().get(participant).getStudent();
        GradeEntry gv = record.getStudentEntries().get(s);
        return gv != null ? gv.getGrade() : null;
    }

    public Timestamp getTimestampAt(int participant) {
        Student s = journal.getEditableParticipants().get(participant).getStudent();
        GradeEntry gv = record.getStudentEntries().get(s);
        return gv != null ? gv.getTimestamp() : null;
    }

    @Messages({"EditableRecord.setGradeAt.message.overrideEqualTimestamp=Überschreibe Wert {0} mit {1} bei gleichem Zeitstempel."})
    public void setGradeAt(final int participant, final Grade grade, final Timestamp timestamp) {
        final Student s = journal.getEditableParticipants().get(participant).getStudent();
        final GradeEntry gv = record.getStudentEntries().get(s);
        final Grade old = gv != null ? gv.getGrade() : null;
        final Timestamp tsold = gv != null ? gv.getTimestamp() : null;
        final Date compareTo = timestamp == null ? new Date() : timestamp.getDate();
        boolean set = tsold == null || tsold.getDate().before(compareTo);
        if (tsold != null && tsold.getDate().equals(compareTo) && !Objects.equals(grade, old)) {
            final String oldl = old != null ? old.getLongLabel() : "null";
            final String msg = NbBundle.getMessage(EditableRecord.class, "EditableRecord.setGradeAt.message.overrideEqualTimestamp", new Object[]{oldl, grade.getLongLabel()});
            Logger.getLogger(EditableRecord.class.getName()).log(Level.INFO, msg);
            set = true;
        }
        if (set) {
            record.putStudentEntry(s, grade, timestamp);
            final IndexedPropertyChangeEvent pce = new IndexedPropertyChangeEvent(this, PROP_GRADE, old, grade, participant);
            journal.getEventBus().post(pce);
        }
    }

    public Optional<Listing> getListing() {
        return Optional.ofNullable(record.getListing());
    }

    public String getListingText() {
        return getListing().map(Listing::getText).orElse(null);
    }

    public void setListing(String text) {
        setListing(text, Timestamp.now());
    }

    public void setListing(final String text, final Timestamp time) {
        final Timestamp before = getListing().map(Listing::getTimestamp).orElse(null);
        if (before != null && (time == null || before.getDate().after(time.getDate()))) {
            return;
        }
        final String textBefore = getListingText();
        record.setListing(text, time);
//        pSupport.firePropertyChange(PROP_TEXT, before, text);
        final PropertyChangeEvent evt = new PropertyChangeEvent(this, PROP_TEXT, textBefore, getListingText());
        journal.getEventBus().post(evt);
    }

//    public int getDayIndex() {
//        return dayIndex;
//    }
//
//    void setDayIndex(int day) {
//        this.dayIndex = day;
//    }
    public Double getWeight() {
        return record.getWeight();
    }

    public void setWeight(Double weight) {
        Double before = getWeight();
        record.setWeight(weight);
//        pSupport.firePropertyChange(PROP_WEIGHT, before, weight);
        final PropertyChangeEvent evt = new PropertyChangeEvent(this, PROP_TEXT, before, getWeight());
        journal.getEventBus().post(evt);
    }

    public boolean isTemplate() {
        for (EditableParticipant ep : journal.getEditableParticipants()) {
//            final Timestamp ts = getTimestampAt(ep.getIndex());
//            if (ts == null || ts.getDate().getTime() == 0l) {
//                continue;
//            }
            final Grade g = getGradeAt(ep.getIndex());
            if (g == null || g.equals(JournalConfiguration.getInstance().getJournalUndefinedGrade())) {
                continue;
            }
            return false;
        }
        return true;
    }

    public boolean isExpiring() {
        return getDate().getTime() < System.currentTimeMillis();
    }

    public int getIndex() {
        return index;
    }

    void setIndex(int index) {
        this.index = index;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String text) {
        annotation = text;
    }

    public JournalRecord getRecord() {
        return record;
    }

    protected void setRecord(R record) {
        this.record = record;
    }

    public RecordMean getGradesMean() {
        if (mitarbeitMean == null) {
            mitarbeitMean = new RecordMean(this);
        }
        return mitarbeitMean;
    }

    public Grades getGrades() {
        final Grades grs = new Grades();
        journal.getEditableParticipants().stream()
                .map(ep -> getGradeAt(ep.getIndex()))
                .filter(Objects::nonNull)
                .forEach(g -> grs.inc(g));
        return grs;
    }

    void remove() {
        journal.getRecordSet().getRecords().remove(date);
        this.record = null;
    }

    @Override
    public int compareTo(EditableRecord o) {
        return getDate().compareTo(o.getDate());
    }

    public boolean canJoinWithPreceding() {
        if (getIndex() > 0) {
            EditableRecord<R> before = getEditableJournal().getEditableRecords().get(getIndex() - 1);
            return FormatUtil.isSameDay(before.getRecordId(), getRecordId());
        }
        return false;
    }

    public boolean canJoinWithNext() {
        if (getIndex() < getEditableJournal().getEditableRecords().size() - 1) {
            EditableRecord next = getEditableJournal().getEditableRecords().get(getIndex() + 1);
            return FormatUtil.isSameDay(next.getRecordId(), getRecordId());
        }
        return false;
    }

}
