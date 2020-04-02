/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.reports.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.Tag;
import org.thespheres.betula.TermId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.util.MarkerAdapter;
import org.thespheres.betula.tag.TagAdapter;
import org.thespheres.betula.util.PropertyMapAdapter;

/**
 *
 * @author boris.heithecker
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractReportEntry {

    @XmlElement(name = "student")
    private StudentId student;
    @XmlElement(name = "term")
    private TermId term;
    @XmlElementWrapper(name = "markers")
    @XmlElement(name = "marker")
    @XmlJavaTypeAdapter(value = MarkerAdapter.XmlMarkerAdapter.class)
    private final Set<Marker> markers = new HashSet<>();
    @XmlElementWrapper(name = "tags")
    @XmlElement(name = "tag")
    @XmlJavaTypeAdapter(value = TagAdapter.XmlTagAdapter.class)
    private final Set<Tag> tags = new HashSet<>();
    @XmlElementWrapper(name = "tags")
    @XmlElement(name = "properties")
    @XmlJavaTypeAdapter(PropertyMapAdapter.class)
    private final Map<String, String> properties = new HashMap<>();
    @XmlElement(name = "document")
    protected DocumentId document;

    protected AbstractReportEntry() {
    }

    protected AbstractReportEntry(DocumentId document) {
        this.document = document;
    }

    public StudentId getStudent() {
        return student;
    }

    public void setStudent(StudentId student) {
        this.student = student;
    }

    public TermId getTerm() {
        return term;
    }

    public void setTerm(TermId term) {
        this.term = term;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public Set<Marker> getMarkers() {
        return markers;
    }

    public String getProperty(String name) {
        return properties.get(name);
    }

    public String setProperty(String name, String value) {
        return properties.put(name, value);
    }

    public DocumentId getDocument() {
        return document;
    }

    public void setDocument(DocumentId document) {
        this.document = document;
    }
}
