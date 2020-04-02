/*
 * EditableProblem.java
 *
 * Created on 27. Oktober 2007, 19:54
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.model;

import java.beans.PropertyChangeEvent;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.thespheres.betula.classtest.Assessable;
import org.thespheres.betula.classtest.Assessable.Problem;
import org.thespheres.betula.classtest.Basket;
import org.thespheres.betula.classtest.Hierarchical;
import org.thespheres.betula.classtest.HierarchyExcpeption;

/**
 *
 * @author boris.heithecker
 * @param <P>
 */
public abstract class EditableProblem<P extends Assessable.Problem & Basket.Ref> {

    public static final String PROP_MAXSCORE = "maxScore";
    public static final String PROP_WEIGHT = "weight";
    public static final String PROP_DISPLAY_NAME = "displayName";
    public static final String PROP_INDEX = "index";
    protected final EditableClassroomTest<?, ?, ?> etest;
    private ProblemScoresSumMean mean;
    protected final P problem;

    protected EditableProblem(EditableClassroomTest<?, ?, ?> etest, P problem) {
        this.etest = etest;
        this.problem = problem;
    }

    public final String getId() {
        return getProblem().getId();
    }

    public abstract int getIndex();

    public abstract Problem getProblem();

    public abstract boolean isBasket();

    public EditableClassroomTest<?, ?, ?> getEditableClassroomTest() {
        return etest;
    }

    public String getDisplayName() {
        String ret = getProblem().getDisplayName();
        return ret != null ? ret : getProblem().getId();
    }

    public void setDisplayName(String dName) {
        String old = getDisplayName();
        getProblem().setDisplayName(dName);
        final PropertyChangeEvent evt = new PropertyChangeEvent(this, EditableProblem.PROP_DISPLAY_NAME, old, dName);
        etest.getEventBus().post(evt);
    }

    public EditableProblem getParent() {
        Hierarchical h = (Hierarchical) getProblem();
        if (h != null) {
            return etest.findProblem(h.getParentId());
            //TODO: error if not found??
        }
        return null;
    }

    public void setParent(EditableBasket p) throws HierarchyExcpeption {
        Hierarchical h = (Hierarchical) getProblem();
        final String old = h.getParentId();
        h.moveTo(p.getId());
        final PropertyChangeEvent evt = new PropertyChangeEvent(this, EditableBasket.PROP_PARENT, old, p.getId());
        etest.getEventBus().post(evt);
    }

    public Double getWeight() {
        return 1d;
    }

    public void setWeight(Double weight) {
    }

    public int getMaxScore() {
        return getProblem().getMaxScore();
    }

    public void setMaxScore(final int maxScore) {
        final int old = getProblem().getMaxScore();
        if (old != maxScore) {
            class SetMaxScoreEdit extends AbstractUndoableEdit {

                @Override
                public void redo() throws CannotRedoException {
                    super.redo();
                    getProblem().setMaxScore(maxScore);
                    final PropertyChangeEvent evt = new PropertyChangeEvent(this, PROP_MAXSCORE, old, maxScore);
                    etest.getEventBus().post(evt);
                }

                @Override
                public void undo() throws CannotUndoException {
                    super.undo();
                    getProblem().setMaxScore(old);
                    final PropertyChangeEvent evt = new PropertyChangeEvent(this, PROP_MAXSCORE, maxScore, old);
                    etest.getEventBus().post(evt);
                }

            }
            final SetMaxScoreEdit edit = new SetMaxScoreEdit();
            getProblem().setMaxScore(maxScore);
            final PropertyChangeEvent evt = new PropertyChangeEvent(this, PROP_MAXSCORE, old, maxScore);
            etest.undoSupport.postEdit(edit);
            etest.getEventBus().post(evt);
        }
    }

    public double getMean() {
        if (mean == null) {
            mean = new ProblemScoresSumMean(this);
        }
        return mean.getMean();
    }

    public void remove() {
        etest.removeProblem((EditableBasket) this);
    }

    public int compareIndexTo(EditableProblem o) {
        return getIndex() - o.getIndex();
    }

}
