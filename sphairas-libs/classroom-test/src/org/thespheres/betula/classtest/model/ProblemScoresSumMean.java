/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.model;

import org.thespheres.betula.util.CollectionElementPropertyChangeEvent;
import com.google.common.eventbus.Subscribe;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.thespheres.betula.util.CollectionChangeEvent;

/**
 *
 * @author boris.heithecker
 */
//TODO: synchonized...
class ProblemScoresSumMean {

    protected final EditableProblem<?> problem;
    private final DescriptiveStatistics values = new DescriptiveStatistics();

    @SuppressWarnings("LeakingThisInConstructor")
    ProblemScoresSumMean(EditableProblem cal) {
        this.problem = cal;
        cal.getEditableClassroomTest().getEventBus().register(this);
        //TODO register to students
        reset();
    }

    private void reset() {
        synchronized (values) {
            values.clear();
            problem.getEditableClassroomTest().getEditableStudents().stream()
                    .forEach(r -> {
                        final Double w = r.getStudentScores().get(problem.getId());
                        if (w != null) {
                            values.addValue(w);
                        } //Else warn corrupt file
                    });
        }
    }

    double getMean() {
        synchronized (values) {
            return values.getMean();
        }
    }

    @Subscribe
    public void studentCollectionChange(CollectionChangeEvent event) {
        if (EditableClassroomTest.COLLECTION_STUDENTS.equals(event.getCollectionName())) {
            reset();
        }
    }

    @Subscribe
    public void studentScoresChange(CollectionElementPropertyChangeEvent event) {
        if (event.getElementKey().equals(problem.getId())) {
            reset();
        }
    }
}
