/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.validate;

import java.util.List;
import java.util.Objects;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.Highlighter;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.niedersachsen.admin.ui.RemoteReportsModel2;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.ui.swingx.HighlighterInstanceFactory;
import org.thespheres.betula.validation.ValidationResultSet;
import org.thespheres.betula.validation.impl.PolicyLegalHint;

/**
 *
 * @author boris.heithecker
 */
class VersetzungsHintsCellValidation extends RemoteReportsCellValidation<VersetzungsResultImpl> {

    VersetzungsHintsCellValidation(Lookup context) {
        super("org/thespheres/betula/niedersachsen/admin/ui/validate/exclamation.png", context);
    }

    @Override
    protected boolean isHighlighted(final RemoteStudent student, final Term term) {
        return getValidationResultSet() != null
                && (getValidationResultSet().stream()
                        .anyMatch(r -> evaluateHints(student, term, r)));
    }

    static boolean evaluateHints(final RemoteStudent student, final Term term, final VersetzungsResultImpl r) {
        final List<PolicyLegalHint> hints = r.getLegalHints();
        return hints != null && !hints.isEmpty() && r.getStudent().equals(student)
                && Objects.equals(term.getScheduledItemId(), r.getDocument().getTerm());
    }

    @Override
    protected ValidationResultSet<RemoteReportsModel2, VersetzungsResultImpl> createValidation(RemoteReportsModel2 model) {
        synchronized (VersetzungsValidationImpl.CACHE) {
            return VersetzungsValidationImpl.CACHE.computeIfAbsent(model.support, k -> VersetzungsValidationImpl.create(model));
        }
    }

    @MimeRegistration(mimeType = "application/betula-unit-nds-zeugnis-settings", service = HighlighterInstanceFactory.class)
    public static class HLFactory implements HighlighterInstanceFactory {

        @Override
        public Highlighter createHighlighter(JXTable table, TopComponent tc) {
            return new VersetzungsHintsCellValidation(tc.getLookup());
        }

    }
}
