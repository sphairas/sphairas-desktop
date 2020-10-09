/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document.util;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import org.thespheres.betula.Identity;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.TargetAssessment;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Entry;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.Template;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.util.GradeAdapter;

/**
 *
 * @author boris.heithecker
 * @param <I>
 * @param <S>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractGradeTargetAssessmentEntry<I extends Identity, S extends Identity> extends BaseTargetAssessmentEntry<I, S> {

    @XmlAttribute(name = "file-null-grade-entries")
    protected final boolean fileNullGradeEntries = true;
    @XmlAttribute(name = "override-timestamp-with-null")
    protected final boolean overrideTimestampWithNull = false;

    protected AbstractGradeTargetAssessmentEntry() {
    }

    protected AbstractGradeTargetAssessmentEntry(Action action, DocumentId id) {
        super(action, id); //TODO: fileNullGradeEntries = action.equals(Action.FILE)
    }

    public void submit(S student, Grade grade, Timestamp timestamp) {
        submit(student, null, grade, timestamp);
    }

    public void submit(final S student, final I gradeId, final Grade grade, final Timestamp timestamp) {
        submit(student, gradeId, grade, timestamp, null);
    }

    public Entry<S, Grade> submit(final S student, final I gradeId, final Grade grade, final Timestamp timestamp, final Action gradeIdAction) {
        return submit(student, gradeId, null, grade, timestamp, gradeIdAction);
    }

    // "process-bulk" = gradeIdAction = Action.File
    public Entry<S, Grade> submit(final S student, final I gradeId, final Marker section, final Grade grade, final Timestamp timestamp, final Action gradeIdAction) {
        if (student == null) {
            throw new IllegalArgumentException("Student null.");
        }

        Template current = this;
        if (gradeId != null) {
            current = findEntry(gradeId);
            if (current == null) {
                if (grade == null && !fileNullGradeEntries) {
                    return null;
                }
                current = new Entry(gradeIdAction, gradeId);
                getChildren().add(current);
            }
        }

        if (section != null) {
            final MarkerAdapter sma = new MarkerAdapter(section);
            Template sectionEntry = findSectionNode(sma, current);
            if (sectionEntry == null) {
                if (grade == null) {
                    return null;
                }
                sectionEntry = new Template(Action.FILE, sma);
                current.getChildren().add(sectionEntry);
            }
            current = sectionEntry;
        }

        Entry studEntry = findEntry(student, current);
        if (studEntry == null) {
            if (grade == null && !fileNullGradeEntries) {
                return null;
            }
            studEntry = new Entry(null, student);
            current.getChildren().add(studEntry);
        }
        if (grade == null && !fileNullGradeEntries) {
            current.getChildren().remove(studEntry);
            studentRemoved(student, gradeId);
        } else {
            final Grade old = findGrade(current);
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
        return select(student, null, null);
    }

    public Grade select(S student, I gradeId, Marker section) {
        if (student == null) {
            throw new IllegalArgumentException("Student null");
        }
        Template studEntry = findStudentEntry(student, gradeId, section);
        if (studEntry == null) {
            return null;
        }
        return findGrade(studEntry);
    }

    protected Entry findStudentEntry(S student, I gradeId, Marker section) {
        Template current = this;
        if (gradeId != null) {
            current = findEntry(gradeId);
            if (current == null) {
                return null;
            }
        }
        if (section != null) {
            final MarkerAdapter sma = new MarkerAdapter(section);
            current = findSectionNode(sma, current);
            if (current == null) {
                return null;
            }
        }
        return findEntry(student, current);
    }

    private Template findSectionNode(final MarkerAdapter section, final Template<?> parent) {
        for (final Template t : parent.getChildren()) {
            if (t.getValue() != null && t.getValue().equals(section)) {
                return t;
            }
        }
        return null;
    }

    protected Grade findGrade(Template stud) {
        try {
            return (stud.getValue() != null && stud.getValue() instanceof GradeAdapter) ? ((GradeAdapter) stud.getValue()).getGrade() : null;
        } catch (Exception ex) {
            return null;
        }
    }

    public Timestamp timestamp(S student) {
        return timestamp(student, null);
    }

    public Timestamp timestamp(S student, I gradeId) {
        return timestamp(student, null, null);
    }

    public Timestamp timestamp(S student, I gradeId, Marker section) {
        if (student == null) {
            throw new IllegalArgumentException("Student null");
        }
        Entry studEntry = findStudentEntry(student, gradeId, section);
        if (studEntry == null) {
            return null;
        }
        return studEntry.getTimestamp();
    }

    public String getPreferredConvention() {
        return getValue().getContentString(TargetAssessment.PROP_PREFERRED_CONVENTION);
    }

    public void setPreferredConvention(String con) {
        getValue().setContent(TargetAssessment.PROP_PREFERRED_CONVENTION, con);
    }

    protected abstract void studentChanged(S s, Identity id, Grade o, Grade n, Timestamp ts);

    protected abstract void studentRemoved(S s, Identity id);
}
