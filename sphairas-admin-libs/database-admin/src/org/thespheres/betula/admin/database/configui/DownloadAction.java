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
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.admin.database.configui.AppResourcesFileChildren.Entry;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.ui.util.HttpUtilities;
import org.thespheres.betula.services.ui.util.dav.URLs;

@ActionID(category = "Tools",
        id = "org.thespheres.betula.admin.database.configui.DownloadAction")
@ActionRegistration(displayName = "#DownloadAction.displayName")
@Messages({"DownloadAction.displayName=Herunterladen",
    "DownloadAction.FileChooserBuilder.title=Verzeichnis auswählen",
    "DownloadAction.success=Datei {0} erfolgreich heruntergeladen."})
public final class DownloadAction implements ActionListener {

    private final List<Entry> context;

    public DownloadAction(final List<Entry> context) {
        this.context = context.stream()
                .filter(e -> !e.isFolder())
                .collect(Collectors.toList());
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        if (context.isEmpty()) {
            return;
        }
        final FileChooserBuilder fcb = new FileChooserBuilder(DownloadAction.class);
        fcb.setDirectoriesOnly(true);
        fcb.setTitle(NbBundle.getMessage(DownloadAction.class, "DownloadAction.FileChooserBuilder.title"));
        final File targetDir = fcb.showOpenDialog();
        if (targetDir == null) {
            return;
        }
        for (final Entry entry : context) {
            if (!entry.isFolder()) {
                try {
                    downLoadEntry(entry, targetDir.toPath());
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    void downLoadEntry(final Entry entry, final Path targetDir) throws IOException {
        final String davBase = URLs.adminResourcesDavBase(LocalProperties.find(entry.getProvider()));
        final WebProvider service = WebProvider.find(entry.getProvider(), WebProvider.class);
        final URI uri = URI.create(davBase + entry.getResourcePath());
        final Path tmp = HttpUtilities.get(service, uri, this::readToTempFile, null, false);
        try {
            Files.copy(tmp, targetDir.resolve(entry.getName()), StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(tmp);
        }
        final String msg = NbBundle.getMessage(UploadAction.class, "DownloadAction.success", entry.getResourcePath());
        StatusDisplayer.getDefault().setStatusText(msg);
    }

    Path readToTempFile(final String lm, final InputStream is) throws IOException {
        final Path ret = Files.createTempFile(null, null);
        try {
            final byte[] bytes = IOUtils.toByteArray(is);
            Files.write(ret, bytes);
        } catch (final IOException ex) {
            Files.deleteIfExists(ret);
            throw ex;
        }
        return ret;
    }
}
