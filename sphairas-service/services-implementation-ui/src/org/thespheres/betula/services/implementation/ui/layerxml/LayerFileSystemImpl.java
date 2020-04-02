package org.thespheres.betula.services.implementation.ui.layerxml;

import org.thespheres.betula.adminconfig.layerxml.AbstractLayerFile;
import java.util.ArrayList;
import java.util.List;
import org.thespheres.betula.adminconfig.layerxml.LayerFileSystem;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "filesystem")
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class LayerFileSystemImpl extends LayerFolderImpl implements LayerFileSystem {

    @XmlElements(value = {
        @XmlElement(name = "folder", type = LayerFolderImpl.class),
        @XmlElement(name = "file", type = LayerFileImpl.class),
        @XmlElement(name = "attr", type = LayerFileAttribute.class)})
    protected List<AbstractLayerFile> folderOrFileOrAttr = new ArrayList<>();

    public LayerFileSystemImpl() {
        super(null);
    }

    @Override
    public List files() {
        return folderOrFileOrAttr;
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
