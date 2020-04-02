/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admincontainer.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.openide.util.NbBundle;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.admincontainer.util.ActionImportTargetsItem;
import org.thespheres.betula.admin.units.AbstractUnitOpenSupport;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.document.model.DocumentsModel;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.utilities.TargetDocumentProperties;
import org.thespheres.betula.xmlimport.utilities.TargetItemsUpdater;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"SigneeUpdater.message.start=Unterzeichner {0} setzen..."})
class SigneeUpdateEdit extends AbstractUndoableEdit {

    private final String wsp;
    private final DocumentsModel model;
    private final RemoteTargetAssessmentDocument[] docs;
    private final String entitlement;
    private final Signee before;
    private final Signee signee;

    private SigneeUpdateEdit(AbstractUnitOpenSupport support, Collection<RemoteTargetAssessmentDocument> docs, String entitlement, Signee old, Signee signee) throws IOException {
        this.docs = docs.stream()
                .toArray(RemoteTargetAssessmentDocument[]::new);
        this.wsp = support.findWebServiceProvider().getInfo().getURL();
        this.model = support.findDocumentsModel();
        this.entitlement = entitlement;
        this.before = old;
        this.signee = signee.equals(Signee.NULL) ? null : signee;
    }

    static SigneeUpdateEdit createAndRun(AbstractUnitOpenSupport support, Collection<RemoteTargetAssessmentDocument> docs, String entitlement, Signee old, Signee signee) throws IOException {
        SigneeUpdateEdit ret = new SigneeUpdateEdit(support, docs, entitlement, old, signee);
        ret.run();
        return ret;
    }

    @Override
    public void redo() throws CannotRedoException {
        throw new UnsupportedOperationException("Must never be called.");
    }

    @Override
    public void undo() throws CannotUndoException {
        throw new UnsupportedOperationException("Must never be called.");
    }

    private void run() throws IOException {
        final WebServiceProvider p = WebProvider.find(wsp, WebServiceProvider.class);
        final List<RemoteTargetAssessmentDocument> dd = new ArrayList<>();
        dd.addAll(Arrays.asList(docs));
        notifyStart();
        processSelection(p, dd, entitlement, signee);
    }

    private void notifyStart() {
        ImportUtil.getIO().select();
        String msg = NbBundle.getMessage(ClearTargetsForTermAction.class, "SigneeUpdater.message.start", signee);
        ImportUtil.getIO().getOut().println(msg);
    }

    private void processSelection(final WebServiceProvider wsp, final Collection<RemoteTargetAssessmentDocument> l, String entit, Signee sig) throws IOException {
        Map<DocumentId, List<RemoteTargetAssessmentDocument>> mapped = l.stream()
                .collect(Collectors.groupingBy(t -> model.convert(t.getDocumentId())));
        //group selected targets

        ImportTargetsItem[] items = mapped.entrySet().stream()
                .map(entry -> rtadToTargetDoc(entry.getKey(), entry.getValue(), entit, sig))
                .toArray(ImportTargetsItem[]::new);

        final TargetItemsUpdater update = new TargetItemsUpdater(items, wsp, null, null);
        wsp.getDefaultRequestProcessor().post(update);
    }

    private ActionImportTargetsItem rtadToTargetDoc(final DocumentId base, final List<RemoteTargetAssessmentDocument> rtad, final String entitlement, final Signee signee) {

        class RTADTD extends TargetDocumentProperties {

            @SuppressWarnings({"OverridableMethodCallInConstructor"})
            public RTADTD(DocumentId id) {
                super(id, null, null, null, null);
                getSignees().put(entitlement, signee);
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
        return new ActionImportTargetsItem(base.toString(), base, true) {
            @Override
            public TargetDocumentProperties[] getImportTargets() {
                return rtad.stream()
                        .map(t -> new RTADTD(t.getDocumentId()))
                        .toArray(TargetDocumentProperties[]::new);
            }

            @Override
            public UnitId getUnitId() {
                return model.convertToUnitId(document);
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
            public Marker[] allMarkers() {
                return null;
            }

        };
    }
}
