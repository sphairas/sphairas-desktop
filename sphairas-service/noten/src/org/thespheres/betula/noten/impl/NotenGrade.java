/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.noten.impl;

import org.thespheres.betula.assess.AbstractGrade;
import org.thespheres.betula.assess.NumberValueGrade;

/**
 *
 * @author Boris Heithecker
 */
public abstract class NotenGrade extends AbstractGrade implements NumberValueGrade {

    protected NotenGrade(String gradeConvention, String gradeId) {
        super(gradeConvention, gradeId);
    }

    public abstract boolean isBiased();

    public abstract boolean isCeilingBiased();

    public abstract boolean isFloorBias();

    public abstract NotenGrade getCeilingBiased();

    public abstract NotenGrade getFloorBiased();

    public abstract NotenGrade getUnbiased();

}
