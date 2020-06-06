/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.database.configui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.admin.database.configui.AppResourcesFileChildren.Entry;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.NoProviderException;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.ui.ConfigurationException;
import org.thespheres.betula.services.ui.util.HttpUtilities;
import org.thespheres.betula.services.ui.util.dav.URLs;

@ActionID(category = "Tools",
        id = "org.thespheres.betula.admin.database.configui.UploadAction")
@ActionRegistration(displayName = "#UploadAction.displayName")
@Messages({"UploadAction.displayName=Datei hochladen",
    "UploadAction.FileChooserBuilder.title=Datei auswählen",
    "UploadAction.success=Datei {0} erfolgreich hochgeladen."})
public final class UploadAction implements ActionListener {

    private final Entry context;

    public UploadAction(final Entry context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent ev) {
        if (!context.isFolder()) {
            return;
        }
        final FileChooserBuilder fcb = new FileChooserBuilder(UploadAction.class);
        fcb.setFilesOnly(true);
        fcb.setTitle(NbBundle.getMessage(UploadAction.class, "UploadAction.FileChooserBuilder.title"));
        final File source = fcb.showSaveDialog();
        if (source == null || source.isDirectory()) {
            return;
        }
        final String resource = context.getResourcePath() + source.getName();
        try {
            uploadeFile(source, resource);
        } catch (final IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    void uploadeFile(final File source, final String resource) throws NoProviderException, IOException, ConfigurationException {
        final String davBase = URLs.adminResourcesDavBase(LocalProperties.find(context.getProvider()));
        final WebProvider service = WebProvider.find(context.getProvider(), WebProvider.class);
        final URI uri = URI.create(davBase + resource);
        final byte[] bytes = Files.readAllBytes(source.toPath());
        HttpUtilities.put(service, uri, bytes, null, null);
        final String msg = NbBundle.getMessage(UploadAction.class, "UploadAction.success", resource);
        StatusDisplayer.getDefault().setStatusText(msg);
    }
}
