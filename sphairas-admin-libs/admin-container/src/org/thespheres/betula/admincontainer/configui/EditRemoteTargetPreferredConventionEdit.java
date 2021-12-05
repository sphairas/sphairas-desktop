/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admincontainer.configui;

import java.io.IOException;
import java.util.List;
import javax.swing.undo.AbstractUndoableEdit;
import org.openide.util.Mutex;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.admin.units.AbstractUnitOpenSupport;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.xmlimport.utilities.TargetItemsUpdater;
import org.thespheres.betula.xmlimport.utilities.UpdaterFilter;

/**
 *
 * @author boris.heithecker
 */
@Messages({"EditRemoteTargetMarkersEdit.missingMarkersInSelection.message=Vorsicht! Es können nicht alle bereits \nvorhandenen Markierungen anzeigt werden!"})
class EditRemoteTargetPreferredConventionEdit extends AbstractUndoableEdit {

    private final AbstractUnitOpenSupport support;
    private final List<EditRemoteTargetPreferredConventionImportTargetsItem> items;
    private final WebServiceProvider service;
    //private final ConfigurableImportTarget importTarget;
    private final AssessmentConvention update;

    EditRemoteTargetPreferredConventionEdit(final AbstractUnitOpenSupport uos, final List<EditRemoteTargetPreferredConventionImportTargetsItem> items, final WebServiceProvider wsp, final AssessmentConvention pu) throws IOException {
        this.support = uos;
        this.items = items;
        this.service = wsp;
        //importTarget = TargetsUtil.findCommonImportTarget(support);
        this.update = pu;
    }

    void runAction() {
        final String pc = update != null ? update.getName() : "null";
        this.items.forEach(i -> i.setPreferredConvention(pc));
        final PreferredConventionUpdater u = new PreferredConventionUpdater(items.stream().toArray(EditRemoteTargetPreferredConventionImportTargetsItem[]::new), service, null, null);
        service.getDefaultRequestProcessor().post(() -> {
            u.run();
            if (u.getException() == null) {
                Mutex.EVENT.writeAccess(() -> support.getUndoSupport().postEdit(this));
            }
        });
    }

    class PreferredConventionUpdater extends TargetItemsUpdater<EditRemoteTargetPreferredConventionImportTargetsItem> {

        PreferredConventionUpdater(EditRemoteTargetPreferredConventionImportTargetsItem[] impKurse, WebServiceProvider provider, Term current, List<UpdaterFilter> filters) {
            super(impKurse, provider, current, filters);
        }

    }

}
