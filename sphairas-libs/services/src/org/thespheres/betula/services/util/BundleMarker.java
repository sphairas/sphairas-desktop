/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.util;

import java.io.Serializable;
import java.text.MessageFormat;
import org.thespheres.betula.document.AbstractMarker;

/**
 *
 * @author boris.heithecker
 */
public class BundleMarker extends AbstractMarker implements Serializable {

    private String message;

    public BundleMarker(String convention, String id, String message) {
        super(convention, id, null);
        this.message = message;
    }

    @Override
    public String getLongLabel(Object... args) {
        return MessageFormat.format(getMessage(), args);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String msg) {
        this.message = msg;
    }

}
