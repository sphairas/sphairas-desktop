/*
 *
 * Created on 15. September 2007, 12:27
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.journal.JournalRecord2;
import org.thespheres.betula.journal.JournalRecord2.GradeValue;
import org.thespheres.betula.journal.RecordNote;

/**
 *
 * @author boris.heithecker
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlJournalRecord2 implements JournalRecord2 {

    @Deprecated
    @XmlAttribute
    private String text;
    @XmlElement(name = "journal-text")
    private XmlJournalText journalText;
    @Deprecated
    @XmlElement(name = "entries")
    @XmlJavaTypeAdapter(value = RecordTableAdapter.class)
    private Map<StudentId, Grade> map;
    @XmlElement(name = "student-entries")
    @XmlJavaTypeAdapter(value = StudentEntriesAdapter2.class)
    private Map<StudentId, GradeValue> map2;
    @XmlAttribute
    private Double weight;
    @XmlElementWrapper(name = "notes")
    @XmlElementRef
    private final ArrayList<RecordNote> notes = new ArrayList<>();
    @XmlTransient
    final Map<StudentId, Long> TIMESTAMPCACHE = new LinkedHashMap<>();
    static final Map<Map, Map<StudentId, Long>> TIMESTAMPREPOS;

    static {
        Map<Map, Map<StudentId, Long>> map = new LinkedHashMap<>();
        TIMESTAMPREPOS = Collections.synchronizedMap(map);
    }

    public XmlJournalRecord2() {
    }

    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        if (map != null && map2 == null) {
            Map<StudentId, Long> ts = TIMESTAMPREPOS.get(map);
            if (ts != null) {
                for (StudentId stud : map.keySet()) {
                    Long l = ts.get(stud);
                    if (l != null) {
                        TIMESTAMPCACHE.put(stud, l);
                    }
                }
                TIMESTAMPREPOS.remove(map);
            }
            map.forEach((s, g) -> {
                Long t = TIMESTAMPCACHE.get(s);
                Timestamp time = null;
                if (t != null) {
                    time = new Timestamp(t);
                }
                GradeValue gv = new StudentEntriesAdapter2.MapEntryAdapter(s, g, time);
                getMap2().put(s, gv);
            });
        }
        //TODO: copy to map2
    }

    public boolean beforeMarshal(Marshaller m) {
        if (map != null) {
            TIMESTAMPREPOS.put(map, TIMESTAMPCACHE);
        }
        return true;
    }

    public boolean afterMarshal(Marshaller m) {
        if (map != null) {
            TIMESTAMPREPOS.remove(map);
        }
        return true;
    }

    private Map<StudentId, GradeValue> getMap2() {
        if (map2 == null) {
            map2 = new HashMap<>();
        }
        return map2;
    }

    @Override
    public Map<StudentId, GradeValue> getStudentEntries() {
        return getMap2();
    }

    @Override
    public void submit(StudentId student, Grade g, Timestamp t) {
        if (g != null) {
            GradeValue gv = getMap2().computeIfAbsent(student, s -> new StudentEntriesAdapter2.MapEntryAdapter(s));
            ((StudentEntriesAdapter2.MapEntryAdapter) gv).set(g, t);
        } else {
            getMap2().remove(student);
        }
    }

    @Override
    public Listing getListing() {
        //Legacy
        if (text != null) {
            return () -> text;
        }
        return journalText;
    }

    @Override
    public void setListing(String text, Timestamp time) {
        this.text = null;
        if (text == null && time == null) {
            this.journalText = null;
        } else {
            this.journalText = new XmlJournalText(text, time);

        }
    }

    @Override
    public Double getWeight() {
        return this.weight != null ? this.weight : 1.0;
    }

    @Override
    public void setWeight(Double weight) {
        this.weight = weight == 1.0 ? null : weight;
    }

    @Override
    public List<RecordNote> getNotes() {
        return notes;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class XmlJournalText implements JournalRecord2.Listing {

        @XmlAttribute(name = "timestamp")
        private Timestamp timestamp;
        @XmlValue
        private String text;

        public XmlJournalText() {
        }

        private XmlJournalText(String text, Timestamp timestamp) {
            this.timestamp = timestamp;
            this.text = text;
        }

        @Override
        public Timestamp getTimestamp() {
            return timestamp;
        }

        @Override
        public String getText() {
            return text;
        }

    }
}
