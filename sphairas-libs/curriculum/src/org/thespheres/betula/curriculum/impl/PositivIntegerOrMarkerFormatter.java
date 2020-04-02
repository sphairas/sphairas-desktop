/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculum.impl;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import javax.swing.JFormattedTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author boris.heithecker
 */
public class PositivIntegerOrMarkerFormatter extends JFormattedTextField.AbstractFormatter implements DocumentListener {

    private JFormattedTextField jft;
    protected final static NumberFormat NF = DecimalFormat.getIntegerInstance(Locale.getDefault());

    @Override
    public void install(JFormattedTextField ftf) {
        super.install(ftf);
        this.jft = ftf;
        this.jft.getDocument().addDocumentListener(this);
    }

    @Override
    public void uninstall() {
        super.uninstall();
        if (this.jft != null) {
            this.jft.getDocument().removeDocumentListener(this);
            this.jft = null;
        }
    }

    @Override
    public Integer stringToValue(final String string) throws ParseException {
        final String text = StringUtils.trimToNull(string);
        return text != null ? NF.parse(text).intValue() : null;
    }

    @Override
    public String valueToString(Object value) {
        if (value instanceof Number) {
            return NF.format(value);
        }
        return value != null ? value.toString() : null;
    }

    private void check() {
        if (this.jft == null) {
            return;
        }
        try {
            final Integer value = stringToValue(jft.getText());
            if (value != null && value < 0) {
                invalidEdit();
            } else {
                jft.commitEdit();
            }
        } catch (ParseException ex) {
            invalidEdit();
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        check();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        check();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        check();
    }

}
