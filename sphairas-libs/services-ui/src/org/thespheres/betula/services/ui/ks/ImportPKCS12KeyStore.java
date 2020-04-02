/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.ks;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.filechooser.FileFilter;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.services.ui.KeyStores;

@ActionID(category = "Tools",
        id = "org.thespheres.betula.services.ui.ks.ImportPKCS12KeyStore")
@ActionRegistration(
        displayName = "#ImportPKCS12KeyStore.displayName")
@ActionReference(path = "Menu/Tools", position = 700, separatorAfter = 900)
@Messages({"ImportPKCS12KeyStore.displayName=Zertifikat/Schlüssel importieren",
    "ImportPKCS12KeyStore.FileChooser.FileDescription=PKCS12 (.p12) Dateien",
    "ImportPKCS12KeyStore.FileChooser.Title=.p12-Datei auswählen",
    "ImportPKCS12KeyStore.showUserPasswordDialog.hint=Passwort für den privaten Schlüssel in der PKCS12-Datei (oder abbrechen)",
    "ImportPKCS12KeyStore.success=Schlüssel/Zertifikat erfolgreich importiert."})
public final class ImportPKCS12KeyStore implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        final Path p = showFileChooserDialog();
        if (p != null && Files.exists(p)) {
            final String file = p.toFile().getName();
            final String hint = NbBundle.getMessage(ImportPKCS12KeyStore.class, "ImportPKCS12KeyStore.showUserPasswordDialog.hint");
            try {
                final char[] pw = KeyStores.showUserPasswordDialog(file, hint, true);
                KeyStoreUtil.copyPKCS12EntriesToSystemKeyStore(p, pw);
                final String msg = NbBundle.getMessage(ImportPKCS12KeyStore.class, "ImportPKCS12KeyStore.success");
                StatusDisplayer.getDefault().setStatusText(msg);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private Path showFileChooserDialog() {
        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().endsWith(".p12");
            }

            @Override
            public String getDescription() {
                return NbBundle.getMessage(ImportPKCS12KeyStore.class, "ImportPKCS12KeyStore.FileChooser.FileDescription");
            }
        };
        File home = new File(System.getProperty("user.home"));
        String title = NbBundle.getMessage(ImportPKCS12KeyStore.class, "ImportPKCS12KeyStore.FileChooser.Title");
        FileChooserBuilder fcb = new FileChooserBuilder(ImportPKCS12KeyStore.class);
        fcb.setTitle(title).setDefaultWorkingDirectory(home).setFileHiding(true).setFileFilter(filter);
        File open = fcb.showOpenDialog();
        return open != null ? open.toPath() : null;
    }
}
