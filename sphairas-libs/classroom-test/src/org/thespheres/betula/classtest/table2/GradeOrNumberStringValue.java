/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.table2;

import org.jdesktop.swingx.renderer.StringValue;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.classtest.module2.ClasstestConfiguration;

/**
 *
 * @author boris.heithecker
 */
final class GradeOrNumberStringValue implements StringValue {

    private final LocalDoubleFormatter ldf = new LocalDoubleFormatter();

    GradeOrNumberStringValue() {
    }

    @Override
    public String getString(Object value) {
        if (value instanceof Grade) {
            Grade g = (Grade) value;
            if (ClasstestConfiguration.useLongLabel()) {
                return g.getLongLabel();
            } else {
                return g.getShortLabel();
            }
        }
        return ldf.valueToString(value);
    }

}
