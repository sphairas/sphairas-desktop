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
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.journal.JournalRecord2.GradeValue;
import org.thespheres.betula.journal.xml.StudentEntriesAdapter2.MapAdapter;
import org.thespheres.betula.util.GradeAdapter;

/**
 *
 * @author boris.heithecker
 */
public class StudentEntriesAdapter2 extends XmlAdapter<MapAdapter, Map<StudentId, GradeValue>> {

    @Override
    public Map<StudentId, GradeValue> unmarshal(MapAdapter v) throws Exception {
        return Arrays.stream(v.recs)
                .collect(Collectors.toMap(ra -> ra.student, ra -> ra));
    }

    @Override
    public MapAdapter marshal(final Map<StudentId, GradeValue> v) throws Exception {
        final MapEntryAdapter[] ret = v == null ? new MapEntryAdapter[0] : v.entrySet().stream()
                .map(e -> new MapEntryAdapter(e.getKey(), e.getValue().getGrade(), e.getValue().getTimestamp()))
                .toArray(MapEntryAdapter[]::new);
        return new MapAdapter(ret);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class MapAdapter {

        @XmlElement(name = "entry")
        private MapEntryAdapter[] recs;

        public MapAdapter() {
        }

        private MapAdapter(MapEntryAdapter[] recs) {
            this.recs = recs;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class MapEntryAdapter implements GradeValue {

//        @XmlJavaTypeAdapter(value = StudentAdapter.class)
        @XmlElement(name = "student")
        private StudentId student;
        @XmlJavaTypeAdapter(value = GradeAdapter.class)
        @XmlElement(name = "grade")
        private Grade grade;
        @XmlElement(name = "timestamp")
        private Timestamp time;

        public MapEntryAdapter() {
        }

        MapEntryAdapter(StudentId s, Grade ma, Timestamp time) {
            this.student = s;
            this.grade = ma;
            this.time = time;
        }

        MapEntryAdapter(StudentId s) {
            this.student = s;
        }

        void set(Grade g, Timestamp t) {
            this.grade = g;
            this.time = t;
        }

        @Override
        public Grade getGrade() {
            return grade;
        }

        @Override
        public Timestamp getTimestamp() {
            return time;
        }
    }
}
