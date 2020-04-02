/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.thespheres.betula.ui.util.ExportToCSVOption;
import org.thespheres.betula.ui.util.ExportToCSVUtil;

/**
 *
 * @author boris.heithecker
 */
@ActionID(category = "Betula", id = "org.thespheres.betula.ui.actions.ExportCSVAction")
@ActionRegistration(displayName = "#ExportCSVAction.displayName",
        asynchronous = true,
        iconBase = "org/thespheres/betula/ui/resources/table-export.png")
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 1750),
    @ActionReference(path = "Shortcuts", name = "A-E")})
@NbBundle.Messages({"ExportCSVAction.displayName=Als csv-Datei speichern (utf-8)"})
public class ExportCSVAction implements ActionListener {

    private final ExportToCSVOption data;

    public ExportCSVAction(ExportToCSVOption sp) {
        data = sp;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final String hint;
        try {
            hint = data.createFileNameHint();
        } catch (IOException ex) {
            return;
        }
        try {
            ExportToCSVUtil.writeFile(data.getCSV(), hint);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

}
