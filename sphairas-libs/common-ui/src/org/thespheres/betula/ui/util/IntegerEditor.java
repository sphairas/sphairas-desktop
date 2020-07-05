/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.thespheres.betula.ui.util;

/*
 * IntegerEditor is used by TableFTFEditDemo.java.
 */
import java.awt.Color;
import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.Component;
import java.awt.Toolkit;
import java.text.NumberFormat;
import java.text.ParseException;
import javax.swing.border.LineBorder;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

/**
 * Implements a cell editor that uses a formatted text field to edit Integer
 * values.
 */
public class IntegerEditor extends DefaultCellEditor {

    private final JFormattedTextField ftf;
    private final NumberFormat nf;
    private final Integer min;
    private final Integer max;

    public IntegerEditor(int min, int max) {
        super(new JFormattedTextField());
        this.min = min;
        this.max = max;
        nf = NumberFormat.getIntegerInstance();
        NumberFormatter formatter = new NumberFormatter(nf);
        formatter.setFormat(nf);
        formatter.setMinimum(min);
        formatter.setMaximum(max);
        ftf = (JFormattedTextField) editorComponent;
        ftf.setFormatterFactory(new DefaultFormatterFactory(formatter));
        ftf.setValue(min);
        ftf.setBorder(new LineBorder(Color.BLACK, 2));
        ftf.setHorizontalAlignment(JTextField.TRAILING);
        ftf.setFocusLostBehavior(JFormattedTextField.PERSIST);

        //Respond when the user presses Enter while the editor is
        //active.  (Tab is handled as specified by
        //JFormattedTextField's focusLostBehavior property.)
        ftf.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "check");
        ftf.getActionMap().put("check", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!ftf.isEditValid()) {
                    if (userSaysRevert()) {
                        ftf.postActionEvent();
                    }
                } else {
                    try {
                        ftf.commitEdit();
                        ftf.postActionEvent();
                    } catch (ParseException exc) {
                    }
                }
            }
        });
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        JFormattedTextField tField = (JFormattedTextField) super.getTableCellEditorComponent(table, value, isSelected, row, column);
        tField.setValue(value);
        return tField;
    }

    @Override
    public Object getCellEditorValue() {
        final Object o = ftf.getValue();
        if (o instanceof Integer) {
            return o;
        } else if (o instanceof Number) {
            return ((Number) o).intValue();
        } else if (o != null) {
            try {
                return nf.parseObject(o.toString());
            } catch (final ParseException ex) {
            }
        }
        return null;
    }

    @Override
    public boolean stopCellEditing() {
        if (ftf.isEditValid()) {
            try {
                ftf.commitEdit();
            } catch (ParseException exc) {
            }

        } else if (!userSaysRevert()) {
            return false;
        }
        return super.stopCellEditing();
    }

    /**
     * Lets the user know that the text they entered is bad. Returns true if the
     * user elects to revert to the last good value. Otherwise, returns false,
     * indicating that the user wants to continue
     *
     * @return editing.
     */
    protected boolean userSaysRevert() {
        Toolkit.getDefaultToolkit().beep();
        ftf.selectAll();
        Object[] options = {"Edit",
            "Revert"};
        int answer = JOptionPane.showOptionDialog(
                SwingUtilities.getWindowAncestor(ftf),
                "The value must be an integer between "
                + min + " and "
                + max + ".\n"
                + "You can either continue editing "
                + "or revert to the last valid value.",
                "Invalid Text Entered",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                options,
                options[1]);

        if (answer == 1) { //Revert!
            ftf.setValue(ftf.getValue());
            return true;
        }
        return false;
    }
}
