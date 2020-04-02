/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document.model;

import java.util.List;
import java.util.Set;
import org.thespheres.betula.Student;
import org.thespheres.betula.TermId;
import org.thespheres.betula.assess.GradeTermTargetAssessment;
import org.thespheres.betula.assess.TargetDocument;
import org.thespheres.betula.document.DocumentId;

/**
 *
 * @author boris.heithecker
 * @param <S>
 * @param <D>
 */
//Key: primaryUnit
public interface UnitsModel<S extends Student, D extends UnitsModel.UnitsModelDocument> {

    public List<S> getStudents();

    public D getTarget(final DocumentId did);

    public Set<D> getTargets();

    public Set<TermId> getTerms();

    public static interface UnitsModelDocument extends TargetDocument, GradeTermTargetAssessment {

        public DocumentId getDocumentId();
    }
}
