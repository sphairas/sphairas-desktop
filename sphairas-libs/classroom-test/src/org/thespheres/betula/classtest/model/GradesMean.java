/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.model;

import java.util.Objects;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.thespheres.betula.classtest.analytics.ClasstestAnalytics;

/**
 *
 * @author boris.heithecker
 */
public class GradesMean {

    private final DescriptiveStatistics values = new DescriptiveStatistics();
    private final EditableClassroomTest<?, ?, ?> etest;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public GradesMean(EditableClassroomTest<?, ?, ?> etest) {
        this.etest = etest;
        reset();
    }

    public synchronized void reset() {
        synchronized (values) {
            values.clear();
            etest.getEditableStudents().stream()
                    .map(es -> es.getStudentScores().getGrade())
                    .map(g -> ClasstestAnalytics.getInstance().valueOf(g))
                    .filter(Objects::nonNull)
                    .forEach(values::addValue);
        }
    }

    public double normalDistribution(double arg) {
        NormalDistribution nd;
        //Need to synchronized access mean/sd pair
        synchronized (values) {
            final double sd = values.getStandardDeviation();
            if (sd == 0d) {
                return 0d;
            }
            nd = new NormalDistribution(values.getMean(), sd);
        }
        return nd.probability(arg - 0.5d, arg + 0.5d);
    }

    public double getMean() {
        synchronized (values) {
            return values.getMean();
        }
    }

    public double getDeviation() {
        synchronized (values) {
            return values.getStandardDeviation();
        }
    }

    public double getSum() {
        synchronized (values) {
            return values.getSum();
        }
    }
}
