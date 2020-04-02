/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admincontainer.action;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.admin.units.RemoteSignee.DocumentInfo;
import org.thespheres.betula.admincontainer.util.ActionImportTargetsItem;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.xmlimport.utilities.TargetDocumentProperties;

/**
 *
 * @author boris.heithecker
 */
class ResetSigneeForTargetImportTargetsItem extends ActionImportTargetsItem {

    private final TargetDocumentProperties[] importTargets;

    private ResetSigneeForTargetImportTargetsItem(final String sourceNode, final DocumentId base, final boolean fragment, final TargetDocumentProperties[] tdp) {
        super(sourceNode, base, fragment);
        this.importTargets = tdp;
    }

    static ResetSigneeForTargetImportTargetsItem documentsToTargetDoc(final DocumentId base, final List<DocumentInfo> l, final NamingResolver nr) {
        String label;
        try {
            label = nr.resolveDisplayName(base);
        } catch (IllegalAuthorityException ex) {
            label = base.toString();
        }
        final Map<DocumentId, String[]> m = l.stream()
                .collect(Collectors.toMap(DocumentInfo::getDocument, DocumentInfo::getEntitlement));
        class RTADTD extends TargetDocumentProperties {

            @SuppressWarnings({"OverridableMethodCallInConstructor"})
            public RTADTD(final DocumentId id, final String[] signeeTypes) {
                super(id, null, null, null, null);
                Arrays.stream(signeeTypes)
                        .forEach(t -> getSignees().put(t, null));
            }

            @Override
            public Grade getDefaultGrade() {
                return null;
            }

            @Override
            public boolean isFragment() {
                return true;
            }
        }
        final TargetDocumentProperties[] tdp = m.entrySet().stream()
                .map(d -> new RTADTD(d.getKey(), d.getValue()))
                .toArray(TargetDocumentProperties[]::new);
        return new ResetSigneeForTargetImportTargetsItem(label, base, true, tdp);
    }

    @Override
    public TargetDocumentProperties[] getImportTargets() {
        return importTargets;
    }

    @Override
    public UnitId getUnitId() {
        return null;
    }

    @Override
    public Marker[] allMarkers() {
        return null;
    }

}
