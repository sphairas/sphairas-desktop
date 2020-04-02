/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services;

import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author boris.heithecker
 */
@Messages("NoProviderException.message=No provider \"{0}\" of type \"{1}\" could be found.")
public class NoProviderException extends IllegalStateException {

    protected final String url;
    protected final String type;

    public NoProviderException(Class type, String url) {
        this.url = url;
        this.type = type.getName();
    }

    @Override
    public String getMessage() {
        return NbBundle.getMessage(NoProviderException.class, "NoProviderException.message", url, type);
    }

}
