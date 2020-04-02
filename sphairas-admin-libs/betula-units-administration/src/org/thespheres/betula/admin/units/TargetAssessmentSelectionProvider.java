/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units;

import org.thespheres.betula.TermId;
import org.thespheres.betula.services.scheme.spi.Term;

public class TargetAssessmentSelectionProvider extends SelectionProvider {

    private final TermId term;
    private final String targetType;

    public TargetAssessmentSelectionProvider(RemoteTargetAssessmentDocument rtad, RemoteStudent rs, AbstractUnitOpenSupport uos, Term term, String targetType) {
        super();
        this.term = term != null ? term.getScheduledItemId() : null;
        this.targetType = targetType;
        if (rtad != null) {
            ic.add(rtad);
        }
        if (rs != null) {
            ic.add(rs);
        }
        if (uos != null) {
            ic.add(uos);
        }
        if (term != null) {
            ic.add(term);
        }
    }

    public TermId getTerm() {
        return term;
    }

    public String getTargetType() {
        return targetType;
    }

}
