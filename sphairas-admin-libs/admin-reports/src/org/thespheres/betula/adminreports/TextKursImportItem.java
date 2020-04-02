/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminreports;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.xmlimport.ImportItem;

/**
 *
 * @author boris.heithecker
 */
public abstract class TextKursImportItem extends ImportItem {

//    private final StudentId[] student;
    private Marker fach;
    private Signee signee;
    private final DocumentId target;
    private final Set<TextEntry> grades = new HashSet<>();
    private String preferredSectionConventionName;

    protected TextKursImportItem(DocumentId target) {
        super(target.toString());
//        this.student = studentId;
        this.target = target;
    }

    public DocumentId getTargetDocument() {
        return target;
    }

    public abstract UnitId getUnit();

//    public StudentId[] students() {
//        return student;
//    }
    public abstract void initializeMarkers(final Marker[] markers);

    @Override
    public boolean isFragment() {
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    public Marker getFach() {
        return fach;
    }

    public void setFach(Marker fach) {
        this.fach = fach;
    }

    public Signee getSignee() {
        return signee;
    }

    public void setSignee(Signee signee) {
        this.signee = signee;
    }

    private Optional<TextEntry> entry(final StudentId student, final TermId gradeId, final Marker section) {
        return grades.stream()
                .filter(g -> g.student.equals(student) && Objects.equals(g.term, gradeId) && Objects.equals(g.section, section))
                .collect(CollectionUtil.requireSingleton());
    }

    public String select(final StudentId student, final TermId term, final Marker section) {
        return entry(student, term, section).
                map(g -> g.text)
                .orElse(null);
    }

    public Timestamp timestamp(StudentId student, TermId term, final Marker section) {
        return entry(student, term, section).
                map(g -> g.timestamp)
                .orElse(null);
    }

    public void submit(final StudentId student, final TermId term, final Marker section, final String text, final Timestamp timestamp) {
        entry(student, term, section)
                .orElseGet(() -> {
                    final TextEntry ge = createGradeEntry(student, term, section);
                    grades.add(ge);
                    return ge;
                })
                .set(text, timestamp);
    }

    protected TextEntry createGradeEntry(final StudentId stud, final TermId term, final Marker section) {
        return new TextEntry(stud, term, section);
    }

    public Set<TextEntry> entries() {
        return grades.stream()
                .collect(Collectors.toSet());
    }

    public Set<TermId> terms() {
        return grades.stream()
                .map(TextEntry::getTerm)
                .collect(Collectors.toSet());
    }

    public Set<StudentId> students() {
        return grades.stream()
                .map(TextEntry::getStudent)
                .collect(Collectors.toSet());
    }

    public String getPreferredSectionConventionName() {
        return preferredSectionConventionName;
    }

    public void setPreferredSectionConventionName(String preferredSectionConventionName) {
        this.preferredSectionConventionName = preferredSectionConventionName;
    }

    public class TextEntry {

        protected final StudentId student;
        protected final TermId term;
        protected String text;
        protected Marker section;
        protected Timestamp timestamp;

        protected TextEntry(StudentId student, TermId term, Marker section) {
            this.student = student;
            this.term = term;
            this.section = section;
        }

        public StudentId getStudent() {
            return student;
        }

        public TermId getTerm() {
            return term;
        }

        public Marker getSection() {
            return section;
        }

        protected void set(String text, Timestamp ts) {
            this.text = text;
            this.timestamp = ts;
        }

        public String getText() {
            return text;
        }

        public Timestamp getTimestamp() {
            return timestamp;
        }

        public boolean isValid() {
            return timestamp != null;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 83 * hash + Objects.hashCode(this.student);
            hash = 83 * hash + Objects.hashCode(this.term);
            return 83 * hash + Objects.hashCode(this.section);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TextEntry other = (TextEntry) obj;
            if (!Objects.equals(this.student, other.student)) {
                return false;
            }
            if (!Objects.equals(this.term, other.term)) {
                return false;
            }
            return Objects.equals(this.section, other.section);
        }

    }
}
