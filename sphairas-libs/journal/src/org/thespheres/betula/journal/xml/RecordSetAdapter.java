/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.xml;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.thespheres.betula.RecordId;
import org.thespheres.betula.journal.xml.RecordSetAdapter.JournalRecordAdapterSet;

/**
 *
 * @author boris.heithecker
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RecordSetAdapter extends XmlAdapter<JournalRecordAdapterSet, Map<RecordId, XmlJournalRecord>> {

    @Override
    public Map<RecordId, XmlJournalRecord> unmarshal(JournalRecordAdapterSet v) throws Exception {
        final Map<RecordId, XmlJournalRecord> ret = new HashMap<>();
        Arrays.stream(v.set)
                .forEach(ra -> {
//                    ra.record.id = ra.id;
                    ret.put(ra.id, ra.record);
                });
        return ret;
    }

    @Override
    public JournalRecordAdapterSet marshal(Map<RecordId, XmlJournalRecord> v) throws Exception {
        JournalRecordAdapter[] arr = v.entrySet().stream()
                .map(e -> new JournalRecordAdapter(e.getKey(), e.getValue()))
                .toArray(JournalRecordAdapter[]::new);
        return new JournalRecordAdapterSet(arr);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class JournalRecordAdapterSet {

        @XmlElement(name = "journal-record")
        private JournalRecordAdapter[] set;

        public JournalRecordAdapterSet() {
        }

        private JournalRecordAdapterSet(JournalRecordAdapter[] s) {
            this.set = s;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class JournalRecordAdapter {

        @XmlElement(name = "record")
        private RecordId id;
        @XmlElement(name = "journal-entry")
        private XmlJournalRecord record;

        public JournalRecordAdapter() {
        }

        private JournalRecordAdapter(RecordId recid, XmlJournalRecord dr) {
            id = recid;
            record = dr;
        }
    }
}
