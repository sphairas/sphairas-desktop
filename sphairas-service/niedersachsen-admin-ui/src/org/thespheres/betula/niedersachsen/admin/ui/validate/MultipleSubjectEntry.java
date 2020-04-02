/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.validate;

import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.validation.ValidationResult;

/**
 *
 * @author boris.heithecker
 */
public class MultipleSubjectEntry extends ValidationResult {

    private final StudentId student;
    private final DocumentId document;
    private final TermId term;

    public MultipleSubjectEntry(final DocumentId documentId, final StudentId studentId, final TermId termId) {
        this.document = documentId;
        this.student = studentId;
        this.term = termId;
    }

    public StudentId getStudent() {
        return student;
    }

    public DocumentId getDocument() {
        return document;
    }

    public TermId getTerm() {
        return term;
    }

}
