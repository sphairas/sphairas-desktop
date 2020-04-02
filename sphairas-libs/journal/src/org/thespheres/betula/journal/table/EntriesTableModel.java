/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.table;

import com.google.common.eventbus.Subscribe;
import java.awt.EventQueue;
import java.beans.IndexedPropertyChangeEvent;
import org.thespheres.betula.journal.model.EditableJournal;
import org.thespheres.betula.journal.model.EditableRecord;
import org.thespheres.betula.journal.model.EditableParticipant;
import javax.swing.table.AbstractTableModel;
import org.openide.util.Mutex;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.util.CollectionChangeEvent;

/**
 *
 * @author boris.heithecker
 */
class EntriesTableModel extends AbstractTableModel {

    private EditableJournal<?, ?> journal;

    EditableJournal<?, ?> getEditableCalendar() {
        return journal;
    }

    synchronized void setEditableJounal(EditableJournal<?, ?> ecal) {
        if (this.journal != null) {
            this.journal.getEventBus().unregister(this);
        }
        this.journal = ecal;
        if (this.journal != null) {
            this.journal.getEventBus().register(this);
        }
        fireTableStructureChanged();
    }

    @Override
    public int getRowCount() {
        return journal != null ? journal.getEditableParticipants().size() : 0;
    }

    @Override
    public int getColumnCount() {
        return journal != null ? journal.getEditableRecords().size() + 1 : 0;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return journal.getEditableParticipants().get(rowIndex).getDirectoryName();
        } else if (columnIndex - 1 < journal.getEditableRecords().size()) {
            return grade(columnIndex - 1, rowIndex);
        } else {
            return null;
        }
    }

    private Object grade(int date, int participant) {
        return journal.getEditableRecords().get(date).getGradeAt(participant);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex != 0;
    }

    @Override
    public void setValueAt(Object val, int participant, int date) {
        Grade g = (Grade) val;
        journal.getEditableRecords().get(date - 1).setGradeAt(participant, g, Timestamp.now());
    }

    @Subscribe
    public void onModelChange(CollectionChangeEvent event) {
        final String cn = event.getCollectionName();
        if (EditableJournal.COLLECTION_RECORDS.equals(cn)
                || EditableJournal.COLLECTION_PARTICIPANTS.equals(cn)) {
            Mutex.EVENT.writeAccess(() -> fireTableStructureChanged());
        }
    }

    @Subscribe
    public void onPropertyChange(IndexedPropertyChangeEvent evt) {
        final Object s = evt.getSource();
        if (s instanceof EditableParticipant) {
//            fireTableStructureChanged();
        } else if (s instanceof EditableRecord) {
            final int index = evt.getIndex();
            EventQueue.invokeLater(() -> fireTableRowsUpdated(index, index));
        }
    }

}
