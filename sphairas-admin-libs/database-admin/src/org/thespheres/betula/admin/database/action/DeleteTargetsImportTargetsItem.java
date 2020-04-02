/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.database.action;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.UnitId;
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
class DeleteTargetsImportTargetsItem extends AbstractImportTargetsItem {

    private final Set<DocumentId> rtad;
    private final DocumentsModel model;
    private final String info;

    private DeleteTargetsImportTargetsItem(String sourceNode, DocumentId id, boolean fragment, final Set<DocumentId> rtad, final DocumentsModel model, final String message) {
        super(sourceNode, id, fragment);
        this.rtad = rtad;
        this.model = model;
        this.info = message;
    }

    static DeleteTargetsImportTargetsItem rtadToTargetDoc(final DocumentId base, final List<RemoteTargetAssessmentDocument> rtad, final Term tm, final DocumentsModel model, final NamingResolver nr, final Grade pending) {
        String label;
        try {
            label = nr.resolveDisplayNameResult(base).getResolvedName(tm);
        } catch (IllegalAuthorityException ex) {
            label = base.toString();
        }
        final Set<StudentId> s = rtad.stream()
                .flatMap(t -> t.students().stream())
                .collect(Collectors.toSet());
        final Set<TermId> i = rtad.stream()
                .flatMap(t -> t.identities().stream())
                .collect(Collectors.toSet());
        long[] count = new long[]{0l};
        s.forEach(sid -> {
            i.forEach(tid -> {
                final long tc = rtad.stream()
                        .map(t -> t.select(sid, tid))
                        .filter(g -> pending == null || !pending.equals(g))
                        .count();
                count[0] += tc;
            });
        });
        final String message = createMessage(s, i, count[0]);
        final Set<DocumentId> docs = rtad.stream()
                .map(d -> d.getDocumentId())
                .collect(Collectors.toSet());
        return new DeleteTargetsImportTargetsItem(label, base, true, docs, model, message);
    }

    @Messages({"DeleteTargetsImportTargetsItem.message={0,choice,0#Kein Eintrag|1#Ein Eintrag|1<{0} Einträge} für {1,choice,0#keinen Schüler/keine Schülerin|1#eine Schülerin/einen Schüler|1<{1} Schülerinnen und Schüler} aus {2,choice,0#keinem Halbjahr|1#einem Halbjahr|1<{2} Halbjahren}"})
    private static String createMessage(final Set<StudentId> s, final Set<TermId> i, final long l) {
       return NbBundle.getMessage(DeleteTargetsImportTargetsItem.class, "DeleteTargetsImportTargetsItem.message", l, s.size(), i.size());
    }

    public String getInfo() {
        return info;
    }

    @Override
    public TargetDocumentProperties[] getImportTargets() {
        class RTADTD extends TargetDocumentProperties {

            @SuppressWarnings({"OverridableMethodCallInConstructor"})
            public RTADTD(DocumentId id) {
                super(id, null, null, null, null);
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

    @Override
    public UnitId getUnitId() {
        return model.convertToUnitId(document);
    }

    @Override
    public Marker[] allMarkers() {
        return null;
    }

}
