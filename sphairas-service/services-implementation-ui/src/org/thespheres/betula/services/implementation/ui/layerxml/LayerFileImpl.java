package org.thespheres.betula.services.implementation.ui.layerxml;

import org.thespheres.betula.adminconfig.layerxml.LayerFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
public class LayerFileImpl extends LayerFile {

    @XmlAttribute(name = "url")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String url;
    @XmlElement(name = "attr")
    protected List<LayerFileAttribute> attr = new ArrayList<>();
    @XmlAnyElement
    @XmlMixed
    protected List<Object> text;

    //JAXB only
    public LayerFileImpl() {
    }

    public LayerFileImpl(String name) {
        super(name);
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public void setUrl(String value) {
        this.url = value;
    }

    public String getValue() {
        return text != null && text.size() == 1 ? text.get(0).toString() : null;
    }

    @Override
    public void setValue(String value) {
        this.text = Collections.singletonList(value);
    }

    public List<LayerFileAttribute> getAttributes() {
        return attr;
    }

    @Override
    public void setStringValueAttribute(final String name, final String value) {
        final Optional<LayerFileAttribute> found = getAttributes().stream()
                .filter(a -> a.getName().equals(name))
                .findAny();
        if (found.isPresent()) {
            found.get().setStringvalue(value);
        } else {
            final LayerFileAttribute a = new LayerFileAttribute(name);
            a.setStringvalue(value);
            getAttributes().add(a);
        }
    }

    @Override
    public void setMethodValueAttribute(final String name, final String value) {
        final Optional<LayerFileAttribute> found = getAttributes().stream()
                .filter(a -> a.getName().equals(name))
                .findAny();
        if (found.isPresent()) {
            found.get().setMethodvalue(value);
        } else {
            final LayerFileAttribute a = new LayerFileAttribute(name);
            a.setMethodvalue(value);
            getAttributes().add(a);
        }
    }
}
