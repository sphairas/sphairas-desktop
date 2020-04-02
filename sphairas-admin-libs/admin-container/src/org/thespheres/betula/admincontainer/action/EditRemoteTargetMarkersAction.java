/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admincontainer.action;

import org.thespheres.betula.admin.units.AbstractRemoteTargetsAction;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.openide.*;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.FoldHandle;
import org.thespheres.betula.admin.units.AbstractUnitOpenSupport;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.admincontainer.action.EditRemoteTargetMarkersVisualPanel.EditRemoteTargetMarkersPanel;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.model.DocumentsModel;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportWizard;

@ActionID(category = "Betula",
        id = "org.thespheres.betula.admin.container.action.EditRemoteTargetMarkersAction")
@ActionRegistration(
        displayName = "#EditRemoteTargetMarkersAction.action.name",
        lazy = false,
        surviveFocusChange = true,
        asynchronous = true)
@ActionReferences({
    @ActionReference(path = "Loaders/application/betula-remote-target-assessment-document/Actions", position = 10000)})
@NbBundle.Messages({
    "EditRemoteTargetMarkersAction.action.name=Markierungen bearbeiten"})
public final class EditRemoteTargetMarkersAction extends AbstractRemoteTargetsAction {

    public EditRemoteTargetMarkersAction() {
    }

    private EditRemoteTargetMarkersAction(Lookup context) {
        super(context, false);
        updateEnabled();
    }

    @Override
    protected AbstractRemoteTargetsAction createAbstractRemoteTargetsAction(Lookup context) {
        return new EditRemoteTargetMarkersAction(context);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(EditRemoteTargetMarkersAction.class, "EditRemoteTargetMarkersAction.action.name");
    }

    @Override
    public void actionPerformed(final List<RemoteTargetAssessmentDocument> l, Optional<AbstractUnitOpenSupport> support) {
        FoldHandle fold;
        try {
            fold = AbstractFileImportAction.messageActionStart(getName());
        } catch (IOException ex) {
            Logger.getLogger(EditRemoteTargetMarkersAction.class.getCanonicalName()).severe(ex.getLocalizedMessage());
            return;
        }
        if (support.isPresent()) {
            processSelection(support.get(), l, term, getName());
        }
        fold.silentFinish();
    }

    private static void processSelection(final AbstractUnitOpenSupport uos, final List<RemoteTargetAssessmentDocument> l, final Term tm, final String actionName) {
        try {
            final DocumentsModel model = uos.findDocumentsModel();
            final Map<DocumentId, List<RemoteTargetAssessmentDocument>> mapped = l.stream()
                    .collect(Collectors.groupingBy(t -> model.convert(t.getDocumentId())));
            //group selected targets
            final WebServiceProvider wsp = uos.findWebServiceProvider();
            final NamingResolver nr = uos.findNamingResolver();

            final List<EditRemoteTargetMarkersImportTargetsItem> items = mapped.entrySet().stream()
                    .flatMap(entry -> EditRemoteTargetMarkersImportTargetsItem.rtadToTargetDoc(entry.getKey(), entry.getValue(), tm, model, nr, tm).stream())
                    .collect(Collectors.toList());

            final EditRemoteTargetMarkersEdit edit = new EditRemoteTargetMarkersEdit(uos, items, wsp);

            class ImportActionWizardIterator extends AbstractFileImportWizard<EditRemoteTargetMarkersEdit> {

                @Override
                protected List<WizardDescriptor.Panel<EditRemoteTargetMarkersEdit>> createPanels() {
                    return Collections.singletonList(new EditRemoteTargetMarkersPanel());
                }

            }
            final WizardDescriptor wd = new WizardDescriptor(new ImportActionWizardIterator(), edit);
//            d.putProperty(AbstractFileImportAction.DATA, xml);
//            d.putProperty(AbstractFileImportAction.SELECTED_NODES, new ChangeSet<>(new HashSet<>()));
//            d.putProperty(AbstractFileImportAction.CLONED_NODES, new HashMap<>());
            // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
            // {1} will be replaced by WizardDescriptor.Iterator.name()
//            wd.setTitleFormat(new MessageFormat("{0} ({1})"));
            wd.setTitle(actionName);
            if (DialogDisplayer.getDefault().notify(wd) == WizardDescriptor.FINISH_OPTION) {
                edit.runAction();
            }
        } catch (IOException ex) {
            ex.printStackTrace(ImportUtil.getIO().getErr());
        }
    }

}
