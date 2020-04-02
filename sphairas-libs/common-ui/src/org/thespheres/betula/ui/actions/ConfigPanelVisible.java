/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

@ActionID(
        category = "Window",
        id = "org.thespheres.betula.ui.actions.ConfigPanelVisible"
)
@ActionRegistration(
        displayName = "#ConfigPanelVisible.displayName"
)
@Messages("ConfigPanelVisible.displayName=Eigenschaften und Einstellungen")
public final class ConfigPanelVisible implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        final TopComponent tc = WindowManager.getDefault().findTopComponent("ConfigurationPanelsTopComponent");
        tc.requestVisible();
    }
}
