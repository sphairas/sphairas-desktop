/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admincontainer.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.openide.util.NbBundle;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.admincontainer.util.ActionImportTargetsItem;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.model.DocumentsModel;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.xmlimport.utilities.TargetDocumentProperties;

/**
 *
 * @author boris.heithecker
 */
class ClearTargetsForTermImportTargetsItem extends ActionImportTargetsItem {

    private String unitDisplayName;
    private final Set<DocumentId> rtad;
    private final DocumentsModel model;
    private UpdateUnitType clearType;
    private ResetType resetType;
    private final NamingResolver namingResolver;
    private final Term term;
    private final Map<StudentId, StudentAction> actions = new HashMap<>();

    private ClearTargetsForTermImportTargetsItem(String sourceNode, DocumentId id, boolean fragment, Set<DocumentId> rtad, DocumentsModel model, NamingResolver nr, Term term, ResetType rt) {
        super(sourceNode, id, fragment);
        this.rtad = rtad;
        this.model = model;
        this.namingResolver = nr;
        this.term = term;
        this.resetType = rt;
    }

    @NbBundle.Messages(value = {"ClearTargetsForTermImportTargetsItem.ResetType.SIGNEES=Nur Unterzeichner zurücksetzen",
        "ClearTargetsForTermImportTargetsItem.ResetType.ENTRIES=Nur Einträge löschen",
        "ClearTargetsForTermImportTargetsItem.ResetType.BOTH=Einträge und Unterzeichner löschen"})
    enum ResetType {
        SIGNEES(NbBundle.getMessage(ResetType.class, "ClearTargetsForTermImportTargetsItem.ResetType.SIGNEES")),
        ENTRIES(NbBundle.getMessage(ResetType.class, "ClearTargetsForTermImportTargetsItem.ResetType.ENTRIES")),
        BOTH(NbBundle.getMessage(ResetType.class, "ClearTargetsForTermImportTargetsItem.ResetType.BOTH"));
        private final String displayName;

        private ResetType(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }

    }

    static ClearTargetsForTermImportTargetsItem rtadToTargetDoc(final DocumentId base, final List<RemoteTargetAssessmentDocument> rtad, final Term tm, final DocumentsModel model, final NamingResolver nr, final Term term) {
        String label;
        try {
            label = nr.resolveDisplayNameResult(base).getResolvedName(tm);
        } catch (IllegalAuthorityException ex) {
            label = base.toString();
        }
        final ResetType defaultType = ResetType.BOTH;
        final Set<DocumentId> docs = rtad.stream()
                .map(d -> d.getDocumentId())
                .collect(Collectors.toSet());
        final ClearTargetsForTermImportTargetsItem item = new ClearTargetsForTermImportTargetsItem(label, base, true, docs, model, nr, term, defaultType);
        if (!defaultType.equals(ResetType.SIGNEES)) {
            rtad.stream()
                    .forEach(t -> {
                        t.students().stream()
                                .forEach(sid -> {
                                    final Grade old = t.select(sid, tm.getScheduledItemId());
                                    if (old != null) {
                                        item.actions.computeIfAbsent(sid, item::createStudentAction)
                                                .addUpdate(t.getDocumentId(), tm, old, null);
                                    }
                                });
                    });
        }
        return item;
    }

    @Override
    public TargetDocumentProperties[] getImportTargets() {
        class RTADTD extends TargetDocumentProperties {

            @SuppressWarnings({"OverridableMethodCallInConstructor"})
            public RTADTD(DocumentId id) {
                super(id, null, null, null, null);
                if (!resetType.equals(ResetType.ENTRIES)) {
                    getSignees().put("entitled.signee", null);
                }
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
        return rtad.stream().map(d -> new RTADTD(d)).toArray(TargetDocumentProperties[]::new);
    }

    ResetType getResetType() {
        return resetType;
    }

    void setResetType(ResetType resetType) {
        this.resetType = resetType;
    }

    private StudentAction createStudentAction(StudentId s) {
        return new StudentAction(s, this);
    }

    @Override
    public String getUnitDisplayName() {
        if (unitDisplayName == null) {
            try {
                unitDisplayName = namingResolver.resolveDisplayNameResult(getUnitId()).getResolvedName(term);
            } catch (IllegalAuthorityException ex) {
                unitDisplayName = getUnitId().toString();
            }
        }
        return unitDisplayName;
    }

    @Override
    public StudentId[] getUnitStudents() {
        return new StudentId[0];
    }

    @Override
    public boolean fileUnitParticipants() {
        return clearType.equals(UpdateUnitType.UPDATE);
    }

    UpdateUnitType getClearType() {
        if (clearType == null) {
            clearType = getProposedClearType();
        }
        return clearType;
    }

    UpdateUnitType getProposedClearType() {
        boolean clear = getUnitId().getAuthority().equals(document.getAuthority()) && getUnitId().getId().equals(document.getId());
        return clear ? UpdateUnitType.UPDATE : UpdateUnitType.KEEP;
    }

    void setClearType(UpdateUnitType clearType) {
        this.clearType = clearType;
    }

    @Override
    public UnitId getUnitId() {
        return model.convertToUnitId(document);
    }

    @Override
    public Marker[] allMarkers() {
        return null;
    }

}
