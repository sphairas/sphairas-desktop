/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.validate;

import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbBundle;
import org.thespheres.betula.Student;
import org.thespheres.betula.TermId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.model.MultiSubject;
import org.thespheres.betula.document.model.UnitsModel;
import org.thespheres.betula.validation.ValidationResultSet;
import org.thespheres.betula.validation.impl.AbstractValidationSet;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"MultipleSubjectValidation.emptyName=Mehrfacheinträge",
    "MultipleSubjectValidation.name=Mehrfacheinträge in {0}"})
public abstract class MultipleSubjectValidation<S extends Student, D extends UnitsModel.UnitsModelDocument, M extends UnitsModel<S, D>> extends AbstractValidationSet<M, MultipleSubjectEntry, DocumentId, Properties> implements ValidationResultSet<M, MultipleSubjectEntry> {

    protected MultipleSubjectValidation(M model, Properties config) {
        super(model, config);
    }

    @Override
    public String getDisplayName(String modelDisplayName) {
        if (StringUtils.isBlank(modelDisplayName)) {
            return NbBundle.getMessage(MultipleSubjectValidation.class, "MultipleSubjectValidation.emptyName");
        }
        return NbBundle.getMessage(MultipleSubjectValidation.class, "MultipleSubjectValidation.name", modelDisplayName);
    }

    @Override
    public void run() {
        final Set<D> coll = model.getTargets();
        fireStart(coll.size());
        coll.stream()
                .forEach(this::processOneDocument);
        fireStop();
    }

    protected void processOneDocument(final D rtad) {
        final MultiSubject rtadSubject = findSubject(rtad);
        if (rtadSubject == null) {
            removeResults(rtad.getDocumentId());
            return;
        }
        final Set<TermId> arr = model.getTerms();
        final List<S> students = model.getStudents();
        final Set<D> targets = model.getTargets();
        arr.stream().forEach(t -> {
            students.stream().forEach(rs -> {
                if (rtad.select(rs.getStudentId(), t) != null
                        && targets.stream()
                                .filter((D d) -> !d.getDocumentId().equals(rtad.getDocumentId()))
                                .filter((D d) -> Objects.equals(d.getTargetType(), rtad.getTargetType()))
                                .filter((D d) -> d.select(rs.getStudentId(), t) != null)
                                .anyMatch((D d) -> Objects.equals(rtadSubject, findSubject(d)))) {
                    addResult(rtad.getDocumentId(), createResult(rtad, rs, t));
                } else {
                    getResults(rtad.getDocumentId()).stream()
                            .filter(r -> rs.getStudentId().equals(r.getStudent()) && t.equals(r.getTerm()))
                            .forEach(r -> removeResult(rtad.getDocumentId(), r.id()));
                }
            });
        });
    }

    protected abstract MultiSubject findSubject(D rtad);

    protected MultipleSubjectEntry createResult(D target, S student, TermId term) {        
        return new MultipleSubjectEntry(target.getDocumentId(), student.getStudentId(), term);
    }

}
