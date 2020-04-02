/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.project.impl;

import java.io.File;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.thespheres.betula.project.BetulaProject;

/**
 *
 * @author boris.heithecker
 */
class Util {

    public static boolean isHidden(FileObject fo) {
        //.getFolders(false))) {
        File f = FileUtil.toFile(fo);
        final Object attr = fo.getAttribute("user-hidden");
        boolean userHidden = attr instanceof Boolean && (Boolean) attr;
        if (!fo.isFolder() && fo.getMIMEType().equals("content/unknown")) {
            userHidden = true;
        }
        return (f != null && f.isHidden()) || fo.getName().equals(BetulaProject.SPHAIRAS_PROJECT) || userHidden;
    }

}
