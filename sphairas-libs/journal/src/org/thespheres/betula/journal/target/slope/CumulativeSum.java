/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.target.slope;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.thespheres.betula.RecordId;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.journal.JournalConfiguration;
import org.thespheres.betula.journal.analytics.JournalAnalytics;
import org.thespheres.betula.journal.model.EditableJournal;
import org.thespheres.betula.journal.model.EditableParticipant;

/**
 *
 * @author boris.heithecker
 */
public class CumulativeSum {

    public static Map<RecordId, PointAt2> create2(final EditableJournal<?, ?> ej, final int part) {
        final EditableParticipant ep = ej.getEditableParticipants().get(part);
        final double wm = ep.getWeightedGradesMean().getWeightedMean();
        final double wdev = ep.getWeightedGradesMean().getWeightedDeviation();

        final Map<RecordId, PointAt2> l = new HashMap<>();
        ej.getEditableRecords().forEach(er -> {
            final Grade gradeAt = er.getGradeAt(part);
            if (gradeAt != null) {
                final Double val = JournalAnalytics.getInstance().valueOf(gradeAt);
                if (val != null) {
                    double w = JournalAnalytics.getInstance().calculateRealWeight(er);
                    final PointAt2 pa = new PointAt2(er.getIndex(), gradeAt, val, w);
                    l.put(er.getRecordId(), pa);
                    try {
                        final ChiSquareSnapshot csq = new ChiSquareSnapshot(er);
                        ChiSquareSnapshot.Result test = csq.test(0.05d);
                        pa.chisq = test.test;
                        if (test.test) {
                            final Grade dg = JournalConfiguration.getInstance().getJournalDefaultGrade();
                            long dSize = csq.sizeMap.get(dg);
                            double diff = dSize - test.expected.get(dg);
                            if (diff > 1.0d) {
                                csq.sizeMap.compute(dg, (g, s) -> s - 1);
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }
        });
        final int n = l.size();
        final double weightsSum = l.values().stream()
                .mapToDouble(p -> p.weight)
                .sum();
//        double cSum = 0d;
//        System.out.println(ep.getSurname() + " " + wdev);
        List<PointAt2> lp2 = l.values().stream()
                .sorted(Comparator.comparingInt(p2 -> p2.index))
                .collect(Collectors.toList());
        PointAt2 before = null;
        for (int i = 0; i < lp2.size(); i++) {
            PointAt2 p = lp2.get(i);
            p.setAdjustedValue(n, weightsSum);
            if (p.getAdjustedValue() != 0.0d) {
                double beforeV = 0.0d;
                if (before != null) {
                    beforeV = before.cSum;
                }
                p.cSum = Math.max(0, beforeV + (p.getAdjustedValue() - wm));
                p.ratio = p.cSum / wdev;
                before = p;
            }
//            System.out.println(ep.getSurname() + " " + p.index + " " + cSum);
        }
        return l;
    }

    public static List<PointAt> create(final EditableJournal<?, ?> ej, final int part) {
        final EditableParticipant ep = ej.getEditableParticipants().get(part);
        final double wm = ep.getWeightedGradesMean().getWeightedMean();
        final double wdev = ep.getWeightedGradesMean().getWeightedDeviation();

        final List<PointAt> l = new ArrayList<>();
        ej.getEditableRecords().forEach(er -> {
            final Grade gradeAt = er.getGradeAt(part);
            if (gradeAt != null) {
                final Double val = JournalAnalytics.getInstance().valueOf(gradeAt);
                if (val != null) {
                    double w = JournalAnalytics.getInstance().calculateRealWeight(er);
                    final PointAt pa = new PointAt(er.getIndex(), gradeAt, val, w);
                    l.add(pa);
                    try {
                        pa.chisq = ChiSquareSnapshot.isChiSquareNormalDist(er, 0.05d);
                    } catch (Exception e) {
                    }

                }
            }
        });
        final int n = l.size();
        final double weightsSum = l.stream()
                .mapToDouble(p -> p.weight)
                .sum();
//        double cSum = 0d;
//        System.out.println(ep.getSurname() + " " + wdev);
        for (final PointAt p : l) {
            p.adjustedValue = p.value * p.weight * (double) n / weightsSum;
            p.cSum = Math.max(0, p.cSum + (p.adjustedValue - wm));
            p.ratio = p.cSum / wdev;
//            System.out.println(ep.getSurname() + " " + p.index + " " + cSum);
        }
        return l;
    }

    static class PointAt2 {

        private final Grade grade;
        private final double value;
        private double weight;
        private double adjustedValue;
        private double cSum;
        double ratio;
        boolean chisq;
        final int index;

        private PointAt2(final int index, final Grade grade, double value, double weight) {
            this.index = index;
            this.grade = grade;
            this.value = value;
            this.weight = weight;
        }

        public Grade getGrade() {
            return grade;
        }

        public double getValue() {
            return value;
        }

        void setWeight(double w) {
            this.weight = w;
        }

        public double getWeight() {
            return weight;
        }

        void setAdjustedValue(int n, double weightsSum) {
            this.adjustedValue = value * weight * (double) n / weightsSum;
        }

        public double getAdjustedValue() {
            return adjustedValue;
        }

        public double getcSum() {
            return cSum;
        }

    }

    static class PointAt {

        private final int index;
        private final Grade grade;
        private final double value;
        private final double weight;
        private double adjustedValue;
        private double cSum;
        double ratio;
        boolean chisq;

        private PointAt(int index, final Grade grade, double value, double weight) {
            this.index = index;
            this.grade = grade;
            this.value = value;
            this.weight = weight;
        }

        public int getIndex() {
            return index;
        }

        public Grade getGrade() {
            return grade;
        }

        public double getValue() {
            return value;
        }

        public double getWeight() {
            return weight;
        }

        public double getAdjustedValue() {
            return adjustedValue;
        }

        public double getcSum() {
            return cSum;
        }

    }
}
