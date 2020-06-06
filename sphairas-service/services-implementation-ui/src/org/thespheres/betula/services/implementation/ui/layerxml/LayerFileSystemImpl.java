package org.thespheres.betula.services.implementation.ui.layerxml;

import org.thespheres.betula.adminconfig.layerxml.LayerFileSystem;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "filesystem")
@XmlAccessorType(XmlAccessType.FIELD)
public class LayerFileSystemImpl extends LayerFolderImpl implements LayerFileSystem {

    public LayerFileSystemImpl() {
        super(null);
    }

    @Override
    public LayerFileImpl getFile(final String path, final boolean create) {
        if (path == null || path.length() < 2 || !path.startsWith("/")) {
            throw new IllegalArgumentException("Empty path or path does not start with //");
        }
        if (path.endsWith("/")) {
            throw new IllegalArgumentException("Path ends with //");
        }
        final String[] el = path.split("/");
        LayerFolderImpl parent = this;
        int i = 1;
        while (parent != null) {
            final boolean lastIndex = i == el.length - 1;
            final String n = el[i++];
            if (lastIndex) {
                return parent.findChildFile(n, create);
            } else {
                final LayerFolderImpl f = parent.findChildFolder(n, create);
                parent = f;
            }
        }
        return null;
    }

}
