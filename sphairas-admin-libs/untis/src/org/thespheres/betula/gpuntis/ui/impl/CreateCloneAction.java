/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis.ui.impl;

import org.thespheres.betula.xmlimport.uiutil.AbstractCreateDocumentsAction;
import org.thespheres.betula.xmlimport.uiutil.CreateDocumentsComponent;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingUtilities;
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
@ActionID(
        category = "Betula",
        id = "org.thespheres.betula.gpuntis.ui.impl.CreateCloneAction")
@ActionRegistration(
        displayName = "#CreateCloneAction.action.name",
        //        iconBase = "org/thespheres/betula/gpuntis/resources/blue-document-copy.png",
        lazy = false) //Must (!) be lazy to honour Presenter.Toolbar and connect button
@ActionReferences({
    @ActionReference(path = "UntisCreateDocumentsVisualPanel/Toolbars/Default", position = 100), //    @ActionReference(path = "Shortcuts", name = "D-A"),
//    @ActionReference(path = "Loadersa/pplication/betula-remote-target-assessment-document/Actions", position = 3000)
})
@NbBundle.Messages({"CreateCloneAction.action.name=Verdoppeln"})
public final class CreateCloneAction extends AbstractCreateDocumentsAction<ImportedLesson, UntisImportData> {

    public CreateCloneAction() {
        super(NbBundle.getMessage(CreateCloneAction.class, "CreateCloneAction.action.name"),
                ImageUtilities.loadImageIcon("org/thespheres/betula/gpuntis/resources/blue-document-copy.png", true));
    }

    @Override
    protected void performAction(CreateDocumentsComponent<ImportedLesson, UntisImportData> panel) {
        Set<ImportedLesson> selected = (Set<ImportedLesson>) panel.getSettings().getProperty(AbstractFileImportAction.SELECTED_NODES);
        final Map<ImportedLesson, Set<ImportedLesson>> clones = (Map<ImportedLesson, Set<ImportedLesson>>) panel.getSettings().getProperty(AbstractFileImportAction.CLONED_NODES);
//        SourceUserOverrides overrides = (SourceUserOverrides) panel.getWizard().getProperty(ImportAction.USER_SOURCE_OVERRIDES);
        int[] sel = panel.getTable().getSelectedRows();
        if (sel.length == 1) {
            final int row = sel[0];
            int modelIndex = panel.getTable().convertRowIndexToModel(row);
            final ImportedLesson l = (ImportedLesson) panel.getTableModel().getItemAt(modelIndex);
            int cid = l.id();
            ImportedLesson clone;
            while (selected.contains(clone = ImportedLesson.create(l.getLesson(), l.getGeneral(), ++cid))) {
            }
            if (selected.add(clone)) {
                clones.computeIfAbsent(l, (ImportedLesson ls) -> new HashSet()).add(clone);
                panel.getTableModel().initialize(panel.getSettings());
                for (int i = 0; i < panel.getTable().getRowCount(); i++) {
                    if (((ImportedLesson) panel.getTableModel().getItemAt(i)).equals(l)) {
                        int nrow = panel.getTable().convertRowIndexToView(i);
                        SwingUtilities.invokeLater(() -> panel.getTable().setRowSelectionInterval(nrow, nrow));
                        break;
                    }
                }
                //                    SwingUtilities.invokeLater(() -> table.setRowSelectionInterval(row, row));
            }
        }
    }

}
