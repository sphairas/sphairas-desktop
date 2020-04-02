/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.target.slope;

import java.text.NumberFormat;
import org.apache.commons.math3.fitting.HarmonicCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.stat.StatUtils;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.journal.analytics.JournalAnalytics;
import org.thespheres.betula.journal.model.EditableJournal;
import org.thespheres.betula.journal.model.EditableParticipant;
import org.thespheres.betula.journal.model.EditableRecord;

/**
 *
 * @author boris.heithecker
 */
public class HarmonicCurveParams {

    private final double amplitude;
    private final double angularFrequency;
    private final double phase;
    final static NumberFormat NF = NumberFormat.getNumberInstance();

    static {
        NF.setMinimumFractionDigits(2);
    }
    private double meanDerivativeOverSample;

    private HarmonicCurveParams(final double[] coeff) {
        this.amplitude = coeff[0];
        this.angularFrequency = coeff[1];
        this.phase = coeff[2];
    }

    public static HarmonicCurveParams create(final EditableJournal<?, ?> ej, final int part) {
        final WeightedObservedPoints obs = new WeightedObservedPoints();
        final EditableParticipant ep = ej.getEditableParticipants().get(part);
        final double mean = ep.getWeightedGradesMean().getMean();
        int at = 0;
        for (final EditableRecord<?> er : ej.getEditableRecords()) {
            final Grade gradeAt = er.getGradeAt(part);
            if (gradeAt != null) {
                final Double val = JournalAnalytics.getInstance().valueOf(gradeAt);
                if (val != null) {
                    double w = JournalAnalytics.getInstance().calculateRealWeight(er);
                    obs.add(w, (double) at++, val - mean);
                }
            }
        }
        final HarmonicCurveFitter fitter = HarmonicCurveFitter.create();
        final double[] coeff = fitter.fit(obs.toList());
        final HarmonicCurveParams ret = new HarmonicCurveParams(coeff);
        final double[] deriv = new double[at];
        for (int i = 0; i < at; i++) {
//            deriv[i] = ret.getAmplitude() * ret.getAngularFrequency() * Math.cos(ret.getAngularFrequency() * (double) i + ret.getPhase());
            deriv[i] = ret.getAngularFrequency() * 45d + Math.cos(ret.getAngularFrequency() * (double) i + ret.getPhase()) + 45d;
        }
        double meanDeriv = StatUtils.mean(deriv);
        ret.meanDerivativeOverSample = meanDeriv;
        return ret;
    }

    public double getAmplitude() {
        return amplitude;
    }

    public double getAngularFrequency() {
        return angularFrequency;
    }

    public double getPhase() {
        return phase;
    }

    public double getPeriod() {
        return (double) (2 * Math.PI / getAngularFrequency());
    }

    public double getMeanDerivativeOverSample() {
        return meanDerivativeOverSample;
    }

    @Override
    public String toString() {
        return NF.format(getAmplitude()) + "/" + NF.format(getPeriod()) + "/" + NF.format(getPhase());
    }

}
