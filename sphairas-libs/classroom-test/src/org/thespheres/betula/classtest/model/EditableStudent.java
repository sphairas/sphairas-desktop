/*
 * EditableStudent.java
 *
 * Created on 24. Oktober 2007, 23:53
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.model;

import org.thespheres.betula.util.CollectionElementPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.util.Objects;
import java.util.Set;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.summary.Sum;
import org.openide.nodes.Node;
import org.thespheres.betula.Student;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.classtest.StudentScores;

/**
 *
 * @author boris.heithecker
 * @param <S>
 */
public class EditableStudent<S extends StudentScores> {

    public static final String PROP_SCORES = "scores";
    public static final String PROP_GRADE = "grade";
    public static final String PROP_AUTODISTRIBUTING = "autoDistributing";
    public static final String PROP_NOTE = "note";
    private final StudentScores sc;
    private final ScoresWrapper editableScores = new ScoresWrapper();
    private final EditableClassroomTest<?, S, ?> etest;
    private Node node;
    private final Student student;
    private int index;

    public EditableStudent(Student s, S scores, EditableClassroomTest<?, S, ?> etest) {
        this.student = s;
        this.etest = etest;
        this.sc = scores;
    }

    public Student getStudent() {
        return student;
    }

    public StudentId getStudentId() {
        return student.getStudentId();
    }

    public StudentScores getStudentScores() {
        return editableScores;
    }

    public EditableClassroomTest<?, S, ?> getEditableClassroomTest() {
        return etest;
    }

    public UnitId getUnit() {
        if (student instanceof Student.PrimaryUnit) {
            return ((Student.PrimaryUnit) student).getPrimaryUnit();
        }
        return null;
    }

    public Node getNodeDelegate() {
        if (node == null) {
            node = new StudentNode(this);
        }
        return node;
    }

    public void updateScores(StudentScores scores) {
//        boolean ret = false;
        for (EditableProblem p : etest.getEditableProblems()) {
            Double d = scores.get(p.getId());
            if (d != null) {
                getStudentScores().put(p.getId(), d);
//                ret = true;
            }
        }
        getStudentScores().setAutoDistributing(scores.isAutoDistributing());
        final Grade g = scores.getGrade();
        if (g != null) {
            getStudentScores().setGrade(g);
        }
    }

    public int getIndex() {
        return index;
    }

    void setIndex(int index) {
        this.index = index;
    }

    private final class ScoresWrapper implements StudentScores {

        @Override
        public Double get(String key) {
            final EditableProblem<?> ep = etest.findProblem(key);
            if (ep != null && ep.isBasket()) {
                final EditableBasket<?> basket = (EditableBasket) ep;
                final DescriptiveStatistics values = new DescriptiveStatistics();
                basket.getReferenced().stream()
                        .map(EditableProblem::getId)
                        .map(sc::get)
                        .filter(Objects::nonNull)
                        .forEach(values::addValue);
                final Sum s = new Sum();
                return s.evaluate(values.getValues());
            }
            return sc.get(key);
        }

        @Override
        public void put(final String key, final Double value) {
            final Double old = sc.get(key);
            if (!Objects.equals(old, value)) {
                class SetScoreEdit extends AbstractUndoableEdit {

                    @Override
                    public void redo() throws CannotRedoException {
                        super.redo();
                        sc.put(key, value);
                        final PropertyChangeEvent pce = new CollectionElementPropertyChangeEvent(EditableStudent.this, EditableStudent.PROP_SCORES, key, old, value);
                        etest.getEventBus().post(pce);
                    }

                    @Override
                    public void undo() throws CannotUndoException {
                        super.undo();
                        sc.put(key, old);
                        final PropertyChangeEvent pce = new CollectionElementPropertyChangeEvent(EditableStudent.this, EditableStudent.PROP_SCORES, key, value, old);
                        etest.getEventBus().post(pce);
                    }

                }
                final SetScoreEdit edit = new SetScoreEdit();
                sc.put(key, value);
                final PropertyChangeEvent pce = new CollectionElementPropertyChangeEvent(EditableStudent.this, EditableStudent.PROP_SCORES, key, old, value);
                etest.undoSupport.postEdit(edit);
                etest.getEventBus().post(pce);
            }
        }

        @Override
        public void remove(String key) {
            sc.remove(key);
        }

        @Override
        public Set<String> keys() {
            return sc.keys();
        }

        @Override
        public Grade getGrade() {
            return sc.getGrade();
        }

        @Override
        public void setGrade(Grade grade) {
            Grade old = sc.getGrade();
            if (!Objects.equals(grade, old)) {
                sc.setGrade(grade);
                final PropertyChangeEvent pce = new PropertyChangeEvent(EditableStudent.this, EditableStudent.PROP_GRADE, old, grade);
                etest.getEventBus().post(pce);
            }
        }

        @Override
        public boolean isAutoDistributing() {
            return sc.isAutoDistributing();
        }

        @Override
        public void setAutoDistributing(boolean autoDistributing) {
            boolean old = isAutoDistributing();
            if (old != autoDistributing) {
                sc.setAutoDistributing(autoDistributing);
                final PropertyChangeEvent pce = new PropertyChangeEvent(EditableStudent.this, EditableStudent.PROP_AUTODISTRIBUTING, old, autoDistributing);
                etest.getEventBus().post(pce);
            }
        }

        @Override
        public String getNote() {
            return sc.getNote();
        }

        @Override
        public void setNote(String note) {
            String before = getNote();
            sc.setNote(note);
            if (!Objects.equals(before, note)) {
                final PropertyChangeEvent pce = new PropertyChangeEvent(EditableStudent.this, EditableStudent.PROP_NOTE, before, note);
                etest.getEventBus().post(pce);
            }
        }

        @Override
        public Double sum() {
            return sc.sum();
        }

    }

}
