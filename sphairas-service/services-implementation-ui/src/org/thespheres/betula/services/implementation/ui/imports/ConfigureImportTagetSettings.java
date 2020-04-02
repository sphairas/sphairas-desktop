/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.imports;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "Betula",
        id = "org.thespheres.betula.services.implementation.ui.imports.ConfigureImportTagetSettings")
@ActionRegistration(iconBase = "org/thespheres/betula/services/implementation/ui/resources/wrench--pencil.png",
        displayName = "#CTL_ConfigureImportTagetSettings")
@ActionReference(path = "Menu/import-export", position = 200000, separatorBefore = 190000)
@Messages("CTL_ConfigureImportTagetSettings=Einstellungen")
public final class ConfigureImportTagetSettings implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO implement action body
    }
}
