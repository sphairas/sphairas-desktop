/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.model;

import com.google.common.eventbus.EventBus;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEditSupport;
import org.thespheres.betula.RecordId;
import org.thespheres.betula.Student;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.journal.Journal;
import org.thespheres.betula.journal.JournalRecord;
import org.thespheres.betula.journal.RecordNote;
import org.thespheres.betula.util.CollectionChangeEvent;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.util.StudentComparator;

/**
 *
 * @author boris.heithecker
 * @param <R>
 * @param <J>
 */
public abstract class EditableJournal<R extends JournalRecord, J extends Journal<R>> { //implements PropertyChangeListener {

    public static final String PROP_JOURNAL_START = "journal-start";
    public static final String PROP_JOURNAL_END = "journal-end";
    public static final String COLLECTION_PARTICIPANTS = "journa-participants";
    public static final String COLLECTION_RECORDS = "journal-records";
    public static final String COLLECTION_NOTES = "journal-notes";
    protected final List<EditableRecord<R>> records = new ArrayList<>();
    protected final List<EditableParticipant> participants = new ArrayList<>();
    protected final List<EditableNote> notes = new ArrayList<>();
    protected final J journal;
    private final PropertyChangeSupport pSupport = new PropertyChangeSupport(this);
    private final EventBus eventBus = new EventBus();
    protected final UndoableEditSupport undoSupport = new UndoableEditSupport(this);

    protected EditableJournal(J rset) {
        this.journal = rset;
        initEditableParticipants();
        initEditableRecords();
        initEditableNotes();
    }

    public J getRecordSet() {
        return journal;
    }

    public LocalDate getJournalStart() {
        return journal.getJournalStart();
    }

    public void setJournalStart(LocalDate val) {
        final LocalDate old = getJournalStart();
        journal.setJournalStart(val);
        pSupport.firePropertyChange(PROP_JOURNAL_START, old, val);
        final PropertyChangeEvent evt = new PropertyChangeEvent(this, PROP_JOURNAL_START, old, getJournalStart());
        getEventBus().post(evt);
    }

    public LocalDate getJournalEnd() {
        return journal.getJournalEnd();
    }

    public void setJournalEnd(LocalDate val) {
        final LocalDate old = getJournalEnd();
        journal.setJournalEnd(val);
        pSupport.firePropertyChange(PROP_JOURNAL_END, old, val);
        final PropertyChangeEvent evt = new PropertyChangeEvent(this, PROP_JOURNAL_END, old, getJournalEnd());
        getEventBus().post(evt);
    }

    public List<EditableRecord<R>> getEditableRecords() {
        return Collections.unmodifiableList(records);
    }

    public List<EditableParticipant> getEditableParticipants() {
        return Collections.unmodifiableList(participants);
    }

    public List<EditableNote> getEditableNotes() {
        return Collections.unmodifiableList(notes);
    }

    private void initEditableParticipants() {
        final StudentComparator sc = new StudentComparator();
        final List<Student> ts = journal.getRecords().values().stream()
                .map(JournalRecord::getStudentEntries)
                .map(Map::keySet)
                .flatMap(Collection::stream)
                .sorted(sc)
                .distinct()
                .collect(Collectors.toList());
        synchronized (participants) {
            participants.clear();
            int i = 0;
            for (Student s : ts) {
                final EditableParticipant ep = createEditableParticipant(s);
                participants.add(ep);
                ep.setIndex(i++);
            }
        }
    }

    private void initEditableRecords() {
        final Comparator<RecordId> comp = Comparator.comparing(RecordId::getLocalDateTime);
        final List<RecordId> keys = journal.getRecords().keySet().stream()
                .sorted(comp)
                .distinct()
                .collect(Collectors.toList());
        synchronized (records) {
            records.clear();
            int i = 0;
            for (RecordId date : keys) {
                EditableRecord rec = createEditableRecord(date);
                records.add(rec);
                rec.setIndex(i++);
            }
        }
    }

    private void initEditableNotes() {
        synchronized (notes) {
            notes.clear();
            journal.getNotes().stream().map(rn -> new EditableNote(rn, this, null))
                    .peek(notes::add)
                    //                    .peek(note -> note.addPropertyChangeListener(this))
                    .forEach(notes::add);
        }
    }

    public EditableNote updateNote(final RecordNote rNote, int recordIndex) {
        final EditableRecord rec = getEditableRecords().get(recordIndex);
        final EditableNote ret = rec.updateNote(rNote);
        fireNoteAdded(ret);
        return ret;
    }

    public EditableNote updateNote(RecordNote rNote) {
        EditableNote ret = new EditableNote(rNote, this, null);
        synchronized (notes) {
            notes.add(ret);
        }
//        ret.addPropertyChangeListener(this);
        fireNoteAdded(ret);
        return ret;
    }

