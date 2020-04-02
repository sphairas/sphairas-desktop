/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.project.ui.wizard;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.project.UnitTargetProjectTemplate;
import org.thespheres.betula.project.UnitTargetProjectTemplate.UnitTargetSelection;

/**
 *
 * @author boris.heithecker
 */
class TargetUnitModel extends DefaultComboBoxModel<UnitTargetSelection> {

    private final RequestProcessor RP = new RequestProcessor(TargetUnitModel.class);
    UnitTargetProjectTemplate template;

    TargetUnitModel() {
    }

    void updateSelectedProvider(Properties p, UnitTargetProjectTemplate selection) {
        Mutex.EVENT.writeAccess(this::removeAllElements);
        if (selection != null && p != null) {
            template = selection;
            RP.post(this::reload);
        } else {
            template = null;
        }
    }

    private void reload() {
        try {
            final List<UnitTargetProjectTemplate.UnitTargetSelection> l = template.createList();
            if (!l.isEmpty()) {
                Mutex.EVENT.writeAccess(() -> l.stream().forEach(this::addElement));
            }
        } catch (IOException ioex) {
            Logger.getLogger(TargetUnitModel.class.getName()).log(Level.WARNING, ioex.getMessage(), ioex);
        }
    }

}
