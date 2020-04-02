/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.uiutil;

import java.text.ParseException;
import java.util.regex.Pattern;
import javax.swing.JFormattedTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.commons.lang3.StringUtils;
import org.thespheres.betula.Identity;
import org.thespheres.betula.xmlimport.ImportTarget;

/**
 *
 * @author boris.heithecker
 * @param <I>
 */
public abstract class IdFormatter<I extends Identity> extends JFormattedTextField.AbstractFormatter implements DocumentListener {

    public static final String NULL_LABEL = "---";
    protected static final Pattern UIDPATTERN = Pattern.compile("([\\w]+(-[\\w]+)*)", Pattern.UNICODE_CHARACTER_CLASS);
    protected JFormattedTextField jft;
    protected ImportTarget config;

    public void initialize(ImportTarget cfg) {
        config = cfg;
    }

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
    public abstract String valueToString(Object value) throws ParseException;

    @Override
    public abstract I stringToValue(String text) throws ParseException;

    protected void check() {
        final String uid = jft.getText();
        if (!checkUid(uid)) {
            this.invalidEdit();
        }
    }

    protected boolean checkUid(final String text) {
        final String uid = StringUtils.trimToNull(text);
        return uid == null || UIDPATTERN.matcher(uid).matches();
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
