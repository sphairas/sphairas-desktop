/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.model;

import com.google.common.eventbus.Subscribe;
import java.beans.PropertyChangeEvent;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.summary.Sum;
import org.thespheres.betula.util.CollectionChangeEvent;

/**
 *
 * @author boris.heithecker
 *///TODO: synchonized .....
class WeightedProblemScores {

    protected final EditableClassroomTest<?, ?, ?> ecal;
    private final DescriptiveStatistics values = new DescriptiveStatistics();
    private final DescriptiveStatistics weights = new DescriptiveStatistics();

    @SuppressWarnings("LeakingThisInConstructor")
    WeightedProblemScores(EditableClassroomTest cal) {
        this.ecal = cal;
        reset();
        cal.getEventBus().register(this);
    }

    private void reset() {
        synchronized (values) {
            values.clear();
            weights.clear();
            ecal.getEditableProblems().stream()
                    .filter(ep -> !ep.isBasket())
                    .forEach(r -> {
                        final Double w = r.getWeight();
                        if (w != null) {
                            weights.addValue(w);
                            values.addValue(r.getMaxScore());
                        }
                    });
        }
    }

    double getWeightedSum() {
        synchronized (values) {
            if (values.getValues().length == 0) {
                return 0d;
            }
            Sum s = new Sum();
            return s.evaluate(values.getValues(), weights.getValues());
        }
    }

    private void stateChanged() {
        reset();
        PropertyChangeEvent evt = new PropertyChangeEvent(this, EditableClassroomTest.PROP_PROBLEMSWEIGHTEDMAXIMUM, null, getWeightedSum());
        ecal.getEventBus().post(evt);
    }

    @Subscribe
    public void problemChange(CollectionChangeEvent event) {
        if (EditableClassroomTest.COLLECTION_PROBLEMS.equals(event.getCollectionName())) {
            stateChanged();
        }
    }

    @Subscribe
    public void propertyChange(PropertyChangeEvent evt) {
        final String name = evt.getPropertyName();
        if (EditableProblem.PROP_MAXSCORE.equals(name) || EditableProblem.PROP_WEIGHT.equals(name)) {
            stateChanged();
        }
    }

}
