/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.validate;

import java.util.Comparator;
import java.util.stream.StreamSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbCollections;

/**
 *
 * @author boris.heithecker
 */
public class ValidationConfigUtilities {

    public static FileObject findLastConfigFile(final String configFolder) {
        final FileObject folder = FileUtil.getConfigFile(configFolder);
        if (folder != null && folder.isFolder()) {
            return StreamSupport.stream(NbCollections.iterable(folder.getData(false)).spliterator(), false)
                    .max(Comparator.comparing(ValidationConfigUtilities::positionOf))
                    .orElse(null);
        }
        return null;
    }

    static int positionOf(FileObject fo) {
        final Object attr = fo.getAttribute("position");
        if (attr != null && attr instanceof Integer) {
            return (int) attr;
        }
        return 0;
    }

}
