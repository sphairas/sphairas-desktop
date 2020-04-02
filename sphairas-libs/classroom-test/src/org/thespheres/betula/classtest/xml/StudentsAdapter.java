/*
 * StudentsAdapter.java
 *
 * Created on 28. April 2007, 10:05
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.xml;

import java.util.Arrays;
import org.thespheres.betula.util.StudentAdapter;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.Student;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.util.GradeAdapter;

/**
 *
 * @author boris.heithecker
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class StudentsAdapter extends XmlAdapter<StudentsAdapter, Map<Student, StudentScoresImpl>> {

    @XmlElement(name = "student-entry")
    private StudentEntry[] entries;

    @Override
    public Map<Student, StudentScoresImpl> unmarshal(StudentsAdapter v) throws Exception {
        return Arrays.stream(v.entries)
                .filter(e -> e.student != null)
                .collect(Collectors.toMap(e -> e.student, e -> e.getXmlStudentScores()));
    }

    @Override
    public StudentsAdapter marshal(Map<Student, StudentScoresImpl> v) throws Exception {
        StudentsAdapter ret = new StudentsAdapter();
        ret.entries = v.entrySet().stream()
                .map(e -> new StudentEntry(e.getKey(), e.getValue()))
                .toArray(StudentEntry[]::new);
        return ret;
    }

    public static class StudentEntry {

        @XmlElement(name = "student")
        @XmlJavaTypeAdapter(StudentAdapter.class)
        private Student student;
        @XmlElement(name = "grade")
        @XmlJavaTypeAdapter(GradeAdapter.class)
        private Grade grade;
        @XmlElement(name = "auto-distributing")
        private boolean autoDistributing;
        @XmlElementWrapper(name = "scores")
        @XmlElement(name = "problem")
        private Score[] scores;
        @XmlElement(name = "note")
        private String note;

        public StudentEntry() {
        }

        private StudentEntry(Student s, final StudentScoresImpl p) {
            this.student = s;
            this.grade = p.getGrade();
            this.autoDistributing = p.isAutoDistributing();
            this.note = p.getNote();
            this.scores = p.keys().stream()
                    .map(key -> new Score(key, p.get(key)))
                    .toArray(Score[]::new);
        }

        private StudentScoresImpl getXmlStudentScores() {
            final StudentScoresImpl ret = new StudentScoresImpl();
            Arrays.stream(scores).forEach(a -> ret.put(a.problem, a.score));
            ret.setGrade(grade);
            ret.setAutoDistributing(autoDistributing);
            ret.setNote(note);
            return ret;
        }

    }

    public static class Score {

        @XmlAttribute(name = "id")
        private String problem;

        @XmlAttribute(name = "score")
        private Double score;

        public Score() {
        }

        private Score(String prob, Double sc) {
            problem = prob;
            score = sc;
        }

    }
}
