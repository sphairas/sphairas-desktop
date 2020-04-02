/*
 * Int2Spinner.java
 *
 * Created on 15. November 2007, 18:05
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.thespheres.betula.noten.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.thespheres.betula.util.Int2;
import org.thespheres.betula.assess.Grade;

/**
 *
 * @author Boris Heithecker
 */
class Int2PercentageSpinner extends Int2Spinner {

    private final Grade grade;

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    Int2PercentageSpinner(Grade grade, AbstractInt2Assessment context) {
        super(context);
        this.grade = grade;
        Int2PercentageSpinnerModel model = new Int2PercentageSpinnerModel();
        context.addPropertyChangeListener(model);
        setModel(model);
    }

    public class Int2PercentageSpinnerModel extends Int2SpinnerModel implements PropertyChangeListener {

//        private Int2PercentageSpinnerModel() {
//            //Int2 i = context.getAllocator().getFloor(grade);
//        }
        //Hier verändern
        @Override
        public Int2 getValue() {
            Int2 value = context.getAllocator().getFloor(Int2PercentageSpinner.this.grade);
            return Int2.percentageOfValue(value, context.getRangeMaximum());
        }

        //Hier verändern
        @Override
        public void setValue(Object percentage) {
            //if(true) return;
            if ((percentage == null) || !(percentage instanceof Int2)) {
                throw new IllegalArgumentException("illegal value");
            }
//            Int2 min = context.getAllocator().getFloor(grade);
//            Int2 max = context.getAllocator().getCeiling(grade);
            try {
                final Int2 value = Int2.valueOfPercentage((Int2) percentage, context.getRangeMaximum());
                final Int2 before = context.getAllocator().getFloor(Int2PercentageSpinner.this.grade);
                context.getAllocator().setFloor(Int2PercentageSpinner.this.grade, value);
                //fireStateChanged();
                class Edit extends AbstractUndoableEdit {

                    @Override
                    public void redo() throws CannotRedoException {
                        context.getAllocator().setFloor(Int2PercentageSpinner.this.grade, value);
                    }

                    @Override
                    public void undo() throws CannotUndoException {
                        context.getAllocator().setFloor(Int2PercentageSpinner.this.grade, before);
                    }

                }
                context.undoSupport.postEdit(new Edit());
            } catch (IllegalArgumentException ex) {
                //illegal value;
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!Int2PercentageSpinner.this.grade.toString().equals(evt.getPropertyName())) {
                return;
            }
            fireStateChanged();
        }
    }

}
