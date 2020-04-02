/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.util;

import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GradeParsingException;

/**
 *
 * @author boris.heithecker
 */
public abstract class DelegatingAssessmentConvention implements AssessmentConvention {

    private AssessmentConvention delegate;

    protected DelegatingAssessmentConvention() {
    }

    protected AssessmentConvention getDelegate() {
        if (this.delegate == null) {
            throw new IllegalStateException("Delegate not set.");
        }
        return delegate;
    }

    protected final void setDelegate(final AssessmentConvention delegate) {
        if (this.delegate != null) {
            throw new IllegalStateException("Delegate alread set.");
        }
        this.delegate = delegate;
    }

    @Override
    public String getName() {
        return getDelegate().getName();
    }

    @Override
    public String getDisplayName() {
        return getDelegate().getDisplayName();
    }

    @Override
    public Grade[] getAllGrades() {
        return getDelegate().getAllGrades();
    }

    @Override
    public Grade parseGrade(String text) throws GradeParsingException {
        return getDelegate().parseGrade(text);
    }

    @Override
    public Grade find(String id) {
        return getDelegate().find(id);
    }

}
