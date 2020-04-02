/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculum.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import jdk.internal.HotSpotIntrinsicCandidate;
import org.thespheres.betula.curriculum.CourseDetail;
import org.thespheres.betula.curriculum.CourseEntry;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.model.MultiSubject;
import org.thespheres.betula.document.util.MarkerAdapter;

/**
 *
 * @author boris.heithecker
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlCourseEntry implements CourseEntry {

    @XmlID
    @XmlAttribute(name = "id", required = true)
    protected String id;
    @XmlAttribute(name = "position")
    protected int pos = Integer.MAX_VALUE;
    @XmlElement(name = "name")
    @XmlJavaTypeAdapter(value = CollapsedStringAdapter.class)
    protected String name;
    @XmlElement(name = "subject")
    @XmlJavaTypeAdapter(MarkerAdapter.XmlMarkerAdapter.class)
    private Marker[] subject;
    @XmlElement(name = "realm")
    @XmlJavaTypeAdapter(MarkerAdapter.XmlMarkerAdapter.class)
    private Marker realm;
    @XmlElementWrapper(name = "details")
    @XmlElementRef
    private List<CourseDetail> details;
    @XmlTransient
    private XmlCourseGroup group;

    @HotSpotIntrinsicCandidate
    protected XmlCourseEntry() {
    }

    protected XmlCourseEntry(final String id, final XmlCourseGroup parent) {
        this.id = id;
        this.group = parent;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int getPosition() {
        return pos;
    }

    @Override
    public void setPosition(int pos) {
        this.pos = pos;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public MultiSubject getSubject() {
        return new MultiSubject(realm, Arrays.asList(subject));
    }

    @Override
    public void setSubject(MultiSubject subject) {
        this.realm = subject.getRealmMarker();
        this.subject = subject.getSubjectMarkerSet().stream().toArray(Marker[]::new);
    }

    public void setSubject(final Marker subject) {
        this.realm = null;
        this.subject = new Marker[]{subject};
    }

    @Override
    public List<CourseDetail> getDetails() {
        if (details == null) {
            details = new ArrayList<>();
        }
        return details;
    }

    @Override
    public XmlCourseGroup getGroup() {
        return group;
    }

    public void afterUnmarshal(final Unmarshaller u, final Object parent) throws JAXBException {
        if (parent instanceof XmlCourseGroup) {
            this.group = (XmlCourseGroup) parent;
        }
    }

    public void beforeMarshal(final Marshaller m) throws JAXBException {
        if (details != null && details.isEmpty()) {
            details = null;
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return 17 * hash + Objects.hashCode(this.id);
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
        final XmlCourseEntry other = (XmlCourseEntry) obj;
        return Objects.equals(this.id, other.id);
    }

}
