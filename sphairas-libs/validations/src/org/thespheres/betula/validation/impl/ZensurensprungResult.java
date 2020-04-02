/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.validation.impl;

import java.time.LocalDateTime;
import org.thespheres.betula.Student;
import org.thespheres.betula.TermId;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.TargetDocument;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.validation.ValidationResult;

/**
 *
 * @author boris.heithecker
 * @param <S>
 * @param <T>
 */
public abstract class ZensurensprungResult<S extends Student, T extends TargetDocument> extends ValidationResult {

    private final S student;
    private final T document;
    private final Grade gradeBefore;
    private final Grade gradeCurrent;
    private final DocumentId documentId;
    private final TermId term;

    public ZensurensprungResult(S student, TermId term, Grade before, Grade current, DocumentId did, T document, LocalDateTime time) {
        super(time);
        this.student = student;
        this.term = term;
        this.documentId = did;
        this.document = document;
        this.gradeBefore = before;
        this.gradeCurrent = current;
    }

    public ZensurensprungResult(S student, TermId term, Grade before, Grade current, DocumentId did, T document) {
        this.student = student;
        this.term = term;
        this.documentId = did;
        this.document = document;
        this.gradeBefore = before;
        this.gradeCurrent = current;
    }

    public S getStudent() {
        return student;
    }

    public TermId getTerm() {
        return term;
    }

    public DocumentId getDocumentId() {
        return documentId;
    }

    public T getTargetDocument() {
        return document;
    }

    public Grade getGradeBefore() {
        return gradeBefore;
    }

    public Grade getGradeCurrent() {
        return gradeCurrent;
    }

}