    boolean removeNote(EditableNote note) {
        if (notes.remove(note)) {
            noteRemoved(note);
            return true;
        } else {
            return false;
        }
    }

    void noteRemoved(EditableNote note) {
        fireNoteRemoved(note);
    }

    public List<EditableNote> getNotesForParticipant(int index) {
        final StudentId stud = getEditableParticipants().get(index).getStudentId();
        return getEditableRecords().stream()
                .map(EditableRecord::getEditableNotes)
                .flatMap(List::stream)
                .filter(n -> n.getStudent().equals(stud))
                .collect(Collectors.toList());
    }

    public EditableParticipant updateParticipant(final Student student) {
        EditableParticipant ret;
        if ((ret = findParticipant(student.getStudentId())) == null) {
            ret = insertPart(student);
        }
        return ret;
    }

    public EditableParticipant findParticipant(final StudentId student) {
        synchronized (participants) {
            return participants.stream()
                    .filter(ep -> ep.getStudentId().equals(student))
                    .collect(CollectionUtil.requireSingleOrNull());
        }
    }

    public EditableRecord<R> findRecord(final RecordId d) {
        synchronized (records) {
            return records.stream()
                    .filter(er -> er.getRecordId().equals(d))
                    .collect(CollectionUtil.requireSingleOrNull());
        }
    }

    private EditableParticipant insertPart(Student student) {

        final EditableParticipant part = createEditableParticipant(student);
        //        
        synchronized (participants) {
            participants.add(part); // return boolean ignored
            //Collections.sort(editableStudents, comparator); //TODO: sortieren!!!
//            part.addPropertyChangeListener(this);
        }
        indices();
//        calendarDays();
        //
        firePartAdded(part);
        return part;
    }

    public void removeParticipant(final StudentId sid) {
        final EditableParticipant part;
        synchronized (participants) {
            part = participants.stream()
                    .filter(ep -> ep.getStudentId().equals(sid))
                    .collect(CollectionUtil.singleOrNull());
        }
        if (part == null) {
            return;
        }
        final Student student = part.getStudent();
        final Map<RecordId, Grade> old;
        synchronized (records) {
            old = records.stream()
                    .filter(er -> er.getGradeAt(part.getIndex()) != null)
                    .collect(Collectors.toMap(EditableRecord::getRecordId, er -> er.getGradeAt(part.getIndex())));
            records.stream()
                    .forEach(er -> er.setGradeAt(part.getIndex(), null, null));
        }
        final List<EditableNote> oldNotes;
        final List<RecordNote> ln;
        synchronized (notes) {
            oldNotes = notes.stream()
                    .filter(en -> en.getStudent() != null && en.getStudent().equals(sid))
                    .collect(Collectors.toList());
            ln = oldNotes.stream()
                    .peek(this::removeNote)
                    .map(EditableNote::getRecordNote)
                    .collect(Collectors.toList());
        }
        class Edit extends AbstractUndoableEdit {

            @Override
            public void redo() throws CannotRedoException {
                super.redo();
                removeStudent(student.getStudentId());
                synchronized (records) {
                    records.stream()
                            .forEach(er -> er.setGradeAt(part.getIndex(), null, null));
                }
                synchronized (notes) {
                    oldNotes.stream()
                            .peek(EditableJournal.this::removeNote)
                            .map(EditableNote::getRecordNote)
                            .collect(Collectors.toList());
                }
            }

            @Override
            public void undo() throws CannotUndoException {
                super.undo();
                final EditableParticipant insert = insertPart(student);
                old.forEach((rid, g) -> {
                    records.stream()
                            .filter(er -> er.getRecordId().equals(rid))
                            .forEach(er -> er.setGradeAt(insert.getIndex(), g, Timestamp.now()));
                });
                ln.forEach(EditableJournal.this::updateNote);
            }

        }
        removeStudent(part.getStudentId());
    }

    private void removeStudent(final StudentId sid) {
        //check if contained???
        //checkindes out of bounds???
        //
        final EditableParticipant remove = findParticipant(sid);
        if (remove != null) {
            final EditableParticipant rem = participants.remove(remove.getIndex());
//        rem.removePropertyChangeListener(this);
            rem.remove();
            indices();
//        calendarDays();
//
            firePartRemoved(rem);
        }
    }

    public EditableRecord<R> updateRecord(RecordId date, JournalRecord source) {
        EditableRecord<R> er;
        if ((er = findRecord(date)) == null) {
            er = insertRecord(date, createJournalRecord());
        }
        final EditableRecord<R> ret = er;
        source.getStudentEntries().forEach((s, gv) -> {
            final EditableParticipant ep = updateParticipant(s);
            if (gv != null && !Objects.equals(ret.getGradeAt(ep.getIndex()), gv.getGrade())) {
                //No timestamp in XmlJournalRecord --> do never overwrite!
//                    final long ts = record.TIMESTAMPCACHE.containsKey(s) ? record.TIMESTAMPCACHE.get(s) : 0l;
                ret.setGradeAt(ep.getIndex(), gv.getGrade(), gv.getTimestamp());
            }
        });
        ret.updateRecord(source);
        return ret;
    }

