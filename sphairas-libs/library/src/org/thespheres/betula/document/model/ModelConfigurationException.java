/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document.model;

/**
 *
 * @author boris.heithecker
 */
public class ModelConfigurationException extends IllegalStateException {

    public ModelConfigurationException() {
    }

    public ModelConfigurationException(String s) {
        super(s);
    }

    public ModelConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModelConfigurationException(Throwable cause) {
        super(cause);
    }

}
