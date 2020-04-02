/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admincontainer.action;

import java.util.List;
import java.util.stream.Collectors;
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
class EditRemoteTargetMarkersImportTargetsItem extends ActionImportTargetsItem {

    private final DocumentId rtad;
    private final DocumentsModel model;
    final Marker[] original;
    private Marker[] update;
    private Marker[] removeMarkers;

    private EditRemoteTargetMarkersImportTargetsItem(String sourceNode, DocumentId base, DocumentId document, DocumentsModel model, Marker[] markers) {
        super(sourceNode, base, true);
        this.rtad = document;
        this.model = model;
        this.original = markers;
    }

    static List<EditRemoteTargetMarkersImportTargetsItem> rtadToTargetDoc(final DocumentId base, final List<RemoteTargetAssessmentDocument> rtad, final Term tm, final DocumentsModel model, final NamingResolver nr, final Term term) {
        String label;
        try {
            label = nr.resolveDisplayNameResult(base).getResolvedName(tm);
        } catch (IllegalAuthorityException ex) {
            label = base.toString();
        }
//        Set<DocumentId> docs = rtad.stream()
//                .map(d -> d.getDocumentId())
//                .collect(Collectors.toSet());
//        final Set<Marker> commonMarkers = rtad.stream()
//                .flatMap(d -> Arrays.stream(d.markers()))
//                .collect(Collectors.toSet());
//        rtad.stream()
//                .map(d -> Arrays.asList(d.markers()))
//                .forEach(commonMarkers::retainAll);
        final String lbl = label;
        return rtad.stream()
                .map(r -> new EditRemoteTargetMarkersImportTargetsItem(lbl, base, r.getDocumentId(), model, r.markers()))
                .collect(Collectors.toList());

//        final TermId tid = tm.getScheduledItemId();
//        rtad.stream()
//                .forEach(t -> {
//                    t.students().stream()
//                            //                    .filter((StudentId s) -> t.select(s, tid) != null)
//                            //                    .forEach((StudentId s) -> item.submit(s, termId, null, null));
//                            .forEach(sid -> {
//                                Grade old = t.select(sid, tm.getScheduledItemId());
//                                if (old != null) {
//                                    item.actions.computeIfAbsent(sid, s -> item.createStudentAction(s))
//                                            .addUpdate(t.getDocumentId(), tm, old, null);
//                                }
//                            });
//                });
    }

    @Override
    public TargetDocumentProperties[] getImportTargets() {
        class RTADTD extends TargetDocumentProperties {

            @SuppressWarnings({"OverridableMethodCallInConstructor"})
            public RTADTD(DocumentId id) {
                super(id, allMarkers(), null, null, null);
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
//        return document.stream().map(d -> new RTADTD(d)).toArray(TargetDocumentProperties[]::new);
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

    void setAllMarkers(Marker[] m) {
        update = m;
    }

    //Unit markers
    @Override
    public Marker[] allMarkers() {
        return update;
    }

    public Marker[] getRemoveMarkers() {
        return removeMarkers;
    }

    public void setRemoveMarkers(Marker[] removeMarkers) {
        this.removeMarkers = removeMarkers;
    }

}
