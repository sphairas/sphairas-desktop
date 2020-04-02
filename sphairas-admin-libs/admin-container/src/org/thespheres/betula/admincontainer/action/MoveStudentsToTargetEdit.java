/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admincontainer.action;

import java.util.HashSet;
import java.util.Set;
import javax.swing.undo.AbstractUndoableEdit;
import org.openide.util.Mutex;
import org.thespheres.betula.admin.units.AbstractUnitOpenSupport;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.util.ChangeSet;
import org.thespheres.betula.xmlimport.uiutil.ImportWizardSettings;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;
import org.thespheres.betula.xmlimport.utilities.TargetItemsUpdater;

/**
 *
 * @author boris.heithecker
 */
class MoveStudentsToTargetEdit extends AbstractUndoableEdit implements ImportWizardSettings.TargetItemSettings<ConfigurableImportTarget, MoveStudentsToTargetImportTargetsItem> {

    private final AbstractUnitOpenSupport support;
    private final ChangeSet<MoveStudentsToTargetImportTargetsItem> items;
    private final ConfigurableImportTarget target;
    private final Term term;
    private final MoveStudentsToTargetImportTargetsItem addItem;
    private final Set<MoveStudentsToTargetImportTargetsItem> removeItems;

    MoveStudentsToTargetEdit(AbstractUnitOpenSupport uos, MoveStudentsToTargetImportTargetsItem add, Set<MoveStudentsToTargetImportTargetsItem> delete, ConfigurableImportTarget target, Term tm) {
        this.support = uos;
        this.addItem = add;
        this.removeItems = delete;
        final Set<MoveStudentsToTargetImportTargetsItem> s = new HashSet<>(removeItems);
        s.add(addItem);
        this.items = new ChangeSet<>(s);
        this.target = target;
        this.term = tm;
    }

    @Override
    public ConfigurableImportTarget getImportTargetProperty() {
        return null;
    }

    @Override
    public ChangeSet<MoveStudentsToTargetImportTargetsItem> getSelectedNodesProperty() {
        return items;
    }

    void runAction() {
        final WebServiceProvider service = target.getWebServiceProvider();
        final TargetItemsUpdater update = new TargetItemsUpdater(getSelectedNodesProperty().stream().toArray(MoveStudentsToTargetImportTargetsItem[]::new), service, term, null);
        service.getDefaultRequestProcessor().post(() -> {
            update.run();
            if (update.getException() == null) {
                Mutex.EVENT.writeAccess(() -> support.getUndoSupport().postEdit(this));
            }
        });
    }

}
