/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.util;

/**
 *
 * @author boris.heithecker
 */
public class UITimeoutException extends RuntimeException {

    public UITimeoutException(String message) {
        super(message);
    }

    public UITimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public UITimeoutException(Throwable cause) {
        super(cause);
    }

}
