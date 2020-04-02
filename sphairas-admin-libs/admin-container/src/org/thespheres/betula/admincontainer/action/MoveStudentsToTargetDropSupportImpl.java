/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admincontainer.action;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.FoldHandle;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.admincontainer.action.MoveStudentsToTargetVisualPanel.MoveStudentsToTargetPanel;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;
import org.thespheres.betula.admin.units.AbstractUnitOpenSupport;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.admin.units.RemoteUnitsModel;
import org.thespheres.betula.admin.units.TargetAssessmentSelectionProvider;
import org.thespheres.betula.admin.units.MoveStudentsToTargetDropSupport;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.UniqueMarkerSet;
import org.thespheres.betula.document.model.DocumentsModel;
import org.thespheres.betula.document.model.MarkerDecoration;
import org.thespheres.betula.services.NoProviderException;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.model.Product;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportWizard;

/**
 *
 * @author boris.heithecker
 */
@Messages({"MoveStudentsToTargetDropSupportImpl.name=Teilnehmer verschieben"})
@ServiceProvider(service = MoveStudentsToTargetDropSupport.class)
public class MoveStudentsToTargetDropSupportImpl extends MoveStudentsToTargetDropSupport {

    @Override
    public boolean moveStudentsToTarget(TargetAssessmentSelectionProvider sp, final RemoteStudent[] studs) {

        FoldHandle fold;
        try {
            fold = AbstractFileImportAction.messageActionStart(getName());
        } catch (IOException ex) {
            Logger.getLogger(MoveStudentsToTargetDropSupportImpl.class.getCanonicalName()).severe(ex.getLocalizedMessage());
            return false;
        }
        boolean ret = processSelection(sp, studs);
        fold.silentFinish();
        return ret;
    }

    private String getName() {
        return NbBundle.getMessage(MoveStudentsToTargetDropSupportImpl.class, "MoveStudentsToTargetDropSupportImpl.name");
    }

    private boolean processSelection(TargetAssessmentSelectionProvider sp, final RemoteStudent[] studs) {
        final AbstractUnitOpenSupport uos = sp.getLookup().lookup(AbstractUnitOpenSupport.class);
        final RemoteTargetAssessmentDocument rtad = sp.getLookup().lookup(RemoteTargetAssessmentDocument.class);
        final RemoteUnitsModel model;
        final DocumentsModel docModel;
        final MarkerDecoration mDeco;
        final ConfigurableImportTarget importTarget;
        try {
            model = uos.getRemoteUnitsModel();
            docModel = uos.findDocumentsModel();
            mDeco = uos.findMarkerDecoration();
            final String url = uos.findBetulaProjectProperties().getProperty("providerURL");
            importTarget = ConfigurableImportTarget.Factory.find(url, ConfigurableImportTarget.class, Product.NO);
        } catch (IOException | NoProviderException ex) {
            ex.printStackTrace(ImportUtil.getIO().getErr());
            return false;
        }
        final Term term = sp.getLookup().lookup(Term.class);
        final DocumentId doc = rtad.getDocumentId();
        final DocumentId base = docModel.convert(doc);
        final Set<RemoteTargetAssessmentDocument> targets = model.getTargets();
        final UniqueMarkerSet origSubject = mDeco.getDistinguishingDecoration(doc, rtad, "subject");
        final UniqueMarkerSet origRealm = mDeco.getDistinguishingDecoration(doc, rtad, "realm");
        final Set<StudentId> students = Arrays.stream(studs)
                .map(RemoteStudent::getStudentId)
                .collect(Collectors.toSet());
        final boolean filterForSubjectAndRealm = !origSubject.isEmpty(); //No subject, z.B. AG   //TODO: compare names
        final Set<RemoteTargetAssessmentDocument> deleteCandidates;
        synchronized (targets) {
            deleteCandidates = targets.stream()
                    .filter(d -> !docModel.convert(d.getDocumentId()).equals(base))
                    .filter(d -> filterForSubjectAndRealm
                    && (mDeco.getDistinguishingDecoration(d.getDocumentId(), d, "subject").equals(origSubject)
                    && mDeco.getDistinguishingDecoration(d.getDocumentId(), d, "realm").equals(origRealm)))
                    .collect(Collectors.toSet());
        }
        final Set<RemoteTargetAssessmentDocument> addCandidates;
        synchronized (targets) {
            addCandidates = targets.stream()
                    .filter(r -> docModel.convert(r.getDocumentId()).equals(base))
                    .collect(Collectors.toSet());
        }
//        final boolean isPrimaryUnit = uos instanceof PrimaryUnitOpenSupport;
        final MoveStudentsToTargetImportTargetsItem add = MoveStudentsToTargetImportTargetsItem.createForAdd(base, addCandidates, students, docModel, importTarget, term);
        final Set<MoveStudentsToTargetImportTargetsItem> remove = MoveStudentsToTargetImportTargetsItem.createForRemove(deleteCandidates, students, docModel, importTarget, term);
        final MoveStudentsToTargetEdit edit = new MoveStudentsToTargetEdit(uos, add, remove, importTarget, term);

        class ImportActionWizardIterator extends AbstractFileImportWizard<MoveStudentsToTargetEdit> {

            @Override
            protected List<WizardDescriptor.Panel<MoveStudentsToTargetEdit>> createPanels() {
                return Collections.singletonList(new MoveStudentsToTargetPanel());
            }

        }
        final WizardDescriptor wd = new WizardDescriptor(new ImportActionWizardIterator(), edit);
        wd.setTitle(getName());
        if (DialogDisplayer.getDefault().notify(wd) == WizardDescriptor.FINISH_OPTION) {
            edit.getSelectedNodesProperty().stream()
                    .forEach(i -> ImportUtil.getIO().getOut().println(i.getMessage()));
            edit.runAction();
        }
//        addCandidates.stream()
//                .forEach(d -> {
//                    String targetType = d.getTargetType();
//                    Grade g = importTarget.getDefaultValue(d.getDocumentId(), null);
//                    Arrays.stream(studs)
//                    .forEach(s -> {
//                        d.submit(s.getStudentId(), term, g, null);
////                        if (targetType.equalsIgnoreCase(NdsCommonConstants.SUFFIX_ZEUGNISNOTEN)) {
////                            d.submit(s.getStudentId(), term, g, null);
////                        } else if (targetType.equalsIgnoreCase(NdsCommonConstants.SUFFIX_AV)) {
////                            d.submit(s.getStudentId(), term, g, null);
////                        } else if (targetType.equalsIgnoreCase(NdsCommonConstants.SUFFIX_SV)) {
////                            d.submit(s.getStudentId(), term, g, null);
////                        }
//                    });
//                }
//                );
//        deleteCandidates.stream()
//                .forEach(d -> {
//                    Arrays.stream(studs)
//                    .forEach(s -> d.submit(s.getStudentId(), term, null, null));
//                }
//                );
        return true;
    }
}
