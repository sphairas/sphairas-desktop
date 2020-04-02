/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.utilities;

/**
 *
 * @author boris.heithecker
 */
public class ImportConfigurationException extends IllegalStateException {

    public ImportConfigurationException() {
    }

    public ImportConfigurationException(String s) {
        super(s);
    }

    public ImportConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImportConfigurationException(Throwable cause) {
        super(cause);
    }

}
