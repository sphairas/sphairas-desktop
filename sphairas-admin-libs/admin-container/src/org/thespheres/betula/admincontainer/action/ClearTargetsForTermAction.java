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
import org.thespheres.betula.admincontainer.action.ClearTargetsForTermVisualPanel.ClearTargetsForTermPanel;
import org.thespheres.betula.admin.units.AbstractUnitOpenSupport;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.model.DocumentsModel;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportWizard;

@ActionID(
        category = "Betula",
        id = "org.thespheres.betula.admin.container.action.ClearTargetsForTermAction")
@ActionRegistration(
        displayName = "#ClearTargetsForTermAction.action.name",
        lazy = false,
        surviveFocusChange = true,
        asynchronous = true)
@ActionReferences({
    @ActionReference(path = "Loaders/application/betula-remote-target-assessment-document/Actions", position = 2300)})
@NbBundle.Messages({
    "ClearTargetsForTermAction.action.name=Alle Einträge in {0} und Unterzeichner löschen",
    "ClearTargetsForTermAction.action.disabledName=Alle Einträge im aktuellen Halbjahr und Unterzeichner löschen"})
public final class ClearTargetsForTermAction extends AbstractRemoteTargetsAction {

    public ClearTargetsForTermAction() {
    }

    private ClearTargetsForTermAction(Lookup context) {
        super(context, false);
        updateEnabled();
    }

    @Override
    protected AbstractRemoteTargetsAction createAbstractRemoteTargetsAction(Lookup context) {
        return new ClearTargetsForTermAction(context);
    }

    @Override
    public String getName() {
        String name;
        try {
            term = findCommonTerm();
            name = NbBundle.getMessage(ClearTargetsForTermAction.class, "ClearTargetsForTermAction.action.name", term.getDisplayName());
        } catch (IOException ex) {
            setEnabled(false);
            name = NbBundle.getMessage(ClearTargetsForTermAction.class, "ClearTargetsForTermAction.action.disabledName");
        }
        return name;
    }

    @Override
    public void actionPerformed(final List<RemoteTargetAssessmentDocument> l, Optional<AbstractUnitOpenSupport> support) {
        FoldHandle fold;
        try {
            fold = AbstractFileImportAction.messageActionStart(getName());
        } catch (IOException ex) {
            Logger.getLogger(ClearTargetsForTermAction.class.getCanonicalName()).severe(ex.getLocalizedMessage());
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
            Map<DocumentId, List<RemoteTargetAssessmentDocument>> mapped = l.stream()
                    .collect(Collectors.groupingBy(t -> model.convert(t.getDocumentId())));
            //group selected targets
            final WebServiceProvider wsp = uos.findWebServiceProvider();
            final NamingResolver nr = uos.findNamingResolver();

            final Set<ClearTargetsForTermImportTargetsItem> items = mapped.entrySet().stream()
                    .map(entry -> ClearTargetsForTermImportTargetsItem.rtadToTargetDoc(entry.getKey(), entry.getValue(), tm, model, nr, tm))
                    .collect(Collectors.toSet());

            final ClearTargetsForTermEdit edit = new ClearTargetsForTermEdit(uos, items, wsp, tm);

            class ImportActionWizardIterator extends AbstractFileImportWizard<ClearTargetsForTermEdit> {

                @Override
                protected List<WizardDescriptor.Panel<ClearTargetsForTermEdit>> createPanels() {
                    return Collections.singletonList(new ClearTargetsForTermPanel());
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
        }
    }

}
