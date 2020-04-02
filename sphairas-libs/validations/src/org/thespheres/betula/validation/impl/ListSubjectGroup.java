/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.validation.impl;

import java.util.Arrays;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.model.Subject;
import org.thespheres.betula.document.util.MarkerAdapter;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "subject-group") //, namespace = "http://www.thespheres.org/xsd/niedersachsen/versetzung.xsd")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class ListSubjectGroup extends SubjectFilter {

    @XmlAttribute(name = "local-properties-provider")
    private String propProv;
    @XmlAttribute(name = "local-properties-key")
    private String propKey;
    @XmlElement(name = "subject")
    @XmlJavaTypeAdapter(value = MarkerAdapter.XmlMarkerAdapter.class)
    private Marker[] markers;
    @XmlElement(name = "display-name")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String display;

    public ListSubjectGroup() {
        super(null);
    }

    public ListSubjectGroup(String name, Marker[] subjects, String display) {
        super(name);
        this.markers = subjects;
        this.display = display;
    }

    public ListSubjectGroup(String name) {
        super(name);
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDisplayName() {
        return display;
    }

    public String getPropProv() {
        return propProv;
    }

    public void setPropProv(String propProv) {
        this.propProv = propProv;
    }

    public String getPropKey() {
        return propKey;
    }

    public void setPropKey(String propKey) {
        this.propKey = propKey;
    }

    public void setSubjects(Marker[] subjectIds) {
        this.markers = subjectIds;
    }

    public Marker[] getSubjects() {
        return markers;
    }

    @Override
    public boolean matches(Subject subject) {
        return markers != null && Arrays.stream(markers).anyMatch(subject.getSubjectMarker()::equals);
    }

    @XmlRootElement(name = "default-subject-group")
    @XmlAccessorType(value = XmlAccessType.FIELD)
    static class DefaultSubjectGroup extends SubjectFilter {

        @XmlTransient
        private Marker[] markers;

        public DefaultSubjectGroup() {
            super("default");
        }

        @Override
        public String getDisplayName() {
            return (getSubjects() == null || getSubjects().length == 0) ? "Alle Fächer" : "Andere Fächer";
        }

        Marker[] getSubjects() {
            return markers;
        }

        void setSubjects(Marker[] arr) {
            markers = arr;
        }

        @Override
        public boolean matches(Subject subject) {
            return markers == null || Arrays.stream(markers).noneMatch(subject.getSubjectMarker()::equals);
        }

    }
}
