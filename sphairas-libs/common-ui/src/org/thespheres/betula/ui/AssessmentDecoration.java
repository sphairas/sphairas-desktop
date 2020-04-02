/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui;

import org.openide.util.Lookup;
import org.thespheres.betula.assess.Grade;

/**
 *
 * @author boris.heithecker
 */
public abstract class AssessmentDecoration {

    public static AssessmentDecoration getDefault() {
        return Lookup.getDefault().lookup(AssessmentDecoration.class);
    }

    public abstract AssessmentDecorationStyle[] styles();
    
    public abstract AssessmentDecorationStyle getStyle(Grade grade);
}
