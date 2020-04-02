/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.util;

import java.io.IOException;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author boris.heithecker
 */
@Messages({"WriteLockException.localizedMessage=Die Resource {0} konnte nicht gesperrt werden."})
public class WriteLockException extends IOException {

    private final String resource;

    public WriteLockException(final String resource) {
        super();
        this.resource = resource;
    }

    @Override
    public String getLocalizedMessage() {
        return NbBundle.getMessage(WriteLockException.class, "WriteLockException.localizedMessage", resource);
    }

}
