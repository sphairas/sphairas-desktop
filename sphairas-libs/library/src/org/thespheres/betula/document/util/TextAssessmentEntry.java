/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document.util;

import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Entry;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.Template;
import org.thespheres.betula.document.Timestamp;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "betula-text-assessment-document", namespace = "http://www.thespheres.org/xsd/betula/container.xsd")
@XmlType(name = "textAssessmentEntryType", namespace = "http://www.thespheres.org/xsd/betula/container.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class TextAssessmentEntry extends BaseTargetAssessmentEntry<TermId, StudentId> implements Serializable {

    private static final long serialVersionUID = 1L;

    public TextAssessmentEntry() {
    }

    @SuppressWarnings("OverridableMethodCallInConstructor")
    private TextAssessmentEntry(Action action, DocumentId id, GenericXmlDocument doc) {
        super(action, id);
        setValue(doc);
    }

    public TextAssessmentEntry(DocumentId id, Action action, boolean fragment) {
        this(action, id, new GenericXmlDocument(fragment));
    }

    @Override
    public Set<StudentId> students() {
        return getChildren().stream()
                .filter(Entry.class::isInstance)
                .map(Entry.class::cast)
                .flatMap(e -> e.getChildren().stream())
                .filter(Entry.class::isInstance)
                .map(Entry.class::cast)
                .map(Entry::getIdentity)
                .filter(StudentId.class::isInstance)
                .map(StudentId.class::cast)
                .collect(Collectors.toSet());
    }

    public Entry<StudentId, String> submit(final StudentId student, final TermId term, final Marker section, final String text, final Timestamp timestamp, final Action gradeIdAction) {
        if (student == null) {
            throw new IllegalArgumentException("Student cannot be null.");
        }
        Template current = this;
        if (term != null) {
            current = findEntry(term);
            if (current == null) {
                if (text == null) {
                    return null;
                }
                current = new Entry(gradeIdAction, term);
                getChildren().add(current);
            }
        }

        if (section != null) {
            final MarkerAdapter sma = new MarkerAdapter(section);
            Template sectionEntry = findSectionNode(sma, current);
            if (sectionEntry == null) {
                if (text == null) {
                    return null;
                }
                sectionEntry = new Template(Action.FILE, sma);
                current.getChildren().add(sectionEntry);
            }
            current = sectionEntry;
        }

        Entry<StudentId, String> studEntry = findEntry(student, current);
        if (studEntry == null) {
            if (text == null) {
                return null;
            }
            studEntry = new Entry(Action.FILE, student);
            current.getChildren().add(studEntry);
        }

        if (text == null) {
            current.getChildren().remove(studEntry);
        } else {
//            String old = findGrade(current);
            studEntry.setValue(text);
            studEntry.setTimestamp(timestamp);
        }
        return studEntry;
    }

    public String select(final StudentId student, final TermId gradeId, final Marker section) {
        if (student == null) {
            throw new IllegalArgumentException("Student null");
        }
        Entry studEntry = findStudentEntry(student, gradeId, section);
        if (studEntry == null) {
            return null;
        }
        return getGrade(studEntry);
    }

    private String getGrade(Entry e) {
        return (e.getValue() != null && e.getValue() instanceof String) ? (String) e.getValue() : null;
    }

    private Entry findStudentEntry(final StudentId student, final TermId term, final Marker section) {
        Template current = this;
        if (term != null) {
            current = findEntry(term);
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

    public Timestamp timestamp(final StudentId student, final TermId gradeId, final Marker section) {
        if (student == null) {
            throw new IllegalArgumentException("Student null");
        }
        final Entry studEntry = findStudentEntry(student, gradeId, section);
        if (studEntry == null) {
            return null;
        }
        return studEntry.getTimestamp();
    }

}
