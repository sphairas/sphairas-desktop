/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.table2;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import javax.swing.JFormattedTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.renderer.StringValue;

/**
 *
 * @author boris.heithecker
 */
public class LocalDoubleFormatter extends JFormattedTextField.AbstractFormatter implements DocumentListener, StringValue {

    private JFormattedTextField jft;
    private final NumberFormat nf = DecimalFormat.getInstance(Locale.getDefault());

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
    public Double stringToValue(final String string) throws ParseException {
        final String text = StringUtils.trimToNull(string);
        return text != null ? nf.parse(text).doubleValue() : null;
    }

    @Override
    public String valueToString(Object value) {
        if (value instanceof Number) {
            return nf.format(value);
        }
        return value != null ? value.toString() : null;
    }

    @Override
    public String getString(Object value) {
        return valueToString(value);
    }

    private void check() {
        if (this.jft == null) {
            return;
        }
        try {
            stringToValue(jft.getText());
//            Int2.validInt2(jft.getText());
            jft.commitEdit();
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
