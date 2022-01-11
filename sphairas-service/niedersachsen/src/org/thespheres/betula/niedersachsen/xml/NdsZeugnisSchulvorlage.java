/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.xml;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.ProviderRegistry;
import org.thespheres.betula.services.ws.CommonDocuments;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "NdsZeugnisSchulvorlage")
@XmlAccessorType(XmlAccessType.FIELD)
public class NdsZeugnisSchulvorlage implements Serializable, CommonDocuments {

    public static final String PROP_SIGNEES_NO_BACKGROUND = "Unterzeicher.ohne.Probedruck";
    public static final String PROP_FEHLTAGE_ZERO_STRING = "Fehltage.Null";
    @XmlAttribute(name = "provider")
    private String provider;
    @XmlElement(name = "XSL-FO-Datei")
    private String xslFoFile;
    @XmlAttribute(name = "Sortierung")
    private String subjectOrder;
    @XmlElement(name = "Schulname")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String schoolName;
    @XmlElement(name = "Schultitel")
    private Subtitle[] schoolName2;
    @XmlElement(name = "Schulort")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String schoolLocation;
    @XmlElement(name = "Vorlage")
    private final Template template = new Template();
    @XmlElement(name = "Logos")
    private final Logos logos = new Logos();
    @XmlElementWrapper(name = "Listen-Eintragsfarben")
    @XmlElement(name = "Listen-Eintragsfarbe")
    private List<Coloring> colorings = new CopyOnWriteArrayList<>();
    @XmlElement(name = "Listen")
    @XmlJavaTypeAdapter(DocumentsMapAdapter.class)
    private final Map<String, DocumentId> documents = new HashMap<>();
    @XmlElement(name = "Eigenschaft")
    private final List<Property> properties = new CopyOnWriteArrayList<>();

    //JAXB only
    public NdsZeugnisSchulvorlage() {
    }

    public NdsZeugnisSchulvorlage(final String provider) {
        this.provider = provider;
    }

    public String getProvider() {
        return provider;
    }

    public String getXslFoFile() {
        return xslFoFile;
    }

    public void setXslFoFile(final String xslFoFile) {
        this.xslFoFile = xslFoFile;
    }

    @Override
    public ProviderInfo getProviderInfo() {
        return provider != null ? ProviderRegistry.getDefault().get(provider) : null;
    }

    public String getSubjectOrder() {
        return subjectOrder;
    }

    public void setSubjectOrder(String subjectOrder) {
        this.subjectOrder = subjectOrder;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public Subtitle[] getSchoolName2() {
        return schoolName2 != null ? schoolName2 : new Subtitle[0];
    }

    public void setSchoolName2(Subtitle[] schoolName2) {
        this.schoolName2 = schoolName2;
    }

    public String getSchoolLocation() {
        return schoolLocation;
    }

    public void setSchoolLocation(String schoolLocation) {
        this.schoolLocation = schoolLocation;
    }

    public Logos getLogos() {
        return logos;
    }

    public String getImageLeftUrl() {
        return logos.getLeft();
    }

    public String getImageRightUrl() {
//        return "url('logo_35_25sw.png')";
        return logos.getRight();
    }

    public Template getTemplate() {
        return template;
    }

    public List<Coloring> getColorings() {
        return colorings;
    }

    public String getColoring(final Grade g) {
        return g == null ? null : colorings.stream()
                .filter(c -> c.getConvention().equals(g.getConvention()))
                .filter(c -> c.getIds() == null || c.getIds().contains(g.getId()))
                .findFirst()
                .map(Coloring::getColor)
                .orElse(null);
    }

    public Optional<Property> getProperty(final String name) {
        return properties.stream()
                .filter(p -> p.getName().equals(name))
                .collect(CollectionUtil.requireSingleton());
    }

    public void setProperty(final String name, final String value) {
        final Optional<Property> p = getProperty(name);
        if (value != null) {
            p.ifPresentOrElse(prop -> prop.setValue(value), () -> {
                final Property prop = new Property(name, value);
                properties.add(prop);
            });
        } else {
            p.ifPresent(properties::remove);
        }
    }

    @Override
    public DocumentId forName(final String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
        return documents.get(name);
    }

    public Map<String, DocumentId> documents() {
        return documents;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Coloring implements Serializable {

        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlAttribute(name = "html-Farbname", required = true)
        private String color;
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlAttribute(name = "Konvention", required = true)
        private String convention;
        @XmlList
        @XmlElement(name = "Werte")
        private String[] ids = null;

        public Coloring() {
        }

        public Coloring(final String color, final String convention, final String[] ids) {
            this.color = color;
            this.convention = convention;
            this.ids = ids;
        }

        public String getColor() {
            return color;
        }

        public String getConvention() {
            return convention;
        }

        public List<String> getIds() {
            return Optional.ofNullable(ids)
                    .map(Arrays::asList)
                    .orElse(null);
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Property implements Serializable {

        @XmlAttribute(name = "Name")
        private String name;
        @XmlAttribute(name = "Wert")
        private String value;

        public Property() {
        }

        public Property(final String name, final String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            return 17 * hash + Objects.hashCode(this.name);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Property other = (Property) obj;
            return Objects.equals(this.name, other.name);
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Logos implements Serializable {

        @XmlAttribute(name = "Links")
        private String left;
        @XmlAttribute(name = "Rechts")
        private String right;
        @XmlAttribute(name = "Breite")
        private String width;

        public String getLeft() {
            return left;
        }

        public void setLeft(String left) {
            this.left = left;
        }

        public String getRight() {
            return right;
        }

        public void setRight(String right) {
            this.right = right;
        }

        public String getWidth() {
            return width;
        }

        public void setWidth(String width) {
            this.width = width;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Template implements Serializable {

        @XmlAttribute(name = "Name")
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Subtitle {

        @XmlValue
        private String title;

        @XmlAttribute(name = "Schriftgröße")
        private String fontSize;

        public Subtitle() {
        }

        public Subtitle(String title, String fontSize) {
            this.title = title;
            this.fontSize = fontSize;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getFontSize() {
            return fontSize;
        }

        public void setFontSize(String fontSize) {
            this.fontSize = fontSize;
        }

    }
}
