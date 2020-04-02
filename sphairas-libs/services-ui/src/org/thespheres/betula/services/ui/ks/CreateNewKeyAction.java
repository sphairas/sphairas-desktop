/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.ks;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

//@ActionID(category = "Tools",
//        id = "org.thespheres.betula.services.ui.ks.CreateNewKeyAction")
//@ActionRegistration(
//        displayName = "#CreateNewKeyAction.displayName")
//@ActionReference(path = "Menu/Tools", position = 720, separatorAfter = 900)
@Messages({"CreateNewKeyAction.displayName=Zertifikat/Schlüssel importieren",
    "CreateNewKeyAction.FileChooser.FileDescription=PKCS12 (.p12) Dateien",
    "CreateNewKeyAction.FileChooser.Title=.p12-Datei auswählen",
    "CreateNewKeyAction.showUserPasswordDialog.hint=Passwort für den privaten Schlüssel in der PKCS12-Datei",
    "CreateNewKeyAction.success=Schlüssel/Zertifikat erfolgreich importiert."})
public final class CreateNewKeyAction implements ActionListener {

    static String PROP_NAME;
    static String PROP_CN;
    static String PROP_O;
    static String PROP_C;
    static String PROP_ST;

    @Override
    public void actionPerformed(ActionEvent e) {

    }

}
