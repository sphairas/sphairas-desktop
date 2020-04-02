/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document;

import org.thespheres.betula.assess.*;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author boris.heithecker
 */
public class MarkerParsingException extends Exception {

    @Messages("message=Could not parse grade.")
    public MarkerParsingException(String convention, String longLabel) {
        super(NbBundle.getMessage(GradeParsingException.class, "message"));
    }
}
