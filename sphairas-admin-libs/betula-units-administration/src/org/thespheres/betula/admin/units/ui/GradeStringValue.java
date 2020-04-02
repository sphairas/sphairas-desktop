/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.ui;

import java.lang.ref.WeakReference;
import java.util.Optional;
import org.jdesktop.swingx.renderer.StringValue;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.TermId;
import org.thespheres.betula.admin.units.RemoteGradeEntry;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.scheme.PrecedingTermGradeReference;
import org.thespheres.betula.ui.util.UIExceptions;

/**
 *
 * @author boris.heithecker
 */
@Messages({"GradeStringValue.PrecedingTermGradeReference.shortLabel={0} ({1})"})
class GradeStringValue implements StringValue {

    private final WeakReference< RemoteTargetAssessmentDocument> document;

    GradeStringValue(RemoteTargetAssessmentDocument rtad) {
        this.document = new WeakReference<>(rtad);
    }

    @Override
    public String getString(Object v) {
        if (v != null) {
            Optional<RemoteGradeEntry> o;
            try {
                o = (Optional<RemoteGradeEntry>) v;
                Grade g = o.map(RemoteGradeEntry::getGrade).orElse(null);
                if (g instanceof PrecedingTermGradeReference) {
                    RemoteGradeEntry rge = o.get();
                    RemoteTargetAssessmentDocument rtad = document.get();
                    if (rtad != null) {
                        try {
                            TermId b = ((PrecedingTermGradeReference) g).findPrecedingTermId(rge.getTermId());
                            Grade referee = rtad.selectGradeAccess(rge.getStudent(), b).map(RemoteGradeEntry::getGrade).orElse(null);
                            String sl = shortLabel(referee);
                            return NbBundle.getMessage(GradeStringValue.class, "GradeStringValue.PrecedingTermGradeReference.shortLabel", sl, g.getShortLabel());
                        } catch (IllegalAuthorityException ex) {
                            UIExceptions.handle(ex, new Object[]{rtad.getDocumentId(), g});
                        }
                    }
                } else {
                    return shortLabel(g);
                }
            } catch (ClassCastException e) {
            }
        }
        return "null";
    }

    protected String shortLabel(Grade g) {
        return g == null ? "" : g.getShortLabel();
    }
}
