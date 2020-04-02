/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document.util;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import org.thespheres.betula.Identity;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.TargetAssessment;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.Document;
import org.thespheres.betula.document.DocumentEntry;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Entry;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.util.GradeAdapter;

/**
 *
 * @author boris.heithecker
 * @param <I>
 * @param <S>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractTargetAssessmentEntry<I extends Identity, S extends Identity> extends DocumentEntry {

    @XmlAttribute(name = "file-null-grade-entries")
    protected final boolean fileNullGradeEntries = true;
    @XmlAttribute(name = "override-timestamp-with-null")
    protected final boolean overrideTimestampWithNull = false;

    protected AbstractTargetAssessmentEntry() {
    }

    protected AbstractTargetAssessmentEntry(Action action, DocumentId id) {
        super(action, id); //TODO: fileNullGradeEntries = action.equals(Action.FILE)
    }

    @Override
    public GenericXmlDocument getValue() {
        return (GenericXmlDocument) super.getValue();
    }

    protected Grade findGrade(Entry stud) {
        try {
            return (stud.getValue() != null && stud.getValue() instanceof GradeAdapter) ? ((GradeAdapter) stud.getValue()).getGrade() : null;
        } catch (Exception ex) {
            return null;
        }
    }

    public void submit(S student, Grade grade, Timestamp timestamp) {
        submit(student, null, grade, timestamp);
    }

    public void submit(final S student, final I gradeId, final Grade grade, final Timestamp timestamp) {
        submit(student, gradeId, grade, timestamp, null);
    }

    // "process-bulk" = gradeIdAction = Action.File
    public Entry<S, Grade> submit(final S student, final I gradeId, final Grade grade, final Timestamp timestamp, final Action gradeIdAction) {
        if (student == null) {
            throw new IllegalArgumentException("Student null.");
        }
        Entry gradeEntry = this;
        if (gradeId != null) {
            gradeEntry = findEntry(gradeId);
            if (gradeEntry == null) {
                if (grade == null && !fileNullGradeEntries) {
                    return null;
                }
                gradeEntry = new Entry(gradeIdAction, gradeId);
                getChildren().add(gradeEntry);
            }
        }
        Entry studEntry = findEntry(student, gradeEntry);
        if (studEntry == null) {
            if (grade == null && !fileNullGradeEntries) {
                return null;
            }
            studEntry = new Entry(null, student);
            gradeEntry.getChildren().add(studEntry);
        }
        if (grade == null && !fileNullGradeEntries) {
            gradeEntry.getChildren().remove(studEntry);
            studentRemoved(student, gradeId);
        } else {
            final Grade old = findGrade(gradeEntry);
            final Action ac = grade == null ? Action.ANNUL : Action.FILE;  //TODO: action
            studEntry.setAction(ac);
            if (grade != null) {
                studEntry.setValue(new GradeAdapter(grade));
            } else {
                studEntry.setValue(null);
            }
            if (timestamp != null || overrideTimestampWithNull) {//TODO: setNullTimestamp??
                studEntry.setTimestamp(timestamp);
            }
            studentChanged(student, gradeId, old, grade, timestamp);
        }
        return studEntry;
    }

    public Grade select(S student) {
        return select(student, null);
    }

    public Grade select(S student, I gradeId) {
        if (student == null) {
            throw new IllegalArgumentException("Student null");
        }
        Entry studEntry = findStudentEntry(student, gradeId);
        if (studEntry == null) {
            return null;
        }
        return findGrade(studEntry);
    }

    protected Entry findStudentEntry(S student, I gradeId) {
        Entry gradeEntry = this;
        if (gradeId != null) {
            gradeEntry = findEntry(gradeId);
            if (gradeEntry == null) {
                return null;
            }
        }
        return findEntry(student, gradeEntry);
    }

    public Timestamp timestamp(S student) {
        return timestamp(student, null);
    }

    public Timestamp timestamp(S student, I gradeId) {
        if (student == null) {
            throw new IllegalArgumentException("Student null");
        }
        Entry studEntry = findStudentEntry(student, gradeId);
        if (studEntry == null) {
            return null;
        }
        return studEntry.getTimestamp();
    }

    public abstract Set<S> students();

    public Set<I> identities() {
        return getChildren().stream()
                .filter(Entry.class::isInstance)
                .map(Entry.class::cast)
                .map(e -> {
                    try {
                        return (I) e.getIdentity();
                    } catch (ClassCastException cce) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public ZonedDateTime getDocumentValidity() {
        final Document.Validity dv = getValue().getDocumentValidity();
        return dv == null ? null : dv.getExpirationDate();
    }

    public void setDocumentValidity(ZonedDateTime deleteDate) {
        getValue().setDocumentValidity(deleteDate);
    }

    public String getPreferredConvention() {
        return getValue().getContentString(TargetAssessment.PROP_PREFERRED_CONVENTION);
    }

    public void setPreferredConvention(String con) {
        getValue().setContent(TargetAssessment.PROP_PREFERRED_CONVENTION, con);
    }

    public String getTargetType() {
        return getValue().getContentString(TargetAssessment.PROP_TARGETTYPE);
    }

    public void setTargetType(String type) {
        getValue().setContent(TargetAssessment.PROP_TARGETTYPE, type);
    }

    public String getSubjectAlternativeName() {
        return getValue().getContentString(TargetAssessment.PROP_SUBJECT_NAME);
    }

    public void setSubjectAlternativeName(String name) {
        getValue().setContent(TargetAssessment.PROP_SUBJECT_NAME, name);
    }

    protected abstract void studentChanged(S s, Identity id, Grade o, Grade n, Timestamp ts);

    protected abstract void studentRemoved(S s, Identity id);
}
