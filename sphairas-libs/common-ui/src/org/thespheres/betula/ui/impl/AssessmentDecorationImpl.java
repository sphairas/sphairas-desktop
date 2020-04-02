/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.impl;

import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.NumberValueGrade;
import org.thespheres.betula.ui.AssessmentDecoration;
import org.thespheres.betula.ui.AssessmentDecorationStyle;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = AssessmentDecoration.class) //TODO: Move to NdS allg. oder so
public class AssessmentDecorationImpl extends AssessmentDecoration {

    private final AssessmentDecorationStyle[] styles = new AssessmentDecorationStyle[]{
        new AssessmentDecorationStyle("defizit", "#ff0000", null, null, null, null),
        new AssessmentDecorationStyle("defizit", "#ff0000", null, null, null, null)
    };

    @Override
    public AssessmentDecorationStyle[] styles() {
        return styles;
    }

    @Override
    public AssessmentDecorationStyle getStyle(Grade grade) {
        if (grade != null) {
            if (grade.getConvention().equals("de.notensystem")) {
                final NumberValueGrade note = (NumberValueGrade) grade;
                if (note.getNumberValue().doubleValue() > 4.5d) {
                    return styles[0];
                } else if (note.getNumberValue().doubleValue() > 4.0d) {
//                    return styles[1];
                }
            }
        }
        return null;
    }

}
