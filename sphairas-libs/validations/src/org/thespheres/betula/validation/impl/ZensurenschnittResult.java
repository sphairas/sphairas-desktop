/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.validation.impl;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.thespheres.betula.Student;
import org.thespheres.betula.document.model.ReportDocument;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.validation.ValidationResult;

/**
 *
 * @author boris.heithecker
 * @param <S>
 * @param <R>
 */
public abstract class ZensurenschnittResult<S extends Student, R extends ReportDocument> extends ValidationResult {

    private final static NumberFormat NF = NumberFormat.getNumberInstance(Locale.getDefault());
    static {
        NF.setMaximumFractionDigits(2);
    }
    private final S student;
    private final R report;
    private Double average;
    private final Map<String, Double> filtered = new HashMap<>();
    protected final ZensurenschnittValidationConfiguration config;

    public ZensurenschnittResult(S student, R report, ZensurenschnittValidationConfiguration config) {
        this.student = student;
        this.report = report;
        this.config = config;
    }

    public S getStudent() {
        return student;
    }

    public R getDocument() {
        return report;
    }

    public Double getAverage() {
        return average;
    }

    public void setAverage(Double average) {
        this.average = average;
    }

    public Map<String, Double> getFiltered() {
        return filtered;
    }

    @Override
    public String toString() {
        final StringJoiner mainSj = new StringJoiner(" ");
        mainSj.add(NF.format(getAverage()));
        final String collect = getFiltered().entrySet().stream()
                .map(this::toString)
                .collect(Collectors.joining(" / ", "(", ")"));
        if (!collect.isEmpty()) {
            mainSj.add(collect);
        }
        return mainSj.toString();
    }

    private String toString(Map.Entry<String, Double> e) {
        return Arrays.stream(config.getSubjectGroups())
                .filter(sf -> sf.getName().equals(e.getKey()))
                .collect(CollectionUtil.singleton())
                .map(sf -> sf.getDisplayName() + ": " + NF.format(e.getValue()))
                .orElse("?");
    }
}
