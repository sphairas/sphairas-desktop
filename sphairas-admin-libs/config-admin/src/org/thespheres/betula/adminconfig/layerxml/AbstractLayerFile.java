package org.thespheres.betula.adminconfig.layerxml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractLayerFile {

    @XmlAttribute(name = "name")
    @XmlJavaTypeAdapter(value = NormalizedStringAdapter.class)
    protected String name;

    protected AbstractLayerFile() {
    }
    
    protected AbstractLayerFile(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
