/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.xml;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.ProviderRegistry;
import org.thespheres.betula.services.ws.CommonDocuments;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "NdsZeugnisSchulvorlage")
@XmlAccessorType(XmlAccessType.FIELD)
public class NdsZeugnisSchulvorlage implements Serializable, CommonDocuments {

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
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String schoolName2;
    @XmlElement(name = "Schulort")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String schoolLocation;
    @XmlElement(name = "Vorlage")
    private final Template template = new Template();
    @XmlElement(name = "Logos")
    private final Logos logos = new Logos();
    @XmlElement(name = "Listen")
    @XmlJavaTypeAdapter(DocumentsMapAdapter.class)
    private final Map<String, DocumentId> documents = new HashMap<>();

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

    public String getSchoolName2() {
        return schoolName2;
    }

    public void setSchoolName2(String schoolName2) {
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
}
