/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.validate;

import java.text.Collator;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.niedersachsen.admin.ui.ReportData2;
import org.thespheres.betula.validation.impl.ZensurenschnittResult;
import org.thespheres.betula.validation.impl.ZensurenschnittValidationConfiguration;

/**
 *
 * @author boris.heithecker
 */
class ZensurenschnittResultImpl extends ZensurenschnittResult<RemoteStudent, ReportData2> {

    private final static NumberFormat FORMAT = NumberFormat.getNumberInstance(Locale.getDefault());

    static {
        FORMAT.setMaximumFractionDigits(2);
        FORMAT.setMinimumFractionDigits(2);
    }

    ZensurenschnittResultImpl(RemoteStudent student, ReportData2 report, ZensurenschnittValidationConfiguration config) {
        super(student, report, config);
    }

    String message() {
        final StringJoiner mainSj = new StringJoiner(" ");
        mainSj.add(FORMAT.format(getAverage()));
        final String collect = getFiltered().entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey(), Collator.getInstance(Locale.getDefault())))
                .map(e -> FORMAT.format(e.getValue()))
                .collect(Collectors.joining("/", "(", ")"));
        if (!collect.isEmpty()) {
            mainSj.add(collect);
        }
        return mainSj.toString();
    }
}
