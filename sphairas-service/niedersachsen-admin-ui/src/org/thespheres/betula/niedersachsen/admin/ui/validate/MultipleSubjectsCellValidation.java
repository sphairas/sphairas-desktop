/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.validate;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.Highlighter;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.thespheres.betula.TermId;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.admin.units.RemoteUnitsModel;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.ui.swingx.HighlighterInstanceFactory;
import org.thespheres.betula.validation.ValidationResultSet;
import org.thespheres.betula.validation.ui.RemoteTargetDocumentCellValidation;

/**
 *
 * @author boris.heithecker
 */
class MultipleSubjectsCellValidation extends RemoteTargetDocumentCellValidation<MultipleSubjectEntry> {

    MultipleSubjectsCellValidation(Lookup context) {
        super("org/thespheres/betula/niedersachsen/admin/ui/validate/exclamation-small.png", context);
    }

    @Override
    protected boolean isHighlighted(RemoteStudent student, TermId term, DocumentId document) {
        return getValidationResultSet() != null
                && getValidationResultSet().stream()
                        .anyMatch(msr -> document.equals(msr.getDocument())
                        && msr.getStudent().equals(student.getStudentId())
                        && msr.getTerm().equals(term));
    }

    @Override
    protected ValidationResultSet<RemoteUnitsModel, MultipleSubjectEntry> createValidation(RemoteUnitsModel model) {
        return new MultipleSubjectValidationImpl(support, model, null);
    }

    @MimeRegistration(mimeType = "application/betula-unit-context", service = HighlighterInstanceFactory.class)
    public static class HLFactory implements HighlighterInstanceFactory {

        @Override
        public Highlighter createHighlighter(JXTable table, TopComponent tc) {
            return new MultipleSubjectsCellValidation(tc.getLookup());
        }

    }
}
