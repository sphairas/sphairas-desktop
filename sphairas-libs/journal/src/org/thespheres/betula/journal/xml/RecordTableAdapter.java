/*
 * RecordTableAdapter.java
 *
 * Created on 16. September 2007, 21:57
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.xml;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.Student;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.journal.xml.RecordTableAdapter.RecordMapAdapter;
import org.thespheres.betula.util.GradeAdapter;
import org.thespheres.betula.util.StudentAdapter;

/**
 *
 * @author boris.heithecker
 */
public class RecordTableAdapter extends XmlAdapter<RecordMapAdapter, Map<Student, Grade>> {

    @Override
    public Map<Student, Grade> unmarshal(RecordMapAdapter v) throws Exception {
        HashMap<Student, Grade> ret = new HashMap<>();
        HashMap<Student, Long> timestamps = new HashMap<>();
        Arrays.stream(v.recs)
                .forEach(r -> {
                    ret.put(r.student, r.grade);
                    timestamps.put(r.student, r.timestamp);
                });
        XmlJournalRecord.TIMESTAMPREPOS.put(ret, timestamps);
        return ret;
    }

    @Override
    public RecordMapAdapter marshal(final Map<Student, Grade> v) throws Exception {
        if (v != null) {
            RecordAdapter[] ret = v.entrySet().stream()
                    .map(e -> {
                        Map<Student, Long> ts = XmlJournalRecord.TIMESTAMPREPOS.get(v);
                        Long time = null;
                        if (ts != null) {  //Darf eigentlich nicht sein !!!!
                            time = ts.get(e.getKey());
                        }
                        return new RecordAdapter(e.getKey(), e.getValue(), time);
                    })
                    .toArray(RecordAdapter[]::new);
            return new RecordMapAdapter(ret);
        } 
        return null;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class RecordMapAdapter {

        @XmlElement(name = "entry")
        private RecordAdapter[] recs;

        public RecordMapAdapter() {
        }

        private RecordMapAdapter(RecordAdapter[] recs) {
            this.recs = recs;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class RecordAdapter {

        @XmlJavaTypeAdapter(value = StudentAdapter.class)
        @XmlElement
        private Student student;
        @XmlJavaTypeAdapter(value = GradeAdapter.class)
        @XmlElement(name = "grade")
        private Grade grade;
        @XmlElement(name = "timestamp")
        private Timestamp time;
        @Deprecated
        @XmlAttribute
        private Long timestamp;

        public RecordAdapter() {
        }

        private RecordAdapter(Student s, Grade ma, Long time) {
            this.student = s;
            this.grade = ma;
            this.timestamp = time;
        }
    }
}
