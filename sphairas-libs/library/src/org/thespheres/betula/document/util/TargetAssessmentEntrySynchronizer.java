/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document.util;

import java.util.function.BiConsumer;
import org.thespheres.betula.TermId;

/**
 *
 * @author boris.heithecker
 */
public class TargetAssessmentEntrySynchronizer extends TargetAssessmentSynchronizer<TermId, TargetAssessmentEntry<TermId>> {

    public TargetAssessmentEntrySynchronizer() {
    }

    public TargetAssessmentEntrySynchronizer(BiConsumer<LogKey<TermId>, LogValue> consumer) {
        super(consumer);
    }

    @Override
    protected void updateLocal(TargetAssessmentEntry<TermId> remote, TargetAssessmentEntry<TermId> local) {
        super.updateLocal(remote, local);
        local.setAction(remote.getAction());
        local.setPreferredConvention(remote.getPreferredConvention());
        local.setTargetType(remote.getTargetType());
        local.setDocumentValidity(remote.getDocumentValidity());
    }

}
