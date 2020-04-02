/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 *
 * @author boris.heithecker
 */
public class ExportToCSVUtil {

    private ExportToCSVUtil() {
    }

    public static void writeFile(final byte[] csv, final String fileNameHint) {
        File save = Mutex.EVENT.writeAccess(() -> showDialog(fileNameHint));
        if (save == null) {
            return;
        }
        if (!save.getName().endsWith(".csv")) {
            save = new File(save.getParent(), save.getName() + ".csv");
        }
        try {
            Files.write(save.toPath(), csv, StandardOpenOption.CREATE_NEW);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @NbBundle.Messages({"ExportToCSVUtil.FileChooser.Title=CSV Export",
        "ExportToCSVUtil.FileChooser.FileDescription=Exportiere nach csv (utf-8)",
        "ExportToCSVUtil.FileChooser.approve=Exportieren"
    })
    private static File showDialog(final String hint) {
        final File home = new File(System.getProperty("user.home"));
        final String title = NbBundle.getMessage(ExportToCSVUtil.class, "ExportToCSVUtil.FileChooser.Title");
        final String approve = NbBundle.getMessage(ExportToCSVUtil.class, "ExportToCSVUtil.FileChooser.approve");
        final FileChooserBuilderWithHint fcb = new FileChooserBuilderWithHint(ExportToCSVUtil.class, hint);
        fcb.setTitle(title).setDefaultWorkingDirectory(home).setApproveText(approve).setFileHiding(true);
        return fcb.showSaveDialog();
    }

}