    protected abstract R createJournalRecord();

    private EditableRecord<R> insertRecord(RecordId date, R record) {

        journal.getRecords().put(date, record);
        EditableRecord<R> nr = createEditableRecord(date);

        synchronized (records) {
            int index = records.size();
            records.add(nr);
            nr.setIndex(index);
            //
            Collections.sort(records);
        }
//        nr.addPropertyChangeListener(this);
        indices();
//        calendarDays();
        //
        fireRecordAdded(nr);
        return nr;
    }

    public EditableRecord removeRecord(final int index) {
        //TODO check if contained???
        //TODO checkindes out of bounds???
        //
        final Set<StudentId> removeStuds = getEditableRecords().get(index).getRecord().getStudentEntries().keySet().stream()
                .filter(s -> getEditableRecords().stream().noneMatch(er -> er.getRecord().getStudentEntries().containsKey(s)))
                .map(Student::getStudentId)
                .collect(Collectors.toSet());

        EditableRecord rem;
        synchronized (records) {
            rem = records.remove(index);
//            rem.removePropertyChangeListener(this);
            rem.remove();
        }

        //TODO: Evtl. aus Grades nehmen
        //set new index in all elements if necessary and get index of new
        //int ret = -1;
        indices();
//        calendarDays();
        //
//        for (int i = 0; i < editableProblems.size(); i++) {
//            editableProblems.get(i).getMean().remove(index);
//        }
        //
        fireRecordRemoved(rem);

        removeStuds.forEach(sid -> removeStudent(sid));
        return rem;
        //nICHT vergessen die event Referenzen im EditableStudent !!! zu löschen !!! mehtode initScores!!
    }

    private boolean indices() {
        final StudentComparator sc = new StudentComparator();
        boolean ret = false;
        synchronized (participants) {
            Collections.sort(participants, Comparator.comparing(EditableParticipant::getStudent, sc));
            for (int i = 0; i < participants.size(); i++) {
                EditableParticipant s = participants.get(i);
                if (s.getIndex() != i) {
                    s.setIndex(i);
                    ret = true;
                }
            }
        }
        final Comparator<RecordId> comp = Comparator.comparing(RecordId::getLocalDateTime);
        synchronized (records) {
            Collections.sort(records, Comparator.comparing(EditableRecord::getRecordId, comp));
            for (int i = 0; i < records.size(); i++) {
                EditableRecord r = records.get(i);
                if (r.getIndex() != i) {
                    r.setIndex(i);
                    ret = true;
                }
            }
        }
        return ret;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pSupport.removePropertyChangeListener(listener);
    }

    private void firePartAdded(EditableParticipant s) {
        final CollectionChangeEvent pce = new CollectionChangeEvent(this, EditableJournal.COLLECTION_PARTICIPANTS, s, CollectionChangeEvent.Type.ADD);
        eventBus.post(pce);
    }

    private void firePartRemoved(EditableParticipant s) {
        final CollectionChangeEvent pce = new CollectionChangeEvent(this, EditableJournal.COLLECTION_PARTICIPANTS, s, CollectionChangeEvent.Type.REMOVE);
        eventBus.post(pce);
    }

    private void fireRecordAdded(EditableRecord nr) {
        final CollectionChangeEvent pce = new CollectionChangeEvent(this, EditableJournal.COLLECTION_RECORDS, nr, CollectionChangeEvent.Type.ADD);
        eventBus.post(pce);
    }

    private void fireRecordRemoved(EditableRecord r) {
        final CollectionChangeEvent pce = new CollectionChangeEvent(this, EditableJournal.COLLECTION_RECORDS, r, CollectionChangeEvent.Type.REMOVE);
        eventBus.post(pce);
    }

    private void fireNoteAdded(EditableNote n) {
        final CollectionChangeEvent pce = new CollectionChangeEvent(this, EditableJournal.COLLECTION_NOTES, n, CollectionChangeEvent.Type.ADD);
        eventBus.post(pce);
    }

    private void fireNoteRemoved(EditableNote n) {
        final CollectionChangeEvent pce = new CollectionChangeEvent(this, EditableJournal.COLLECTION_NOTES, n, CollectionChangeEvent.Type.REMOVE);
        eventBus.post(pce);
    }

    protected abstract EditableRecord<R> createEditableRecord(RecordId date);

    protected abstract EditableParticipant createEditableParticipant(Student s);

}
