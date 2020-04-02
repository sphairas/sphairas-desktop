package org.thespheres.betula.validation.impl;

import java.util.Objects;
import java.util.Properties;
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

    public void runOneDocument(D document, StudentId filter) {
        fireStart(1);
        processOneDocument(document, filter);
        fireStop();
    }

    protected void processOneDocument(final D rtad) {
        processOneDocument(rtad, null);
    }

    protected void processOneDocument(final D rtad, final StudentId filter) {
        model.getTerms().stream().forEach(t -> {
            //TODO: use config, if set, to find maximum number of identities to skip
            final TermId before = findPrecedingTerm(t);
            model.getStudents().stream()
                    .filter(rs -> filter == null || filter.equals(rs.getStudentId()))
                    .forEach(rs -> {
                        final Key k = new Key(rs.getStudentId(), rtad.getDocumentId(), t);
                        final Grade g = rtad.select(rs.getStudentId(), t);
                        R r = null;
                        if (g != null) {
                            Grade b = rtad.select(rs.getStudentId(), before);
                            if (b != null) {
                                r = evaluate(rs, t, rtad, b, g);
                                if (r != null) {
                                    setResult(k, r);
                                }
                            }
                        }
                        if (r == null) {
                            removeResults(k);
                        }
                    });
        });
    }

    protected R evaluate(S student, TermId term, D doc, Grade before, Grade current) {
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

    protected TermId findPrecedingTerm(TermId t) {
        if (t.getId() != 0) {
            return new TermId(t.getAuthority(), t.getId() - 1);
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
