/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.analytics;

import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.NumberValueGrade;

/**
 *
 * @author boris.heithecker
 */
public class ClasstestAnalytics {

    private final static ClasstestAnalytics INSTANCE = new ClasstestAnalytics();

    private ClasstestAnalytics() {
    }

    public static ClasstestAnalytics getInstance() {
        return INSTANCE;
    }

    public Double valueOf(Grade g) {
        if (g == null) {
            return null;
        }
        if (g instanceof Grade.Biasable) {
            g = ((Grade.Biasable) g).getUnbiased();
        }
        if (g instanceof NumberValueGrade) {
            NumberValueGrade ng = (NumberValueGrade) g;
            return ng.getNumberValue().doubleValue();
        }
        return null;
    }

}
