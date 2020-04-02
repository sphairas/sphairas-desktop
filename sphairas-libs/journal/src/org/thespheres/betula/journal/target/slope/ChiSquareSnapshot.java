/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.target.slope;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.thespheres.betula.Student;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GradeFactory;
import org.thespheres.betula.journal.JournalConfiguration;
import org.thespheres.betula.journal.analytics.JournalAnalytics;
import org.thespheres.betula.journal.model.EditableRecord;
import org.thespheres.betula.util.GradeEntry;

/**
 *
 * @author boris.heithecker
 */
class ChiSquareSnapshot {

    private final DescriptiveStatistics stat = new DescriptiveStatistics();
    private final EditableRecord record;
    private final Grade[] grades;
    final Map<Grade, Long> sizeMap;

    public ChiSquareSnapshot(final EditableRecord record) {
        this.record = record;
        grades = Optional.ofNullable(JournalConfiguration.getInstance().getJournalEntryPreferredConvention())
                .map(GradeFactory::findConvention)
                .map((AssessmentConvention c) -> Arrays.asList(c.getAllGradesReverseOrder()))
                .orElse((List<Grade>) Collections.EMPTY_LIST)
                .stream()
                .filter((Grade g) -> JournalAnalytics.getInstance().valueOf(g) != null)
                .toArray(Grade[]::new);
        final Map<Student, GradeEntry> m = record.getRecord().getStudentEntries();
        sizeMap = m.values().stream()
                .filter(ge -> Arrays.stream(grades).anyMatch(ge.getGrade()::equals))
                .collect(Collectors.groupingBy(GradeEntry::getGrade, Collectors.counting()));
    }

    Result test(final double sigLevel) {
        stat.clear();
        for (Map.Entry<Grade, Long> e : sizeMap.entrySet()) {
            final double value = JournalAnalytics.getInstance().valueOf(e.getKey());
            for (long l = 0l; l < e.getValue(); l++) {
                stat.addValue(value);
            }
        }
        final long[] values = Arrays.stream(grades)
                .mapToLong(de -> sizeMap.getOrDefault(de, 0L))
                .toArray();
        final long n = Arrays.stream(values).sum();
        final double statDev = stat.getStandardDeviation();
        final Result ret = new Result();
        if (statDev == 0.0d) {
            return ret;
        }
        final NormalDistribution pdf = new NormalDistribution(stat.getMean(), statDev);
        final double[] expected = new double[grades.length];
        for (int i = 0; i < expected.length; i++) {
            final double[] bounds = bounds(grades, i);
            expected[i] = pdf.probability(bounds[0], bounds[1]) * n;
            ret.expected.put(grades[i], expected[i]);
//            ret.values.put(grades[i], values[i]);
        }
        final ChiSquareTest test = new ChiSquareTest();
        ret.test = test.chiSquareTest(expected, values, sigLevel);
        ret.deviation = statDev;
        return ret;
    }

    class Result {

        double deviation;
        boolean test;
        Map<Grade, Double> expected = new HashMap<>();
//        Map<Grade, Long> values = new HashMap<>();
    }

    static boolean isChiSquareNormalDist(final EditableRecord record, final double d) {
        final Map<Student, GradeEntry> m = record.getRecord().getStudentEntries();
        double[] arr = m.keySet().stream()
                .map(m::get)
                .map(g -> JournalAnalytics.getInstance().valueOf(g.getGrade()))
                .filter(Objects::nonNull)
                .mapToDouble(d1 -> d1)
                .toArray();

        final Map<Grade, Long> sizeMap = m.values().stream()
                .collect(Collectors.groupingBy(GradeEntry::getGrade, Collectors.counting()));
        final Grade[] grades = Optional.ofNullable(JournalConfiguration.getInstance().getJournalEntryPreferredConvention())
                .map(GradeFactory::findConvention)
                .map((AssessmentConvention c) -> Arrays.asList(c.getAllGradesReverseOrder()))
                .orElse((List<Grade>) Collections.EMPTY_LIST)
                .stream()
                .filter((Grade g) -> JournalAnalytics.getInstance().valueOf(g) != null)
                .toArray(Grade[]::new);
        final long[] values = Arrays.stream(grades).mapToLong((Grade de) -> sizeMap.getOrDefault(de, 0L)).toArray();
        final long n = Arrays.stream(values).sum();
        final NormalDistribution pdf = new NormalDistribution(record.getGradesMean().getMean(), record.getGradesMean().getDeviation());
        final double[] expected = new double[grades.length];
        for (int i = 0; i < expected.length; i++) {
            final double[] bounds = bounds(grades, i);
            expected[i] = pdf.probability(bounds[0], bounds[1]) * n;
        }
        final ChiSquareTest test = new ChiSquareTest();
        return test.chiSquareTest(expected, values, d);
    }

    static double[] bounds(final Grade[] grs, final int index) {
        final Grade g = grs[index];
        double gv = JournalAnalytics.getInstance().valueOf(g);
        final double[] ret = new double[2];
        if (index == grs.length - 1) {
            ret[1] = Double.MAX_VALUE;
        } else {
            double uv = JournalAnalytics.getInstance().valueOf(grs[index + 1]);
            ret[1] = (uv + gv) / 2;
        }
        if (index == 0) {
            ret[0] = Double.MIN_VALUE;
        } else {
            double lv = JournalAnalytics.getInstance().valueOf(grs[index - 1]);
            ret[0] = (lv + gv) / 2;
        }
        return ret;
    }

}
