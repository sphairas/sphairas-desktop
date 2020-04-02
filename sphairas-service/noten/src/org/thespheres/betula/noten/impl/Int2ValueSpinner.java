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
class Int2ValueSpinner extends Int2Spinner {

    private final Grade grade;

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    Int2ValueSpinner(Grade grade, AbstractInt2Assessment context) {
        super(context);
        this.grade = grade;
        Int2ValueSpinnerModel model = new Int2ValueSpinnerModel();
        context.addPropertyChangeListener(model);
        setModel(model);
    }

    public class Int2ValueSpinnerModel extends Int2SpinnerModel implements PropertyChangeListener {

//        private Int2ValueSpinnerModel() {
//            //Int2 i = context.getAllocator().getFloor(grade);
//        }
        @Override
        public Int2 getValue() {
            return context.getAllocator().getFloor(Int2ValueSpinner.this.grade);
        }

        @Override
        public void setValue(Object value) {
            if ((value == null) || !(value instanceof Int2)) {
                throw new IllegalArgumentException("illegal value");
            }
//            Int2 min = context.getAllocator().getFloor(grade);
//            Int2 max = context.getAllocator().getCeiling(grade);
            try {
                Int2 before = context.getAllocator().getFloor(Int2ValueSpinner.this.grade);
                context.getAllocator().setFloor(Int2ValueSpinner.this.grade, (Int2) value);
                class Edit extends AbstractUndoableEdit {

                    @Override
                    public void redo() throws CannotRedoException {
                        context.getAllocator().setFloor(Int2ValueSpinner.this.grade, (Int2) value);
                    }

                    @Override
                    public void undo() throws CannotUndoException {
                        context.getAllocator().setFloor(Int2ValueSpinner.this.grade, before);
                    }

                }
                context.undoSupport.postEdit(new Edit());
            } catch (IllegalArgumentException ex) {
                //illegal value;
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!Int2ValueSpinner.this.grade.toString().equals(evt.getPropertyName())) {
                return;
            }
            fireStateChanged();
        }
    }

}
