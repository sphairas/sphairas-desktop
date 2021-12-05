/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admincontainer.configui;

import java.util.List;
import java.util.stream.Collectors;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.admincontainer.util.ActionImportTargetsItem;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.model.DocumentsModel;
import org.thespheres.betula.xmlimport.utilities.TargetDocumentProperties;

/**
 *
 * @author boris.heithecker
 */
class EditRemoteTargetPreferredConventionImportTargetsItem extends ActionImportTargetsItem {

    private final DocumentId rtad;
    private final DocumentsModel model;
    final String original;
    private String update;

    private EditRemoteTargetPreferredConventionImportTargetsItem(final String sourceNode, final DocumentId base, final DocumentId document, DocumentsModel model, final String preferredConvention) {
        super(sourceNode, base, true);
        this.rtad = document;
        this.model = model;
        this.original = preferredConvention;
    }

    static List<EditRemoteTargetPreferredConventionImportTargetsItem> rtadToTargetDoc(final DocumentId base, final List<RemoteTargetAssessmentDocument> rtad, final DocumentsModel model) {
        return rtad.stream()
                .map(r -> new EditRemoteTargetPreferredConventionImportTargetsItem(base.toString(), base, r.getDocumentId(), model, r.getPreferredConvention()))
                .collect(Collectors.toList());
    }

    @Override
    public TargetDocumentProperties[] getImportTargets() {
        class RTADTD extends TargetDocumentProperties {

            @SuppressWarnings({"OverridableMethodCallInConstructor"})
            public RTADTD(DocumentId id) {
                super(id, null, null, false, EditRemoteTargetPreferredConventionImportTargetsItem.this.getPreferredConvention(), null);
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
        return new TargetDocumentProperties[]{new RTADTD(rtad)};
    }

    @Override
    public StudentId[] getUnitStudents() {
        return new StudentId[0];
    }

    @Override
    public boolean fileUnitParticipants() {
        return false;
    }

    @Override
    public UnitId getUnitId() {
        return model.convertToUnitId(rtad);
    }

    void setPreferredConvention(final String c) {
        update = c;
    }

    @Override
    public String getPreferredConvention() {
        return update;
    }

}
