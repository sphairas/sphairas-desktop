/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.noten.impl;

import java.beans.PropertyChangeListener;
import java.util.Iterator;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoableEditSupport;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.util.Int2;
import org.thespheres.betula.assess.Allocator;
import org.thespheres.betula.assess.AssessmentContext;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.assess.Distribution;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.AssessorMap;

/**
 *
 * @author boris.heithecker
 */
public abstract class AbstractInt2Assessment implements AssessmentContext<StudentId, Int2>, AssessmentConvention.OfBiasable {

    Int2 ceiling;
    AllocatorImpl allocator;
    AssessorListImpl assessorList;
    final UndoableEditSupport undoSupport = new UndoableEditSupport(this);

    protected AbstractInt2Assessment(Int2 rangeMaximum) {
        this.ceiling = rangeMaximum;
    }

    public void addUndoableEditListener(UndoableEditListener l) {
        undoSupport.addUndoableEditListener(l);
    }

    public void removeUndoableEditListener(UndoableEditListener l) {
        undoSupport.removeUndoableEditListener(l);
    }

    @Override
    public Int2 getRangeMaximum() {
        return ceiling;
    }

    @Override
    public Iterator<Grade> iterator() {
        return linkedGradesIterator();
    }

    Iterator<Grade> linkedGradesIterator() {
        return new Iterator<Grade>() {

            private Grade next = AbstractInt2Assessment.this.getFloorUnbiased();

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public Grade next() {
                Grade current = next;
                next = current.getNextHigher();
                return current;
            }

            @Override
            public void remove() {
            }
        };
    }

    @Override
    public Allocator<Int2> getAllocator() {
        return allocator;
    }

    @Override
    public AssessorMap getAssessorList() {
        return assessorList;
    }

    @Override
    public Distribution getCurrentDistribution() {
        return allocator.getCurrentDist();
    }

    @Override
    public void applyDistribution(Distribution d) {
        allocator.distribute(d, ceiling);
    }

    @Override
    public void setRangeMaximum(Int2 value) {
        Distribution current = getCurrentDistribution();
        this.ceiling = value;
        try {
            allocator.distribute(current, value);
        } catch (IllegalArgumentException e) {
        }
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        allocator.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        allocator.removePropertyChangeListener(l);
    }

}
