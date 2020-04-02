/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.util;

import java.beans.PropertyChangeEvent;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.swing.event.EventListenerList;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.assess.TargetAssessment;
import org.thespheres.betula.assess.TargetAssessment.Listener;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "target-assessment", namespace = "http://www.thespheres.org/xsd/betula/target-assessment.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlTargetAssessment implements TargetAssessment<Grade, Listener> {

    private final transient EventListenerList listeners = new EventListenerList();
    @XmlAttribute(name = "preferred-convention")
    private String convention;
    private final transient Map<StudentId, AssessValue> map = new TargetMap();
    @XmlElementWrapper(name = "entries")
    @XmlElement(name = "entry", type = AssessEntry.class)
    private final Set<Map.Entry<StudentId, AssessValue>> entries = new HashSet<>();

    @Override
    public void submit(StudentId student, Grade grade, Timestamp timestamp) {
        if (student == null) {
            throw new IllegalArgumentException("Student null");
        }
        AssessValue value = null;
        AssessValue old;
        if (grade != null) {
            value = new AssessValue(grade, timestamp);
            old = map.put(student, value);
        } else {
            old = map.get(student);
            map.remove(student);
        }
        if (!Objects.equals(old, value)) {
            studentChanged(student, old, value);
        }
    }

    @Override
    public Grade select(StudentId student) {
        return map.get(student) != null ? map.get(student).grade : null;
    }

    @Override
    public Timestamp timestamp(StudentId student) {
        return map.get(student) != null ? map.get(student).timestamp : null;
    }

    @Override
    public Set<StudentId> students() {
        return Collections.unmodifiableSet(map.keySet());
    }

    @Override
    public String getPreferredConvention() {
        return convention;
    }

    public void setPreferredConvention(String convention) {
        String ov = this.convention;
        this.convention = convention;
        if (!Objects.equals(convention, ov)) {
            PropertyChangeEvent pce = new PropertyChangeEvent(this, TargetAssessment.PROP_PREFERRED_CONVENTION, ov, this.convention);
            Arrays.stream(listeners.getListeners(Listener.class))
                    .forEach(l -> l.propertyChange(pce));
        }
    }

    @Override
    public void addListener(Listener listener) {
        listeners.add(Listener.class, listener);
    }

    @Override
    public void removeListener(Listener listener) {
        listeners.remove(Listener.class, listener);
    }

    private void studentChanged(StudentId s, AssessValue o, AssessValue n) {
        Arrays.stream(listeners.getListeners(Listener.class))
                .forEach(l -> l.valueForStudentChanged(this, s, o != null ? o.grade : null, n != null ? n.grade : null, n != null ? n.timestamp : null));
    }

    private final class TargetMap extends AbstractMap<StudentId, AssessValue> {

        @Override
        public Set<Entry<StudentId, AssessValue>> entrySet() {
            return entries;
        }

        @Override
        public AssessValue put(StudentId key, AssessValue value) {
            Iterator<Entry<StudentId, AssessValue>> it = entries.iterator();
            AssessValue old = null;
            boolean found = false;
            while (it.hasNext()) {
                Entry<StudentId, AssessValue> e = it.next();
                if (e.getKey().equals(key)) {
                    found = true;
                    old = e.getValue();
                    if (value != null) {
                        e.setValue(value);
                    } else {
                        it.remove();
                    }
                    break;
                }
            }
            if (!found) {
                entries.add(new AssessEntry(key, value));
            }
            studentChanged(key, old, value);
            return old;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class AssessValue {

        @XmlElement(name = "grade")
        @XmlJavaTypeAdapter(GradeAdapter.class)
        private Grade grade;
        @XmlElement(name = "timestamp")
        private Timestamp timestamp;

        public AssessValue() {
        }

        private AssessValue(Grade grade, Timestamp timestamp) {
            this.grade = grade;
            this.timestamp = timestamp;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 97 * hash + Objects.hashCode(this.grade);
            hash = 97 * hash + Objects.hashCode(this.timestamp);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final AssessValue other = (AssessValue) obj;
            if (!Objects.equals(this.grade, other.grade)) {
                return false;
            }
            return Objects.equals(this.timestamp, other.timestamp);
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class AssessEntry implements Map.Entry<StudentId, AssessValue> {

        @XmlElement(name = "student")
        private StudentId student;
        private AssessValue value;

        public AssessEntry() {
        }

        public AssessEntry(StudentId student, AssessValue value) {
            this.student = student;
            this.value = value;
        }

        @Override
        public StudentId getKey() {
            return student;
        }

        @Override
        public AssessValue getValue() {
            return value;
        }

        @Override
        public AssessValue setValue(AssessValue value) {
            AssessValue old = this.value;
            this.value = value;
            return old;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 23 * hash + Objects.hashCode(this.student);
            hash = 23 * hash + Objects.hashCode(this.value);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final AssessEntry other = (AssessEntry) obj;
            if (!Objects.equals(this.student, other.student)) {
                return false;
            }
            return Objects.equals(this.value, other.value);
        }

    }
}
