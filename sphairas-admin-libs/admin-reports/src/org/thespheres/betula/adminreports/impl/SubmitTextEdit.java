/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminreports.impl;

import org.openide.util.RequestProcessor.Task;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.admin.units.SubmitResult;
import org.thespheres.betula.document.Marker;

/**
 *
 * @author boris.heithecker
 */
public class SubmitTextEdit {

    Task task;
    SubmitResult result;
    private final String text;
    private final TermId term;
    private final Marker section;
    private final StudentId student;
    private final RemoteTextTargetAssessmentDocument rttad;

    SubmitTextEdit(RemoteTextTargetAssessmentDocument rttad, StudentId student, TermId term, Marker section, String text) {
        this.rttad = rttad;
        this.student = student;
        this.term = term;
        this.section = section;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public TermId getTerm() {
        return term;
    }

    public Marker getSection() {
        return section;
    }

    public StudentId getStudent() {
        return student;
    }

    public SubmitResult getResult() {
        return result;
    }

    void setResult(SubmitResult result) {
        this.result = result;
    }

}
