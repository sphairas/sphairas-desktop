/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.database.action;

import org.thespheres.betula.admin.units.AbstractRemoteTargetsAction;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import org.thespheres.betula.admin.database.action.DeleteTargetsListVisualPanel.DeleteTargetsListPanel;
import org.thespheres.betula.admin.units.AbstractUnitOpenSupport;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.admincontainer.util.TargetsUtil;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.model.DocumentsModel;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportWizard;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;

@ActionID(
        category = "Betula",
        id = "org.thespheres.betula.admin.database.action.DeleteTargetsAction")
@ActionRegistration(
        displayName = "#DeleteTargetsAction.action.name",
        lazy = false,
        surviveFocusChange = true,
        asynchronous = true)
@ActionReferences({
    @ActionReference(path = "Loaders/application/betula-remote-target-assessment-document/Actions", position = 200000, separatorBefore = 100000)})
@NbBundle.Messages({
    "DeleteTargetsAction.action.name=Liste(n) vernichten"})
public final class DeleteTargetsAction extends AbstractRemoteTargetsAction {

    public DeleteTargetsAction() {
    }

    private DeleteTargetsAction(Lookup context) {
        super(context, false);
        updateEnabled();
    }

    @Override
    protected AbstractRemoteTargetsAction createAbstractRemoteTargetsAction(Lookup context) {
        return new DeleteTargetsAction(context);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(DeleteTargetsAction.class, "DeleteTargetsAction.action.name");
    }

    @Override
    public void actionPerformed(final List<RemoteTargetAssessmentDocument> l, Optional<AbstractUnitOpenSupport> support) {
        FoldHandle fold;
        try {
            fold = AbstractFileImportAction.messageActionStart(getName());
        } catch (IOException ex) {
            Logger.getLogger(DeleteTargetsAction.class.getCanonicalName()).severe(ex.getLocalizedMessage());
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
            final WebServiceProvider service = uos.findWebServiceProvider();
            final NamingResolver nr = uos.findNamingResolver();

            final ConfigurableImportTarget it = TargetsUtil.findCommonImportTarget(uos);
            final Grade pending = it == null ? null : it.getDefaultValue(null, null);
            final Set<DeleteTargetsImportTargetsItem> items = mapped.entrySet().stream()
                    .map(entry -> DeleteTargetsImportTargetsItem.rtadToTargetDoc(entry.getKey(), entry.getValue(), tm, model, nr, pending))
                    .collect(Collectors.toSet());

            final DeleteTargetsListVisualPanel.DeleteTargetsListDescriptor edit = new DeleteTargetsListVisualPanel.DeleteTargetsListDescriptor(items);
            class ImportActionWizardIterator extends AbstractFileImportWizard<DeleteTargetsListVisualPanel.DeleteTargetsListDescriptor> {

                @Override
                protected List<WizardDescriptor.Panel<DeleteTargetsListVisualPanel.DeleteTargetsListDescriptor>> createPanels() {
                    return Collections.singletonList(new DeleteTargetsListPanel());
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
                final DeleteTargetsUpdater update = new DeleteTargetsUpdater(items.stream().toArray(DeleteTargetsImportTargetsItem[]::new), service);
                service.getDefaultRequestProcessor().post(() -> {
                    update.run();
                });
            }
        } catch (IOException ex) {
            ex.printStackTrace(ImportUtil.getIO().getErr());
        }
    }

}
