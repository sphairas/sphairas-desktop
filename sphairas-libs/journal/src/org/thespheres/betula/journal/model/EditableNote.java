/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.model;

import java.beans.PropertyChangeEvent;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.journal.RecordNote;

/**
 *
 * @author boris.heithecker
 */
public class EditableNote {

    public static final String PROP_TEXT = "note-text";
    public static final String PROP_GRADE = "note-grade";
    private final RecordNote recordNote;
    private final EditableRecord record;
//    private final PropertyChangeSupport propertyChangeSupport;
    private boolean valid;
    private final EditableJournal journal;

    EditableNote(RecordNote rn, EditableJournal ej, EditableRecord record) {
        this.recordNote = rn;
        this.journal = ej;
        this.record = record;
//        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }

    public EditableJournal getEditableCalendar() {
        return journal;
    }

    public EditableRecord getRecord() {
        return record;
    }

    public RecordNote getRecordNote() {
        return recordNote;
    }

    public String getScope() {
        return recordNote.getScope();
    }

    public String getText() {
        return recordNote.getCause();
    }

    public void setText(String cause, Timestamp time) {
        final String before = getText();
        recordNote.setCause(cause, time);
        final PropertyChangeEvent evt = new PropertyChangeEvent(this, PROP_TEXT, before, getText());
        journal.getEventBus().post(evt);
    }

    public Grade getGrade() {
        return recordNote.getGrade();
    }

    public void setGrade(Grade g) {
        final Grade before = getGrade();
        recordNote.setGrade(g);
        final PropertyChangeEvent evt = new PropertyChangeEvent(this, PROP_GRADE, before, getGrade());
        journal.getEventBus().post(evt);
    }

    public StudentId getStudent() {
        return recordNote.getStudent();
    }

    public boolean remove() {
        if (record != null && record.removeNote(this)) {
            this.valid = false;
            return true;
        } else {
            return false;
        }
    }

//    public void addPropertyChangeListener(PropertyChangeListener listener) {
//        propertyChangeSupport.addPropertyChangeListener(listener);
//    }
//
//    public void removePropertyChangeListener(PropertyChangeListener listener) {
//        propertyChangeSupport.removePropertyChangeListener(listener);
//    }
}
