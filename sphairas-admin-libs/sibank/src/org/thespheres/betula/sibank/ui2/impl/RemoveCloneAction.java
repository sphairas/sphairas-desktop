/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.sibank.ui2.impl;

import java.awt.Toolkit;
import java.util.Map;
import java.util.Set;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.thespheres.betula.sibank.SiBankImportData;
import org.thespheres.betula.sibank.SiBankKursItem;
import org.thespheres.betula.xmlimport.uiutil.AbstractCreateDocumentsAction;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import org.thespheres.betula.xmlimport.uiutil.CreateDocumentsComponent;

/**
 *
 * @author boris.heithecker
 */
@ActionID(category = "Betula",
        id = "org.thespheres.betula.sibank.ui2.impl.RemoveCloneAction")
@ActionRegistration(displayName = "#RemoveCloneAction.action.name",
        //        iconBase = "org/thespheres/betula/gpuntis/resources/blue-document-copy.png",
        lazy = false) //Must (!) be lazy to honour Presenter.Toolbar and connect button
@ActionReferences({
    @ActionReference(path = "SiBankCreateDocumentsVisualPanel/Toolbars/Default", position = 120), //    @ActionReference(path = "Shortcuts", name = "D-A"),
//    @ActionReference(path = "Loadersa/pplication/betula-remote-target-assessment-document/Actions", position = 3000)
})
@NbBundle.Messages({"RemoveCloneAction.action.name=Clone entfernen"})
public final class RemoveCloneAction extends AbstractCreateDocumentsAction<SiBankKursItem, SiBankImportData> {

    public RemoveCloneAction() {
        super(NbBundle.getMessage(RemoveCloneAction.class, "RemoveCloneAction.action.name"),
                ImageUtilities.loadImageIcon("org/thespheres/betula/gpuntis/resources/blue-document--minus.png", true));
    }

    @Override
    protected void performAction(CreateDocumentsComponent<SiBankKursItem, SiBankImportData> panel) {
        Set<SiBankKursItem> selected = (Set<SiBankKursItem>) panel.getSettings().getProperty(AbstractFileImportAction.SELECTED_NODES);
        final Map<SiBankKursItem, Set<SiBankKursItem>> clones = (Map<SiBankKursItem, Set<SiBankKursItem>>) panel.getSettings().getProperty(AbstractFileImportAction.CLONED_NODES);
//        SourceUserOverrides overrides = (SourceUserOverrides) panel.getWizard().getProperty(ImportAction.USER_SOURCE_OVERRIDES);
        int[] sel = panel.getTable().getSelectedRows();
        if (sel.length == 1) {
            final int row = sel[0];
            int modelIndex = panel.getTable().convertRowIndexToModel(row);
            final SiBankKursItem l = (SiBankKursItem) panel.getTableModel().getItemAt(modelIndex);
            if (l.id() == 0) {
                Toolkit.getDefaultToolkit().beep();
            } else {
                SiBankKursItem original = null;
                for (SiBankKursItem s : selected) {
                    if (s.id() == 0 && s.getDistinguisher().equals(l.getDistinguisher()) || s.getImportFile().equals(l.getImportFile())) {
                        original = s;
                    }
                }
                if (original != null) {
                    selected.remove(l);
                    clones.get(original).remove(l);
                    panel.getTableModel().initialize(panel.getSettings());
                }
            }
        }
    }

}
