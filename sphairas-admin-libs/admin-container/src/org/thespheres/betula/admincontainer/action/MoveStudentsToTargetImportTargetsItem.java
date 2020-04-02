/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admincontainer.action;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;
import org.thespheres.betula.admincontainer.util.ActionImportTargetsItem;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.model.DocumentsModel;
import org.thespheres.betula.document.model.ModelConfigurationException;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.util.UnitInfo;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.utilities.TargetDocumentProperties;

/**
 *
 * @author boris.heithecker
 */
class MoveStudentsToTargetImportTargetsItem extends ActionImportTargetsItem {

    enum Type {

        SOURCE, TARGET
    }
    private final Type action;
    private final Set<DocumentId> rtad;
    private final DocumentsModel model;
    private final ConfigurableImportTarget importTarget;
    private final Term term;
    private final Map<StudentId, StudentAction> actions = new HashMap<>();
    private UpdateUnitType updateUnitType;
    private String unitDisplayName;
    private StudentId[] updateStudents;

    private MoveStudentsToTargetImportTargetsItem(Type type, String sourceNode, DocumentId id, Set<DocumentId> rtad, DocumentsModel model, ConfigurableImportTarget it, Term term) {
        super(sourceNode, id, true);
        this.action = type;
        this.rtad = rtad;
        this.model = model;
        this.importTarget = it;
        this.term = term;
    }

    private static MoveStudentsToTargetImportTargetsItem baseCreate(Type t, ConfigurableImportTarget target, DocumentId base, Term term1, Set<RemoteTargetAssessmentDocument> rtad1, DocumentsModel model1) {
        String lbl;
        try {
            lbl = target.getNamingResolver().resolveDisplayNameResult(base).getResolvedName(term1);
        } catch (IllegalAuthorityException ex) {
            lbl = base.toString();
        }
        Set<DocumentId> docs = rtad1.stream()
                .map(RemoteTargetAssessmentDocument::getDocumentId)
                .collect(Collectors.toSet());
        MoveStudentsToTargetImportTargetsItem item = new MoveStudentsToTargetImportTargetsItem(t, lbl, base, docs, model1, target, term1);
        return item;
    }

    //addSetAware: if true, studs will only be added if already contained in RemoteTargetAssessmentDocument.students() set.
    static MoveStudentsToTargetImportTargetsItem createForAdd(DocumentId base, Set<RemoteTargetAssessmentDocument> rtad, final Set<StudentId> studs, DocumentsModel model, ConfigurableImportTarget target, Term term) {
        MoveStudentsToTargetImportTargetsItem item = baseCreate(Type.TARGET, target, base, term, rtad, model);
//        final TermId tid = term.getScheduledItemId();
        rtad.stream()
                .forEach(t -> {
                    Grade dGrade = item.importTarget.getDefaultValue(t.getDocumentId(), t);
                    studs.stream()
                            //                            .filter(sid -> !addSetAware || t.students().contains(sid))
                            .forEach(sid -> {
                                final Grade old = t.select(sid, term.getScheduledItemId());
//                        if (old != null) {
                                item.actions.computeIfAbsent(sid, s -> item.createStudentAction(s))
                                        .addUpdate(t.getDocumentId(), term, old, dGrade);
//                        }
                            });
                });
        UnitId unitId = item.getUnitId();
        if (unitId != null && item.importTarget.getUnits().hasUnit(unitId)) {
            try {
                UnitInfo ui = item.importTarget.getUnits().fetchParticipants(item.getUnitId(), null);
                StudentId[] nstuds = Stream.concat(Arrays.stream(ui.getStudents()), studs.stream())
                        .toArray(StudentId[]::new);
                item.students = nstuds;
            } catch (IOException ex) {
                ex.printStackTrace(ImportUtil.getIO().getErr());
            }
        }
        item.updateStudents = studs.stream().toArray(StudentId[]::new);
        return item;
    }

