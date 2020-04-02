/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admincontainer.action;

import java.util.Collections;
import java.util.Set;
import javax.swing.undo.AbstractUndoableEdit;
import org.openide.util.Mutex;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.admin.units.AbstractUnitOpenSupport;
import org.thespheres.betula.admincontainer.action.ClearTargetsForTermImportTargetsItem.ResetType;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.util.ChangeSet;
import org.thespheres.betula.xmlimport.ImportTarget;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.uiutil.ImportWizardSettings;
import org.thespheres.betula.xmlimport.utilities.TargetDocumentProperties;
import org.thespheres.betula.xmlimport.utilities.TargetItemsUpdater;
import org.thespheres.betula.xmlimport.utilities.UpdaterFilter;

/**
 *
 * @author boris.heithecker
 */
class ClearTargetsForTermEdit extends AbstractUndoableEdit implements ImportWizardSettings.TargetItemSettings<ImportTarget, ClearTargetsForTermImportTargetsItem>, UpdaterFilter<ClearTargetsForTermImportTargetsItem, TargetDocumentProperties> {

    private final AbstractUnitOpenSupport support;
    private final ChangeSet<ClearTargetsForTermImportTargetsItem> items;
    private final WebServiceProvider service;
    private final Term term;

    ClearTargetsForTermEdit(AbstractUnitOpenSupport uos, Set<ClearTargetsForTermImportTargetsItem> items, WebServiceProvider wsp, Term tm) {
        this.support = uos;
        this.items = new ChangeSet<>(items);
        this.service = wsp;
        this.term = tm;
    }

    @Override
    public ImportTarget getImportTargetProperty() {
        return null;
    }

    @Override
    public ChangeSet<ClearTargetsForTermImportTargetsItem> getSelectedNodesProperty() {
        return items;
    }

    @Override
    public boolean accept(ClearTargetsForTermImportTargetsItem iti, TargetDocumentProperties td, StudentId student, TermId term, ImportTargetsItem.GradeEntry entry) {
        return !iti.getResetType().equals(ResetType.SIGNEES);
    }

    void runAction() {
        final TargetItemsUpdater update = new TargetItemsUpdater(items.stream().toArray(ClearTargetsForTermImportTargetsItem[]::new), service, term, Collections.singletonList(this));
        service.getDefaultRequestProcessor().post(() -> {
            update.run();
            if (update.getException() == null) {
                Mutex.EVENT.writeAccess(() -> support.getUndoSupport().postEdit(this));
            }
        });
    }

}
