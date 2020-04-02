/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis.ui.impl;

import java.awt.Toolkit;
import org.thespheres.betula.xmlimport.uiutil.AbstractCreateDocumentsAction;
import org.thespheres.betula.xmlimport.uiutil.CreateDocumentsComponent;
import java.util.Map;
import java.util.Set;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.thespheres.betula.gpuntis.ImportedLesson;
import org.thespheres.betula.gpuntis.UntisImportData;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;

/**
 *
 * @author boris.heithecker
 */
@ActionID(category = "Betula",
        id = "org.thespheres.betula.gpuntis.ui.impl.RemoveCloneAction")
@ActionRegistration(displayName = "#RemoveCloneAction.action.name",
        //        iconBase = "org/thespheres/betula/gpuntis/resources/blue-document-copy.png",
        lazy = false) //Must (!) be lazy to honour Presenter.Toolbar and connect button
@ActionReferences({
    @ActionReference(path = "UntisCreateDocumentsVisualPanel/Toolbars/Default", position = 120), //    @ActionReference(path = "Shortcuts", name = "D-A"),
//    @ActionReference(path = "Loadersa/pplication/betula-remote-target-assessment-document/Actions", position = 3000)
})
@NbBundle.Messages({"RemoveCloneAction.action.name=Doppel löschen"})
public final class RemoveCloneAction extends AbstractCreateDocumentsAction<ImportedLesson, UntisImportData> {

    public RemoveCloneAction() {
        super(NbBundle.getMessage(CreateCloneAction.class, "RemoveCloneAction.action.name"),
                ImageUtilities.loadImageIcon("org/thespheres/betula/gpuntis/resources/blue-document--minus.png", true));
    }

    @Override
    protected void performAction(final CreateDocumentsComponent<ImportedLesson, UntisImportData> panel) {
        final Set<ImportedLesson> selected = (Set<ImportedLesson>) panel.getSettings().getProperty(AbstractFileImportAction.SELECTED_NODES);
        final Map<ImportedLesson, Set<ImportedLesson>> clones = (Map<ImportedLesson, Set<ImportedLesson>>) panel.getSettings().getProperty(AbstractFileImportAction.CLONED_NODES);
        int[] sel = panel.getTable().getSelectedRows();
        if (sel.length == 1) {
            final int row = sel[0];
            int modelIndex = panel.getTable().convertRowIndexToModel(row);
            final ImportedLesson clone = (ImportedLesson) panel.getTableModel().getItemAt(modelIndex);
            int cid = clone.id();
            if (cid == 0) {
                Toolkit.getDefaultToolkit().beep();
            } else {
                ImportedLesson original = null;
                for (ImportedLesson s : selected) {
                    if (s.id() == 0 && s.getLesson().equals(clone.getLesson())) {
                        original = s;
                    }
                }
                if (original != null) {
                    selected.remove(clone);
                    clones.get(original).remove(clone);
                    panel.getTableModel().initialize(panel.getSettings());
                }
            }
        }
    }

}
