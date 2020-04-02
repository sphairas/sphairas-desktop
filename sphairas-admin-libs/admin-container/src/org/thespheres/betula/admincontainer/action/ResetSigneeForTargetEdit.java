/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admincontainer.action;

import java.util.Set;
import javax.swing.undo.AbstractUndoableEdit;
import org.openide.util.Mutex;
import org.thespheres.betula.admin.units.SigneesTopComponentModel;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.util.ChangeSet;
import org.thespheres.betula.xmlimport.ImportTarget;
import org.thespheres.betula.xmlimport.uiutil.ImportWizardSettings;
import org.thespheres.betula.xmlimport.utilities.TargetItemsUpdater;

/**
 *
 * @author boris.heithecker
 */
class ResetSigneeForTargetEdit extends AbstractUndoableEdit implements ImportWizardSettings.TargetItemSettings<ImportTarget, ResetSigneeForTargetImportTargetsItem> {

    private final ChangeSet<ResetSigneeForTargetImportTargetsItem> items;
    private final WebServiceProvider service;
    private final SigneesTopComponentModel model;

    ResetSigneeForTargetEdit(Set<ResetSigneeForTargetImportTargetsItem> items, final WebServiceProvider wsp, final SigneesTopComponentModel m) {
        this.items = new ChangeSet<>(items);
        this.service = wsp;
        this.model = m;
    }

    @Override
    public ImportTarget getImportTargetProperty() {
        return null;
    }

    @Override
    public ChangeSet<ResetSigneeForTargetImportTargetsItem> getSelectedNodesProperty() {
        return items;
    }

    void runAction() {
        final TargetItemsUpdater update = new TargetItemsUpdater(items.stream().toArray(ResetSigneeForTargetImportTargetsItem[]::new), service, null, null);
        service.getDefaultRequestProcessor().post(() -> {
            update.run();
            if (update.getException() == null) {
                Mutex.EVENT.writeAccess(() -> model.getUndoSupport().postEdit(this));
            }
        });
    }

}
