/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admincontainer.action;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.FoldHandle;
import org.thespheres.betula.admin.units.RemoteSignee;
import org.thespheres.betula.admin.units.RemoteSignee.DocumentInfo;
import org.thespheres.betula.admin.units.SigneesTopComponentModel;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.model.DocumentsModel;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.ui.util.ServiceUIUtilities;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.ui.util.MultiContextAction;
import org.thespheres.betula.ui.util.MultiContextSensitiveAction;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;

@ActionID(
        category = "Betula",
        id = "org.thespheres.betula.admin.container.action.ResetSigneeForTargetAction")
@ActionRegistration(
        displayName = "#ResetSigneeForTargetAction.action.name",
        lazy = false,
        surviveFocusChange = true,
        asynchronous = true)
@ActionReferences({
    @ActionReference(path = "Loaders/application/betula-signee-target-document/Actions", position = 2300)})
@NbBundle.Messages({
    "ResetSigneeForTargetAction.action.name=Unterzeichner löschen"})
public final class ResetSigneeForTargetAction extends MultiContextAction {

    public ResetSigneeForTargetAction() {
        super(RemoteSignee.class, SigneesTopComponentModel.class);
        multiTypes.add(DocumentInfo.class);
        putValue("iconBase", "org/thespheres/betula/classtest/resources/betulastud16.png");
    }

    @Override
    protected MultiContextSensitiveAction createMultiContextSensitiveAction() {
        return new ActionRunner();
    }

    static String getName() {
        return NbBundle.getMessage(ResetSigneeForTargetAction.class, "ResetSigneeForTargetAction.action.name");
    }

    static void actionPerformed(final List<DocumentInfo> l, final RemoteSignee signee, final SigneesTopComponentModel m) {
        FoldHandle fold;
        try {
            fold = AbstractFileImportAction.messageActionStart(getName());
        } catch (IOException ex) {
            Logger.getLogger(ResetSigneeForTargetAction.class.getCanonicalName()).severe(ex.getLocalizedMessage());
            return;
        }
        processSelection(l, signee, m);
        fold.silentFinish();
    }

    private static void processSelection(final List<DocumentInfo> l, final RemoteSignee signee, final SigneesTopComponentModel m) {
        final DocumentsModel model = ServiceUIUtilities.findDocumentsModelFromProvider(m.getProviderInfo());
        final Map<DocumentId, List<DocumentInfo>> mapped = l.stream()
                .collect(Collectors.groupingBy(d -> model.convert(d.getDocument())));
        //group selected targets
        final WebServiceProvider wsp = signee.findWebServiceProvider();
        final NamingResolver nr = NamingResolver.find(m.getSigneesProviderUrl());

        final Set<ResetSigneeForTargetImportTargetsItem> items = mapped.entrySet().stream()
                .map(entry -> ResetSigneeForTargetImportTargetsItem.documentsToTargetDoc(entry.getKey(), entry.getValue(), nr))
                .collect(Collectors.toSet());

        final ResetSigneeForTargetEdit edit = new ResetSigneeForTargetEdit(items, wsp, m);

//        class ImportActionWizardIterator extends AbstractFileImportWizard<ResetSigneeForTargetEdit> {
//
//            @Override
//            protected List<WizardDescriptor.Panel<ResetSigneeForTargetEdit>> createPanels() {
//                return Collections.singletonList(new ClearTargetsForTermPanel());
//            }
//
//        }
//        final WizardDescriptor wd = new WizardDescriptor(new ImportActionWizardIterator(), edit);
////            d.putProperty(AbstractFileImportAction.DATA, xml);
////            d.putProperty(AbstractFileImportAction.SELECTED_NODES, new ChangeSet<>(new HashSet<>()));
////            d.putProperty(AbstractFileImportAction.CLONED_NODES, new HashMap<>());
//        // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
//        // {1} will be replaced by WizardDescriptor.Iterator.name()
////            wd.setTitleFormat(new MessageFormat("{0} ({1})"));
//        wd.setTitle(getName());
//        if (DialogDisplayer.getDefault().notify(wd) == WizardDescriptor.FINISH_OPTION) {
        edit.runAction();
//        }
    }

    static class ActionRunner extends MultiContextSensitiveAction {

        @Override
        protected String getName(final boolean enabled) {
            return ResetSigneeForTargetAction.getName();
        }

        @Override
        protected void updateEnabled(boolean enabled, Lookup instances) {
            super.updateEnabled(enabled, instances);
            if (instances.lookupAll(RemoteSignee.class).stream()
                    .count() != 1l) {
                setEnabled(false);
            }
            if (instances.lookupAll(SigneesTopComponentModel.class).stream()
                    .count() != 1l) {
                setEnabled(false);
            }
        }

        @Override
        public void actionPerformed(final ActionEvent e, final Lookup context) {
            final SigneesTopComponentModel m = context.lookupAll(SigneesTopComponentModel.class).stream()
                    .map(SigneesTopComponentModel.class::cast)
                    .collect(CollectionUtil.requireSingleOrNull());
            final RemoteSignee signee = context.lookupAll(RemoteSignee.class).stream()
                    .map(RemoteSignee.class::cast)
                    .collect(CollectionUtil.requireSingleOrNull());
            final List<DocumentInfo> docs = context.lookupAll(DocumentInfo.class).stream()
                    .map(DocumentInfo.class::cast)
                    .collect(Collectors.toList());
            ResetSigneeForTargetAction.actionPerformed(docs, signee, m);
        }

    }
}
