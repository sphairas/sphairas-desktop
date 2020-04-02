/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.assess;

import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author boris.heithecker
 */
public class GradeParsingException extends Exception {

    private final AbstractGrade stub;

    @Messages("message=Could not parse grade.")
    public GradeParsingException(String convention, String text) {
        super(NbBundle.getMessage(GradeParsingException.class, "message"));
        this.stub = new AbstractGrade(convention != null ? convention : "null", text != null ? text : "null");
    }

    public AbstractGrade getGradeStub() {
        return stub;
    }
}
