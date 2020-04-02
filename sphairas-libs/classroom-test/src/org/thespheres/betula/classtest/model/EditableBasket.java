/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.model;

import com.google.common.eventbus.Subscribe;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.openide.util.Exceptions;
import org.thespheres.betula.classtest.Assessable;
import org.thespheres.betula.classtest.Assessable.Problem;
import org.thespheres.betula.classtest.Basket;
import org.thespheres.betula.util.CollectionChangeEvent;

/**
 *
 * @author boris.heithecker
 * @param <P>
 */
public class EditableBasket<P extends Assessable.Problem & Basket.Ref<?> & Basket<?, ?>> extends EditableProblem<P> {

    public static final String PROP_REFERENCES = "references";
    public static final String PROP_PARENT = "parent";
    private List<EditableProblem<?>> refs;
    private final Comparator<EditableProblem> cmp = Comparator.comparingInt(EditableProblem::getIndex);
    private ProblemRefSum refSum;

    @SuppressWarnings("LeakingThisInConstructor")
    protected EditableBasket(EditableClassroomTest etest, P problem) {
        super(etest, problem);
        etest.getEventBus().register(this);
    }

    @Override
    public P getProblem() {
        return problem;
    }

    @Override
    public int getIndex() {
        return problem.getIndex();
    }

    void setIndex(int index) {
        try {
            final int old = getIndex();
            problem.setIndex(index);
            final PropertyChangeEvent evt = new PropertyChangeEvent(this, EditableProblem.PROP_INDEX, old, index);
            etest.getEventBus().post(evt);
        } catch (UnsupportedOperationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public Double getWeight() {
        assert getProblem() instanceof Basket.Ref;
        return ((Basket.Ref<Double>) getProblem()).getWeight();
    }

    @Override
    public void setWeight(Double weight) {
        Basket.Ref<Double> xp = (Basket.Ref<Double>) getProblem();
        Double old = xp.getWeight();
        if (!Objects.equals(weight, old)) {
            class SetWeightEdit extends AbstractUndoableEdit {

                @Override
                public void redo() throws CannotRedoException {
                    super.redo();
                    xp.setWeight(weight);
                    final PropertyChangeEvent evt = new PropertyChangeEvent(this, PROP_WEIGHT, old, weight);
                    etest.getEventBus().post(evt);
                }

                @Override
                public void undo() throws CannotUndoException {
                    super.undo();
                    xp.setWeight(old);
                    final PropertyChangeEvent evt = new PropertyChangeEvent(this, PROP_WEIGHT, weight, old);
                    etest.getEventBus().post(evt);
                }

            }
            final SetWeightEdit edit = new SetWeightEdit();
            xp.setWeight(weight);
            final PropertyChangeEvent evt = new PropertyChangeEvent(this, PROP_WEIGHT, old, weight);
            etest.undoSupport.postEdit(edit);
            etest.getEventBus().post(evt);
        }
    }

    void initListeners() {
        if (isBasket()) {
            for (EditableStudent es : etest.getEditableStudents()) {
//                updateScore(es);
//                es.getScores().addPropertyChangeListener(new StudentListener(es));
            }
        }
    }

    public void addReference(EditableProblem reference) {
        Basket<Problem, Basket.Ref<?>> xp = asBasket();
        xp.addReference(reference.getProblem());
        refs.add(reference);
        refs.sort(cmp);
//        updateMaxScore();
        final PropertyChangeEvent evt = new PropertyChangeEvent(this, PROP_REFERENCES, null, null);
        etest.getEventBus().post(evt);
    }

    public void removeReference(EditableProblem reference) {
        Basket<Problem, Basket.Ref<?>> xp = asBasket();
        final boolean ret = xp.removeReference(reference.getId());
        if (ret) {
            refs.remove(reference);
            refs.sort(cmp);
        }
        final PropertyChangeEvent evt = new PropertyChangeEvent(this, PROP_REFERENCES, null, null);
        etest.getEventBus().post(evt);
    }

    public List<EditableProblem<?>> getReferenced() {
        if (refs == null) {
            refs = new ArrayList<>();
            asBasket().getReferences().stream()
                    .map(r -> etest.findProblem(r.getId()))
                    .filter(Objects::nonNull)
                    .forEach(refs::add);
            refs.sort(cmp);
//            updateMaxScore();
        }
        return refs;
    }

    @Override
    public void remove() {
        etest.getEditableProblems().stream()
                .filter(ep -> ep.isBasket())
                .map(ep -> ep.asBasket())
                .forEach(eb -> eb.removeReference(getId()));
        super.remove();
    }

    @Override
    public boolean isBasket() {
        return !getReferenced().isEmpty();
    }

    private Basket<Problem, Basket.Ref<?>> asBasket() {
        return (Basket<Problem, Basket.Ref<?>>) getProblem();
    }

    @Override
    public int getMaxScore() {
        if (!isBasket()) {
            return super.getMaxScore();
        }
        return (int) getRefSum().getWeightedSum();
    }

    private ProblemRefSum getRefSum() {
        if (refSum == null) {
//            updateMaxScore();
            refSum = new ProblemRefSum(this);
        }
        return refSum;
    }

    @Override
    public void setMaxScore(int maxScore) {
        if (isBasket()) {
            throw new UnsupportedOperationException("Cannot set maxscore on basket.");
        }
        super.setMaxScore(maxScore);
    }

    @Subscribe
    public void onModelChange(CollectionChangeEvent event) {
        if (event.getCollectionName().equals(EditableClassroomTest.COLLECTION_PROBLEMS)) {
            event.getItemAs(EditableProblem.class)
                    .ifPresent(refs::remove);
        }
    }

}
