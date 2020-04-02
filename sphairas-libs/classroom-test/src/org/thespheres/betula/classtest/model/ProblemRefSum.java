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
class ProblemRefSum {

    protected final EditableBasket<?> basket;
    private final DescriptiveStatistics values = new DescriptiveStatistics();
    private final DescriptiveStatistics weights = new DescriptiveStatistics();

    @SuppressWarnings("LeakingThisInConstructor")
    public ProblemRefSum(EditableBasket<?> basket) {
        this.basket = basket;
        basket.getEditableClassroomTest().getEventBus().register(this);
        reset();
    }

    private void reset() {
        synchronized (values) {
            values.clear();
            weights.clear();
            basket.getReferenced().stream()
                    .forEach(r -> {
                        final Double w = r.getWeight();
                        if (w != null) {
                            weights.addValue(w);
                            values.addValue(r.getMaxScore());
                        }
                    });
        }
    }

    public double getWeightedSum() {
        synchronized (values) {
            Sum s = new Sum();
            if (values.getN() != 0) {
                return s.evaluate(values.getValues(), weights.getValues());
            }
            return 0d;
        }
    }

    @Subscribe
    public void problemChange(CollectionChangeEvent event) {
        if (EditableClassroomTest.COLLECTION_PROBLEMS.equals(event.getCollectionName())) {
            event.getItemAs(EditableProblem.class).ifPresent(ep -> {
                if (event.getType().equals(CollectionChangeEvent.Type.REMOVE) && ep == basket) {
                    basket.getEditableClassroomTest().getEventBus().unregister(this);
                }
                reset();
            });
        }
    }

    @Subscribe
    public void propertyChange(PropertyChangeEvent evt) {
        final String name = evt.getPropertyName();
        if (evt.getSource() instanceof EditableProblem) {
            EditableProblem ep = (EditableProblem) evt.getSource();
            boolean contained = basket.getReferenced().stream()
                    .anyMatch(r -> r == ep);
            if (contained && (EditableProblem.PROP_MAXSCORE.equals(name) || EditableProblem.PROP_WEIGHT.equals(name))) {
                reset();
            }
            if (ep == basket && name.equals(EditableBasket.PROP_REFERENCES)) {
                reset();
            }
        }
    }

}
