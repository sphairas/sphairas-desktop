package org.thespheres.betula.services.implementation.ui.layerxml;

import org.thespheres.betula.adminconfig.layerxml.AbstractLayerFile;
import org.thespheres.betula.adminconfig.layerxml.LayerFolder;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

@XmlAccessorType(XmlAccessType.FIELD)
public class LayerFolderImpl extends AbstractLayerFile implements LayerFolder {

    @XmlElements(value = {
        @XmlElement(name = "folder", type = LayerFolderImpl.class),
        @XmlElement(name = "file", type = LayerFileImpl.class),
        @XmlElement(name = "attr", type = LayerFileAttribute.class)})
    protected List<AbstractLayerFile> folderOrFileOrAttr = new ArrayList<>();

    //JAXB only
    public LayerFolderImpl() {
    }

    protected LayerFolderImpl(String name) {
        super(name);
    }

    @Override
    public List<AbstractLayerFile> files() {
        return folderOrFileOrAttr;
    }

    LayerFolderImpl findChildFolder(final String name, final boolean create) {
        return files().stream()
                .filter(f -> f.getName().equals(name))
                .filter(LayerFolderImpl.class::isInstance)
                .map(LayerFolderImpl.class::cast)
                .findAny()
                .orElseGet(() -> {
                    if (create) {
                        final LayerFolderImpl ret = new LayerFolderImpl(name);
                        folderOrFileOrAttr.add(ret);
                        return ret;
                    } else {
                        return null;
                    }
                });
    }

    LayerFileImpl findChildFile(final String name, final boolean create) {
        return files().stream()
                .filter(f -> f.getName().equals(name))
                .filter(LayerFileImpl.class::isInstance)
                .map(LayerFileImpl.class::cast)
                .findAny()
                .orElseGet(() -> {
                    if (create) {
                        final LayerFileImpl ret = new LayerFileImpl(name);
                        folderOrFileOrAttr.add(ret);
                        return ret;
                    } else {
                        return null;
                    }
                });
    }

    public LayerFileImpl[] layerFiles() {
        return files().stream()
                .filter(LayerFileImpl.class::isInstance)
                .map(LayerFileImpl.class::cast)
                .toArray(LayerFileImpl[]::new);
    }

}
