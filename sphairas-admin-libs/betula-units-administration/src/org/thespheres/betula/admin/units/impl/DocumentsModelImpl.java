/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.impl;

import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.model.DocumentsModel;
import org.thespheres.betula.document.model.ModelConfigurationException;
import org.thespheres.betula.services.util.Units;

/**
 *
 * @author boris.heithecker
 */
@Messages("DocumentsModelImpl.convertToUnitId.noFallback=Could not find UnitId for DocumentId {0}.")
public class DocumentsModelImpl extends DocumentsModel {

    private final Units units;
    private final UnitId fallback;

    public DocumentsModelImpl(Units u, UnitId fallback) {
        this.units = u;
        this.fallback = fallback;
    }

    @Override
    public UnitId convertToUnitId(final DocumentId s) {
        UnitId ret = super.convertToUnitId(s);
        if (!units.hasUnit(ret)) {
            if (fallback != null) {
                return fallback;
            } else {
//                UnitDocumentBean ub;
//                MultiTargetAssessmentDocumentBean tb;
//                tb.findUnits(s);
//                if(res.length == 1) return ;
            }
            String msg = NbBundle.getMessage(DocumentsModelImpl.class, "DocumentsModelImpl.convertToUnitId.noFallback", s);
            throw new ModelConfigurationException(msg);
        }
        return ret;
    }

}
