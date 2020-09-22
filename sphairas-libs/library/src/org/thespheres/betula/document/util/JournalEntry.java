/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document.util;

import java.io.Serializable;
import org.thespheres.betula.RecordId;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.DocumentEntry;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Entry;
import org.thespheres.betula.document.Timestamp;

/**
 *
 * @author boris.heithecker
 * @param <V>
 */
public class JournalEntry<V> extends DocumentEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    public JournalEntry() {
    }

    @SuppressWarnings("OverridableMethodCallInConstructor")
    private JournalEntry(Action action, DocumentId id, GenericXmlDocument doc) {
        super(action, id);
        setValue(doc);
    }

    public JournalEntry(DocumentId id, Action action, boolean fragment) {
        this(action, id, new GenericXmlDocument(fragment));
    }

    public V select(RecordId record) {
        if (record == null) {
            throw new IllegalArgumentException("Record null");
        }
        Entry<RecordId, V> recordEntry = null;
        try {
            recordEntry = findEntry(record);
        } catch (ClassCastException e) {
        }
        if (recordEntry == null) {
            return null;
        }
        return getJournalValue(recordEntry);
    }

    private V getJournalValue(Entry<RecordId, V> recordEntry) {
        try {
            return recordEntry.getValue() != null ? recordEntry.getValue() : null;
        } catch (Exception ex) {
            return null;
        }
    }

    public Timestamp timestamp(RecordId record) {
        if (record == null) {
            throw new IllegalArgumentException("Record null");
        }
        Entry<RecordId, V> recordEntry = null;
        try {
            recordEntry = findEntry(record);
        } catch (ClassCastException e) {
        }
        if (recordEntry == null) {
            return null;
        }
        return recordEntry.getTimestamp();
    }

    public void submit(RecordId record, V journalValue, Timestamp timestamp) {
        if (record == null) {
            throw new IllegalArgumentException("Record null");
        }
        Entry<RecordId, V> recordEntry = findEntry(record);
        if (recordEntry == null) {
            if (journalValue == null) {
                return;
            }
            recordEntry = new Entry(Action.FILE, record);
            getChildren().add(recordEntry);
        }

        if (journalValue == null) {
            getChildren().remove(recordEntry);
        } else {
//            V old = findGrade(recordEntry);
            recordEntry.setValue(journalValue);
            recordEntry.setTimestamp(timestamp);
        }
    }
}
