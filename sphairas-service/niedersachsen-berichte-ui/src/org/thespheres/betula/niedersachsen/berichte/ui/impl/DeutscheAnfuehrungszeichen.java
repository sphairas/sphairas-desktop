/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.berichte.ui.impl;

import org.netbeans.api.editor.mimelookup.MimePath;
import javax.swing.text.BadLocationException;
import org.apache.commons.lang3.StringUtils;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.spi.editor.typinghooks.TypedTextInterceptor;

/**
 *
 * @author boris.heithecker
 */
public class DeutscheAnfuehrungszeichen implements TypedTextInterceptor {
    
    @Override
    public void insert(final MutableContext context) throws BadLocationException {
        if ("\"".equals(context.getText())) {
            final int pos = context.getOffset();
            if (pos > 0) {
                final String charBefore = context.getDocument().getText(pos - 1, 1);
                if (!StringUtils.isBlank(charBefore)) {
                    context.setText("“", 1);
                    return;
                }
            }
            context.setText("„", 1);
        }
    }
    
    @Override
    public boolean beforeInsert(Context context) throws BadLocationException {
        return false;
    }
    
    @Override
    public void afterInsert(Context context) throws BadLocationException {
    }
    
    @Override
    public void cancelled(Context context) {
    }
    
    @MimeRegistration(mimeType = "text/betula-remote-reports", service = TypedTextInterceptor.Factory.class)
    public static class Factory implements TypedTextInterceptor.Factory {
        
        @Override
        public TypedTextInterceptor createTypedTextInterceptor(MimePath mimePath) {
            return new DeutscheAnfuehrungszeichen();
        }
        
    }
}
