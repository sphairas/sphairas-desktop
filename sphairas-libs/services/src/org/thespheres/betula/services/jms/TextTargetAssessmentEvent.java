/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.jms;

import java.io.Serializable;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.document.Timestamp;

/**
 *
 * @author boris.heithecker
 */
public class TextTargetAssessmentEvent extends AbstractDocumentEvent implements QualifiedEvent, Serializable {

    private static final long serialVersionUID = 1L;
    private final StudentId student;
    private final TermId term;
    private final String text;
    private final String old;
    private final Timestamp time;

    private TextTargetAssessmentEvent(DocumentId source, DocumentEventType type, StudentId student, TermId term, String text, String old, Timestamp time, Signee signee) {
        super(source, type, signee);
        this.student = student;
        this.term = term;
        this.text = text;
        this.old = old;
        this.time = time;
    }

    public TextTargetAssessmentEvent(DocumentId source, StudentId student, TermId term, String text, String old, Timestamp time, Signee signee) {
        this(source, DocumentEventType.CHANGE, student, term, text, old, time, signee);
    }

    public TextTargetAssessmentEvent(DocumentId source, DocumentEventType event, Signee signee) {
        this(source, event, null, null, null, null, null, signee);
    }

    public StudentId getStudentId() {
        return student;
    }

    public TermId getTermId() {
        return term;
    }

    public String getNewValue() {
        return text;
    }

    public String getOldValue() {
        return old;
    }

    @Override
    public Timestamp getTimestamp() {
        return time;
    }
}
