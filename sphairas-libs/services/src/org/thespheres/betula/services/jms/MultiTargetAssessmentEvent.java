/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.jms;

import java.io.Serializable;
import org.thespheres.betula.Identity;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.services.jms.MultiTargetAssessmentEvent.Update;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.document.Timestamp;

/**
 *
 * @author boris.heithecker
 * @param <G>
 */
public class MultiTargetAssessmentEvent<G extends Identity> extends AbstractDocumentEvent implements QualifiedEvent, Serializable {

    private final Update<G>[] updates;
    private final Timestamp time;

    public MultiTargetAssessmentEvent(DocumentId source, Update<G>[] updates, Signee signee, Timestamp time) {
        super(source, DocumentEventType.CHANGE, signee);
        this.updates = updates;
        this.time = time;
    }

    public MultiTargetAssessmentEvent(DocumentId source, Update<G>[] updates, Signee signee) {
        this(source, updates, signee, Timestamp.now());
    }

    public MultiTargetAssessmentEvent(DocumentId source, DocumentEventType event, Signee signee, Timestamp time) {
        super(source, event, signee);
        this.updates = null;
        this.time = time;
    }

    public MultiTargetAssessmentEvent(DocumentId source, DocumentEventType event, Signee signee) {
        this(source, event, signee, Timestamp.now());
    }

    public Update<G>[] getUpdates() {
        return updates;
    }

    @Override
    public Timestamp getTimestamp() {
        return time;
    }

    public static class Update<G extends Identity> implements Serializable {

        private final StudentId student;
        private final G gradeId;
        private final Grade grade;
        private final Grade old;
        private final Timestamp time;

        public Update(StudentId student, G gradeId, Grade old, Grade grade, Timestamp time) {
            this.student = student;
            this.gradeId = gradeId;
            this.grade = grade;
            this.old = old;
            this.time = time;
        }

        public StudentId getStudent() {
            return student;
        }

        public G getGradeId() {
            return gradeId;
        }

        public Grade getValue() {
            return grade;
        }

        public Grade getOldValue() {
            return old;
        }

        public Timestamp getTimestamp() {
            return time;
        }

    }
}
