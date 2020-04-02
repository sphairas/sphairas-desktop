/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.util;

/**
 *
 * @author boris.heithecker
 */
public class LocalizedIllegalArgumentException extends IllegalArgumentException {

    private String localizedMessage;

    public LocalizedIllegalArgumentException() {
    }

    public LocalizedIllegalArgumentException(String s) {
        super(s);
    }

    public LocalizedIllegalArgumentException(String message, Throwable cause) {
        super(message, cause);
    }

    public LocalizedIllegalArgumentException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getLocalizedMessage() {
        return localizedMessage;
    }

    public void setLocalizedMessage(String localizedMessage) {
        this.localizedMessage = localizedMessage;
    }

}
