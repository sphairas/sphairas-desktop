/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.analytics;

import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.journal.model.EditableRecord;

/**
 *
 * @author boris.heithecker
 */
public class JournalAnalytics {

    private final static JournalAnalytics INSTANCE = new JournalAnalytics();

    private JournalAnalytics() {
    }

    public static JournalAnalytics getInstance() {
        return INSTANCE;
    }

    public Double valueOf(Grade g) {
        if (g == null) {
            return null;
        }
        if ("progress".equals(g.getConvention())) {
            return Double.parseDouble(g.getId());
        }
        if (g.getConvention().equals("mitarbeit2")) {
            String id = g.getId();
            switch (id) {
                case "plus-plus":
                    return 100.0;
                case "plus":
                    return 85.0;
                case "x-plus":
                    return 55.0;
                case "x":
                    return 40.0;
                case "minus":
                    return 30.0;
                case "minus-minus":
                    return 10.0;
            }
        }
        return null;
    }

    public double calculateRealWeight(EditableRecord<?> r) {
        double weight = r.getGradesMean().getDeviation();
        double userWeight = r.getWeight();
        return (double) weight * userWeight;
    }

}
