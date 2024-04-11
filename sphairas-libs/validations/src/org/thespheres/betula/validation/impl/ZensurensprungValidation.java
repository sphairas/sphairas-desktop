package org.thespheres.betula.validation.impl;

import java.util.Objects;
import java.util.Properties;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.Student;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.NumberValueGrade;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.model.UnitsModel;
import org.thespheres.betula.validation.ValidationResultSet;
import org.thespheres.betula.validation.impl.ZensurensprungValidation.Key;

/**
 *
 * @author boris.heithecker
 * @param <S>
 * @param <D>
 * @param <M>
 * @param <R>
 */
@Messages({"ZensurensprungValidation.emptyName=Notensprünge",
    "ZensurensprungValidation.name=Notensprünge in {0}"})
public abstract class ZensurensprungValidation<S extends Student, D extends UnitsModel.UnitsModelDocument, M extends UnitsModel<S, D>, R extends ZensurensprungResult> extends AbstractValidationSet<M, R, Key, Properties> implements ValidationResultSet<M, R> {

    public ZensurensprungValidation(M model, Properties config) {
        super(model, config);
    }

    @Override
    public String getDisplayName(String modelDisplayName) {
        if (StringUtils.isBlank(modelDisplayName)) {
            return NbBundle.getMessage(ZensurensprungValidation.class, "ZensurensprungValidation.emptyName");
        }
        return NbBundle.getMessage(ZensurensprungValidation.class, "ZensurensprungValidation.name", modelDisplayName);
    }

    @Override
    public void run() {
        fireStart(model.getTargets().size());
        model.getTargets().forEach(this::processOneDocument);
        fireStop();
    }

    public void runOneDocument(D document, StudentId studentFilter, TermId current) {
        fireStart(1);
        processOneDocument(document, studentFilter, current);
        fireStop();
    }

    protected void processOneDocument(final D rtad) {
        processOneDocument(rtad, null, null);
    }

    protected void processOneDocument(final D rtad, final StudentId studFilter, final TermId current) {
        model.getTerms().stream()
                .filter(rs -> current == null || current.equals(rs))
                .forEach(t -> {
                    //TODO: use config, if set, to find maximum number of identities to skip
                    final TermId[] before = findPrecedingTerms(t);
                    model.getStudents().stream()
                            .filter(rs -> studFilter == null || studFilter.equals(rs.getStudentId()))
                            .forEach(rs -> {
                                final Key k = new Key(rs.getStudentId(), rtad.getDocumentId(), t);
                                final Grade g = rtad.select(rs.getStudentId(), t);
                                R r = null;
                                if (g != null) {
                                    for (TermId tid : before) {
                                        Grade b = rtad.select(rs.getStudentId(), tid);
                                        if (isPrecedingCandidate(b)) {
                                            r = evaluate(rs, t, rtad, b, g);
                                            if (r != null) {
                                                setResult(k, r);
                                            }
                                            break;
                                        }
                                    }
                                }
                                if (r == null) {
                                    removeResults(k);
                                }
                            });
                });
    }

    protected boolean isPrecedingCandidate(Grade b) {
        return b instanceof NumberValueGrade;
    }

    protected R evaluate(S student, TermId term, D doc, Grade before, Grade current) {
        before = before instanceof Grade.Biasable ? ((Grade.Biasable) before).getUnbiased() : before;
        current = current instanceof Grade.Biasable ? ((Grade.Biasable) current).getUnbiased() : current;
        if (before instanceof NumberValueGrade && current instanceof NumberValueGrade) {
            NumberValueGrade nb = (NumberValueGrade) before;
            NumberValueGrade nc = (NumberValueGrade) current;
            if (Math.abs(nc.getNumberValue().doubleValue() - nb.getNumberValue().doubleValue()) >= 2d) {
                return createResult(student, term, doc, before, current);
            }
        }
        return null;
    }

    protected abstract R createResult(S student, TermId term, D doc, Grade before, Grade current);

    protected TermId[] findPrecedingTerms(TermId t) {
        if (t.getId() != 0) {
            int tid = t.getId();
            int subtract = (tid % 2 == 1) ? 2 : 3;
            return IntStream.rangeClosed(1, subtract)
                    .mapToObj(i -> new TermId(t.getAuthority(), t.getId() - i))
                    .toArray(TermId[]::new);
        }
        throw new IllegalArgumentException("Cannot find preceding TermId for: " + t.toString());
    }

    static final class Key {

        final StudentId student;
        final DocumentId document;
        final TermId term;

        Key(StudentId student, DocumentId document, TermId term) {
            this.student = student;
            this.document = document;
            this.term = term;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 53 * hash + Objects.hashCode(this.student);
            hash = 53 * hash + Objects.hashCode(this.document);
            hash = 53 * hash + Objects.hashCode(this.term);
            return hash;
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
            final Key other = (Key) obj;
            if (!Objects.equals(this.student, other.student)) {
                return false;
            }
            if (!Objects.equals(this.document, other.document)) {
                return false;
            }
            return Objects.equals(this.term, other.term);
        }

    }
}
