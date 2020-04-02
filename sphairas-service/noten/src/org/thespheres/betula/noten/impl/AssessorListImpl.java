/*
 * AssessorListImpl.java
 *
 * Created on 17. November 2007, 18:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.thespheres.betula.noten.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Iterator;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.util.Int2;
import org.thespheres.betula.assess.AssessmentContext;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.AssessorMapEntry;
import org.thespheres.betula.assess.AssessorMap;

/**
 *
 * @author Boris Heithecker
 */
public class AssessorListImpl implements AssessorMap<StudentId, Int2>, PropertyChangeListener {

    private final HashMap<StudentId, AssEntryImpl> entries = new HashMap<>();
    private final AssessmentContext<StudentId, Int2> context;

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    AssessorListImpl(AssessmentContext<StudentId, Int2> ctx) {
        this.context = ctx;
        context.addPropertyChangeListener(this);
    }

    @Override
    public AssessorMapEntry<Int2> createAndAdd(StudentId sid, Int2 value) {
        AssEntryImpl entry = new AssEntryImpl(value);
        entries.put(sid, entry);
        return entry;
    }

    @Override
    public void remove(StudentId entry) {
        AssEntryImpl e = entries.get(entry);
        if (e != null) {
            for (PropertyChangeListener l : e.pSupport.getPropertyChangeListeners()) {
                e.pSupport.removePropertyChangeListener(l);
            }
        }
        entries.remove(entry);
    }

    @Override
    public boolean contains(StudentId entry) {
        return entries.containsKey(entry);
    }

    @Override
    public Iterator<AssessorMapEntry<Int2>> iterator() {
        Iterator i = entries.values().iterator();
        return (Iterator<AssessorMapEntry<Int2>>) i;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        //Check if grade change ..... Siehe Allocator impl setfloor
        //Check if marginModel oder marginValue .....
        redistribute();
    }

    private void redistribute() {
        entries.values().stream()
                .forEach(entry -> entry.defineGrade());
    }

    private class AssEntryImpl implements AssessorMapEntry<Int2> {

        private Int2 value;
        private Grade grade;
        private final PropertyChangeSupport pSupport = new PropertyChangeSupport(AssessorListImpl.this);

        private AssEntryImpl(Int2 value) {
            this.value = value;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pSupport.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pSupport.removePropertyChangeListener(listener);
        }

        @Override
        public void setValue(Int2 value) {
            //Check equal?, kein unnötiges fire ...
            Int2 oldValue = this.value;
            this.value = value;
            pSupport.firePropertyChange(AssessorMapEntry.PROP_VALUE, oldValue, value);
            defineGrade(); //Check equal?
        }

        @Override
        public Int2 getValue() {
            return value;
        }

        @Override
        public void setGrade(Grade grade) {
            //Check equal?, kein unnötiges fire ...
            Grade old = this.grade;
            this.grade = grade;
            pSupport.firePropertyChange(AssessorMapEntry.PROP_GRADE, old, grade);
        }

        @Override
        public Grade getGrade() {
            return grade;
        }

        private void defineGrade() {
            Grade g = context.getAllocator().allocate(this.value);
            if (context instanceof NotenAssessment && g instanceof NotenGrade) {
                g = ((NotenAssessment) context).getMarginModel().defineMargin(this.value, (NotenGrade) g);
            }
            this.setGrade(g);
        }
    }

}
