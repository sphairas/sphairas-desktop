/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport;

import java.beans.PropertyVetoException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.event.EventListenerList;
import org.thespheres.betula.xmlimport.utilities.TargetDocumentProperties;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GradeTermTargetAssessment;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.assess.IdentityTargetAssessment;
import org.thespheres.betula.xmlimport.utilities.UpdaterFilter;

/**
 *
 * @author boris.heithecker
 */
public abstract class ImportTargetsItem extends ImportItem implements GradeTermTargetAssessment {

    public static final String PROP_UNIQUE_SUBJECT = "unique-subject";
    public static final String PROP_SUBJECTS = "subjects";
    public static final String PROP_UNITID = "unitId";
    public static final String PROP_SIGNEE = "signee";
    public static final String PROP_SELECTED_TERM = "selected.term";
    public static final String PROP_SUBJECT_ALT_NAME = "subject.alternative.name";
    protected StudentId[] students;
    protected String termScheduleProvider;
    private final Set<GradeEntry> grades = new HashSet<>();
    protected String preferredConvention;
    protected String subjectAlternativeName;
    protected AssessmentConvention convention;
    private final EventListenerList listeners = new EventListenerList();
    protected UnitId unit;
    protected Set<Marker> subject = Collections.EMPTY_SET;
    protected String sourceSubject;
    protected Signee signee;
    protected String sourceSignee;
    private final Map<String, UpdaterFilter> filterChain = new HashMap<>();

    public ImportTargetsItem(String sourceNode) {
        super(sourceNode);
    }

    protected ImportTargetsItem(String sourceNode, String sourceSubject, String sourceSignee) {
        super(sourceNode);
        this.sourceSubject = sourceSubject;
        this.sourceSignee = sourceSignee;
    }

    public StudentId[] getUnitStudents() {
        return students;
    }

    //LSchB.PROVIDER_INFO.getURL()
    public String getPreferredTermSchedule() {
        return termScheduleProvider;
    }

    protected void setPreferredTermScheduleProvider(String termScheduleProvider) {
        this.termScheduleProvider = termScheduleProvider;
    }

    public abstract DocumentId getTargetDocumentIdBase();//without suffix -zeugnisnoten etc

    public DocumentId[] getTargetDocumentIdBaseOptions() {
        return new DocumentId[]{getTargetDocumentIdBase()};
    }

    public void setTargetDocumentIdBase(final DocumentId did) {
        if (did == null
                || (getUnitId() != null && !Arrays.stream(getTargetDocumentIdBaseOptions()).anyMatch(did::equals))) {
            throw new IllegalArgumentException("DocumentId must match any of TargetDocumentIdBaseOptions");
        }
        setTargetDocumentIdBaseOption(did);
    }

    protected void setTargetDocumentIdBaseOption(final DocumentId did) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public abstract TargetDocumentProperties[] getImportTargets();

    //!untisKurs.isKlassenfach()
    public abstract boolean fileUnitParticipants();

    @Override
    public Grade select(StudentId student, TermId term) {
        return entry(student, term).
                map(g -> g.grade)
                .orElse(null);
    }

    @Override
    public Timestamp timestamp(StudentId student, TermId term) {
        return entry(student, term).
                map(g -> g.timestamp)
                .orElse(null);
    }

    public Optional<GradeEntry> entry(StudentId student, TermId gradeId) {
        return grades.stream()
                .filter(g -> g.student.equals(student) && Objects.equals(g.term, gradeId))
                .collect(CollectionUtil.requireSingleton());
    }

    @Override
    public void submit(final StudentId stud, final TermId term, final Grade grade, final Timestamp ts) {
        grades.stream()
                .filter(g -> g.student.equals(stud) && Objects.equals(g.term, term))
                .collect(CollectionUtil.requireSingleton())
                .orElseGet(() -> {
                    final GradeEntry ge = createGradeEntry(stud, term);
                    grades.add(ge);
                    return ge;
                })
                .set(grade, ts);
    }

    public GradeEntry createGradeEntry(final StudentId stud, final TermId term) {
        return new GradeEntry(stud, term);
    }

