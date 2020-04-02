/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmldefinitions;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.util.PropertyMapAdapter;
import org.thespheres.betula.xmldefinitions.XmlMarkerConventionDefinition.XmlMarkerSubsetDefinition;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "marker-definition")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlMarkerDefinition implements Marker, Serializable {

    @XmlTransient
    private XmlMarkerSubsetDefinition parent;
    @XmlAttribute(name = "id")
    private String id;
    @XmlElement(name = "message")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String message;
    @XmlElement(name = "label")
    private String label;
    @XmlAttribute(name = "deprecated")
    private Boolean deprec;
    @XmlElement(name = "properties")
    @XmlJavaTypeAdapter(PropertyMapAdapter.class)
    private final Map<String, String> names = new HashMap<>();
    @XmlElement(name = "description")
    private final List<XmlDescription> description = new ArrayList<>();

    public XmlMarkerDefinition() {
    }

    public XmlMarkerDefinition(XmlMarkerSubsetDefinition parent, String id, String message, String shortMessage) {
        this.parent = parent;
        this.id = id;
        this.message = message;
        this.label = shortMessage;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getSubset() {
        return parent.getSubset();
    }

    @Override
    public String getConvention() {
        return parent.parent.getName();
    }

    @Override
    public String getLongLabel(Object... args) {
        if (args != null && Arrays.stream(args).anyMatch(a -> a.toString().equals("category"))) {
            return parent.getCategory();
        }
        return MessageFormat.format(getMessage(), args);
    }

    protected String getMessage() {
        return message;
    }

    @Override
    public String getShortLabel() {
        return getLabel() != null ? getLabel() : getLongLabel();
    }

    protected String getLabel() {
        return label;
    }

    public List<XmlDescription> getDescription() {
        return description;
    }

    public boolean isDeprecated() {
        return deprec != null && deprec;
    }

    public void setDeprecated(boolean deprec) {
        this.deprec = deprec ? true : null;
    }

    void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        this.parent = (XmlMarkerSubsetDefinition) parent;
    }

    @Override
    public String toString() {
        return "{" + getConvention() + "}" + getSubset() + ":" + getId();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(getConvention());
        hash = 23 * hash + Objects.hashCode(getId());
        return 23 * hash + Objects.hashCode(getSubset());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Marker)) {
            return false;
        }
        final Marker other = (Marker) obj;
        if (!Objects.equals(getConvention(), other.getConvention())) {
            return false;
        }
        if (!Objects.equals(getId(), other.getId())) {
            return false;
        }
        return Objects.equals(getSubset(), other.getSubset());
    }

}
