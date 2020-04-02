/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.model;

import com.google.common.eventbus.Subscribe;
import java.beans.PropertyChangeEvent;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.thespheres.betula.util.CollectionChangeEvent;

/**
 *
 * @author boris.heithecker
 */
//TODO: synchonized...
class ClasstestScoresSumMean {

    protected final EditableClassroomTest<?, ?, ?>  ecal;
    private final DescriptiveStatistics values = new DescriptiveStatistics();

    @SuppressWarnings("LeakingThisInConstructor")
    ClasstestScoresSumMean(EditableClassroomTest cal) {
        this.ecal = cal;
        cal.getEventBus().register(this);
        reset();
    }

    private void reset() {
        synchronized (values) {
            values.clear();
            ecal.getEditableStudents().stream()
                    .forEach(r -> {
                        final Double w = r.getStudentScores().sum();
                        values.addValue(w);
                    });
        }
    }

    double getMean() {
        synchronized (values) {
            return values.getMean();
        }
    }

    @Subscribe
    public void studentChange(CollectionChangeEvent event) {
        if (EditableClassroomTest.COLLECTION_STUDENTS.equals(event.getCollectionName())) {
            reset();
        }
    }

    @Subscribe
    public void studentProperty(PropertyChangeEvent evt) {
        if (EditableStudent.PROP_SCORES.equals(evt.getPropertyName()) || evt.getSource() instanceof EditableStudent) {
            reset();
        }
    }
}
