/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thespheres.betula.noten.impl;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.thespheres.betula.util.Int2;

/**
 *
 * @author Boris Heithecker
 */
public class MarginModel {

    MarginModel(NotenAssessment context) {
        this.context = context;
        this.pSupport = new PropertyChangeSupport(context);
    }
    
    MarginModel(NotenAssessment context, MarginModel.Model model, Int2 marginValue) {
        this(context);
        this.marginModel = model;
        this.marginValue = marginValue;
    }
    
    public NotenGrade defineMargin(Int2 value, NotenGrade grade) {
        switch(marginModel) {
            case Margin: return defMarg(value, grade);
            case Equal: return defEqual(value, grade);
            default: return grade;
        }
    }

    public MarginModel.Model getMarginModel() {
        return marginModel;
    }

    public void setMarginModel(MarginModel.Model marginModel) {
        MarginModel.Model old = getMarginModel();
        this.marginModel = marginModel;
        pSupport.firePropertyChange("marginModel", old, marginModel);
    }

    public Int2 getMarginValue() {
        return marginValue;
    }

    public void setMarginValue(Int2 marginValue) {
        Int2 old = getMarginValue();
        this.marginValue = marginValue;
        pSupport.firePropertyChange("marginValue", old, marginValue);
    }

    private NotenGrade defEqual(Int2 value, NotenGrade grade) {
        Int2 floor = context.getAllocator().getFloor(grade);
        Int2 ceiling = context.getAllocator().getCeiling(grade);
        Int2 diff = ceiling.subtractFormThis(floor);
        if (diff.getInternalValue() < 3) {
            return grade;
        }
        Int2 mv = diff.divideBy(Int2.fromInternalValue(6));
        if (ceiling.subtractFormThis(value).subtractFormThis(mv).getInternalValue() < 0) {
            return grade.getCeilingBiased();
        } else if (value.subtractFormThis(floor).subtractFormThis(mv).getInternalValue() < 0) {
            return grade.getFloorBiased();
        } else {
            return grade;
        }
    }

    private NotenGrade defMarg(Int2 value, NotenGrade grade) {
        Int2 floor = context.getAllocator().getFloor(grade);
        Int2 ceiling = context.getAllocator().getCeiling(grade);
        Int2 diff = ceiling.subtractFormThis(floor);
        Int2 disc = diff.divideBy(marginValue.multiply(Int2.fromInternalValue(6)));
        if (disc.getInternalValue() <= 1) {
            return grade;
        }
        if (ceiling.subtractFormThis(value).subtractFormThis(marginValue).getInternalValue() < 0) {
            return grade.getCeilingBiased();
        } else if (value.subtractFormThis(floor).subtractFormThis(marginValue).getInternalValue() < 0) {
            return grade.getFloorBiased();
        } else {
            return grade;
        }
    }

    void addPropertyChangeListener(PropertyChangeListener l) {
        pSupport.addPropertyChangeListener(l);
    }

    void removePropertyChangeListener(PropertyChangeListener l) {
        pSupport.removePropertyChangeListener(l);
    }
    
    public enum Model {Margin, Equal}

    private final NotenAssessment context;
    private MarginModel.Model marginModel = Model.Margin;
    private Int2 marginValue = Int2.fromInternalValue(2);
    private PropertyChangeSupport pSupport;
}