    @Override
    public Set<StudentId> students() {
        return grades.stream()
                .map(GradeEntry::getStudent)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<TermId> identities() {
        return grades.stream()
                .map(GradeEntry::getTerm)
                .collect(Collectors.toSet());
    }

    @Override
    public String getPreferredConvention() {
        return convention != null ? convention.getName() : preferredConvention;
    }

    public String getSubjectAlternativeName() {
        return subjectAlternativeName;
    }

    public AssessmentConvention getAssessmentConvention() {
        return convention;
    }

    public void setAssessmentConvention(AssessmentConvention assessmentConvention) {
        convention = assessmentConvention;
    }

    @Override
    public void addListener(final GradeTermTargetAssessment.Listener listener) {
        listeners.add(IdentityTargetAssessment.Listener.class, listener);
    }

    @Override
    public void removeListener(final GradeTermTargetAssessment.Listener listener) {
        listeners.remove(IdentityTargetAssessment.Listener.class, listener);
    }

    public UnitId getUnitId() {
        return unit;
    }

    public void setUnitId(UnitId unit) {
        UnitId old = getUnitId();
        this.unit = unit;
        try {
            vSupport.fireVetoableChange(PROP_UNITID, old, unit);
        } catch (PropertyVetoException ex) {
            this.unit = old;
        }
    }

    @Override
    public Marker[] allMarkers() {
//        final Stream<Marker> subst = !Marker.isNull(subject) ? Stream.of(subject) : Stream.empty();
        return Stream.concat(subject.stream(), uniqueMarkers.stream()).toArray(Marker[]::new);
    }

    public String getSourceSubject() {
        return sourceSubject;
    }

    public Marker getSubjectMarker() {
        return subject.stream().collect(CollectionUtil.singleOrNull());
    }

    public Marker[] getSubjectMarkers() {
        return subject.stream().toArray(Marker[]::new);
    }

    public void setSubjectMarker(final Marker fach) {
        this.setSubjectMarker(new Marker[]{fach});
    }

    public void setSubjectMarker(final Marker[] fach) {
        final Set<Marker> old = subject;
        this.subject = Arrays.stream(fach).collect(Collectors.toSet());
        try {
            vSupport.fireVetoableChange(PROP_UNIQUE_SUBJECT, old.stream().collect(CollectionUtil.singleOrNull()), subject.stream().collect(CollectionUtil.singleOrNull()));
            vSupport.fireVetoableChange(PROP_SUBJECTS, old, subject);
        } catch (PropertyVetoException ex) {
            this.subject = old;
        }
    }

    public Signee getSignee() {
        return signee;
    }

    public void setSignee(Signee signee) {
        Signee old = this.signee;
        this.signee = signee;
        try {
            vSupport.fireVetoableChange(PROP_SIGNEE, old, signee);
        } catch (PropertyVetoException ex) {
            this.signee = old;
        }
    }

    public String getSourceSigneeName() {
        return sourceSignee;
    }

    public abstract String getUnitDisplayName();

    public boolean importUnitDisplayName() {
        return false;
    }

    public abstract boolean isUnitIdGenerated();

    public abstract boolean existsUnitInSystem();

    public <F extends UpdaterFilter> F getFilter(String name, Supplier<F> create) {
        synchronized (filterChain) {
            return (F) filterChain.computeIfAbsent(name, n -> create.get());
        }
    }

    public List<UpdaterFilter> getFilters() {
        synchronized (filterChain) {
            return filterChain.values().stream()
                    .collect(Collectors.toList());
        }
    }

    public class GradeEntry {

        protected final StudentId student;
        protected final TermId term;
        protected Grade grade;
        protected Timestamp timestamp;

        protected GradeEntry(StudentId student, TermId term) {
            this.student = student;
            this.term = term;
        }

        protected void set(Grade grade, Timestamp ts) {
            this.grade = grade;
            this.timestamp = ts;
        }

        public StudentId getStudent() {
            return student;
        }

        public Grade getGrade() {
            return grade;
        }

        public TermId getTerm() {
            return term;
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
            hash = 53 * hash + Objects.hashCode(this.student);
            return 53 * hash + Objects.hashCode(this.term);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final GradeEntry other = (GradeEntry) obj;
            if (!Objects.equals(this.student, other.student)) {
                return false;
            }
            return Objects.equals(this.term, other.term);
        }

    }
}
