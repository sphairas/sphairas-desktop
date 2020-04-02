/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.noten.impl;

import java.awt.LayoutManager;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import javax.swing.AbstractSpinnerModel;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.event.ChangeListener;
import org.thespheres.betula.util.Int2;

/**
 *
 * @author Boris Heithecker
 */
public class Int2Spinner extends JSpinner {

    protected final AbstractInt2Assessment context;

    protected Int2Spinner(AbstractInt2Assessment ctx) {
        super();
        this.context = ctx;
    }

    @Override
    protected JComponent createEditor(SpinnerModel model) {
        return new Int2SpinnerEditor();
    }

    public class Int2SpinnerEditor extends JSpinner.DefaultEditor implements ChangeListener, PropertyChangeListener, LayoutManager {

        @SuppressWarnings({"OverridableMethodCallInConstructor"})
        public Int2SpinnerEditor() {
            super(Int2Spinner.this);
            JFormattedTextField ftf = getTextField();
            ftf.setEditable(true);
            final JFormattedTextField.AbstractFormatter af = new JFormattedTextField.AbstractFormatter() {

                @Override
                public Object stringToValue(String text) throws ParseException {
                    return new Int2(text);
                }

                @Override
                public String valueToString(Object value) throws ParseException {
                    if (value == null || !(value instanceof Int2)) {
                        return "";
                    } else {
                        return ((Int2) value).toString();
                    }
                }
            };
            ftf.setFormatterFactory(new AbstractFormatterFactory() {

                @Override
                public JFormattedTextField.AbstractFormatter getFormatter(JFormattedTextField tf) {
                    return af;
                }
            });
            ftf.setHorizontalAlignment(JTextField.RIGHT);
        }
    }

    public abstract class Int2SpinnerModel extends AbstractSpinnerModel {

        protected Int2SpinnerModel() {
            //Int2 i = context.getAllocator().getFloor(grade);
        }

        private Int2 incrValue(int dir) {
            return Int2.fromInternalValue(((Int2) getValue()).getInternalValue() + dir);
        }

        @Override
        public Object getNextValue() {
            return incrValue(1);
        }

        @Override
        public Object getPreviousValue() {
            return incrValue(-1);
        }

    }
}