    static Set<MoveStudentsToTargetImportTargetsItem> createForRemove(Set<RemoteTargetAssessmentDocument> rs, final Set<StudentId> studs, DocumentsModel model, ConfigurableImportTarget target, Term term) {
        final Map<DocumentId, Set<RemoteTargetAssessmentDocument>> m = rs.stream()
                .collect(Collectors.groupingBy(d -> model.convert(d.getDocumentId()), Collectors.toSet()));
        return m.entrySet().stream()
                .map(e -> {
                    final DocumentId base = e.getKey();
                    final Set<RemoteTargetAssessmentDocument> rtad = e.getValue();
                    final MoveStudentsToTargetImportTargetsItem item = baseCreate(Type.SOURCE, target, base, term, rtad, model);
                    rtad.stream()
                            .forEach(t -> {
                                t.students().stream()
                                        .filter(studs::contains)
                                        .forEach(sid -> {
                                            final Grade old = t.select(sid, term.getScheduledItemId());
                                            if (old != null) {
                                                item.actions.computeIfAbsent(sid, s -> item.createStudentAction(s))
                                                        .addUpdate(t.getDocumentId(), term, old, null);
                                            }
                                        });
                            });
                    UnitId unitId = item.getUnitId();
                    if (unitId != null && item.importTarget.getUnits().hasUnit(unitId)) {
                        try {
                            UnitInfo ui = item.importTarget.getUnits().fetchParticipants(item.getUnitId(), null);
                            StudentId[] nstuds = Arrays.stream(ui.getStudents())
                                    .filter(s -> !studs.contains(s))
                                    .toArray(StudentId[]::new);
                            item.students = nstuds;
                            item.updateStudents = Arrays.stream(ui.getStudents())
                                    .filter(studs::contains)
                                    .toArray(StudentId[]::new);
                        } catch (IOException ex) {
                            ex.printStackTrace(ImportUtil.getIO().getErr());
                        }
                    }
                    return item;
                })
                .collect(Collectors.toSet());
    }

    public Type getAction() {
        return action;
    }

    @Override
    public TargetDocumentProperties[] getImportTargets() {
        class RTADTD extends TargetDocumentProperties {

            public RTADTD(DocumentId id) {
                super(id, null, null, null, null);
            }

            @Override
            public Grade getDefaultGrade() {
                return null; //Don't use default grades!! set default grade individually
            }

            @Override
            public boolean isFragment() {
                return true;
            }
        }
        return rtad.stream().map(d -> new RTADTD(d)).toArray(TargetDocumentProperties[]::new);
    }

    private StudentAction createStudentAction(StudentId s) {
        return new StudentAction(s, this);
    }

    @Override
    public UnitId getUnitId() {
        try {
            return model.convertToUnitId(document);
        } catch (ModelConfigurationException e) {
            e.printStackTrace(ImportUtil.getIO().getErr());
            return null;
        }
    }

    @Override
    public String getUnitDisplayName() {
        final UnitId unitId = getUnitId();
        if (unitDisplayName == null && unitId != null) {
            try {
                unitDisplayName = importTarget.getNamingResolver().resolveDisplayNameResult(unitId).getResolvedName(term);
            } catch (IllegalAuthorityException ex) {
                unitDisplayName = unitId.getId();
            }
        }
        return unitDisplayName;
    }

    @Override
    public StudentId[] getUnitStudents() {
        if (getUpdateType().equals(UpdateUnitType.UPDATE) && students != null) {
            return students;
        }
        return new StudentId[0];
    }

    @Override
    public boolean fileUnitParticipants() {
        return updateUnitType.equals(UpdateUnitType.UPDATE);
    }

    UpdateUnitType getUpdateType() {
        if (updateUnitType == null) {
            updateUnitType = getProposedUpdateUnitType();
        }
        return updateUnitType;
    }

    UpdateUnitType getProposedUpdateUnitType() {
        final UnitId uid = getUnitId();
        boolean update = uid != null && (uid.getAuthority().equals(document.getAuthority()) && getUnitId().getId().equals(document.getId()));
        return update ? UpdateUnitType.UPDATE : UpdateUnitType.KEEP;
    }

    void setUpdateUnitType(final UpdateUnitType clearType) {
        this.updateUnitType = clearType;
    }

    @Override
    public Marker[] allMarkers() {
        return null;
    }

    public int getNumUpdateStudents() {
        return updateStudents != null && getUpdateType().equals(UpdateUnitType.UPDATE) ? updateStudents.length : 0;
    }

    public String getMessage() {
        return "message"; //num students, log in ImportUtil.getIO
    }

}
