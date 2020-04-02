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
import org.thespheres.betula.Student;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.journal.xml.StudentEntriesAdapter.MapAdapter;
import org.thespheres.betula.util.GradeAdapter;
import org.thespheres.betula.util.StudentAdapter;
import org.thespheres.betula.util.GradeEntry;

/**
 *
 * @author boris.heithecker
 */
public class StudentEntriesAdapter extends XmlAdapter<MapAdapter, Map<Student, GradeEntry>> {

    @Override
    public Map<Student, GradeEntry> unmarshal(MapAdapter v) throws Exception {
        return Arrays.stream(v.recs)
                .collect(Collectors.toMap(ra -> ra.student, ra -> ra));
    }

    @Override
    public MapAdapter marshal(final Map<Student, GradeEntry> v) throws Exception {
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
    public static class MapEntryAdapter implements GradeEntry {

        @XmlJavaTypeAdapter(value = StudentAdapter.class)
        @XmlElement
         Student student;
        @XmlJavaTypeAdapter(value = GradeAdapter.class)
        @XmlElement(name = "grade")
         Grade grade;
        @XmlElement(name = "timestamp")
         Timestamp time;

        public MapEntryAdapter() {
        }

        MapEntryAdapter(Student s, Grade ma, Timestamp time) {
            this.student = s;
            this.grade = ma;
            this.time = time;
        }

        MapEntryAdapter(Student s) {
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
