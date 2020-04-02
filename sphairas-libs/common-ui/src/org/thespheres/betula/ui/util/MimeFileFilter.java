/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.util;

import java.io.File;
import javax.swing.filechooser.FileFilter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author boris.heithecker
 */
public class MimeFileFilter extends FileFilter {

    private final String mime;
    private final String description;

    public MimeFileFilter(String mime, String description) {
        this.mime = mime;
        this.description = description;
    }

    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        FileObject fo = FileUtil.toFileObject(f);
        if (fo != null) {
            if (fo.getMIMEType().contains(mime)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
