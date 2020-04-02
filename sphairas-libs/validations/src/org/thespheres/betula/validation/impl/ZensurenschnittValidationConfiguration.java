/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.validation.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.util.MarkerAdapter.XmlMarkerAdapter;
import org.thespheres.betula.validation.impl.ListSubjectGroup.DefaultSubjectGroup;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "student-average-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class ZensurenschnittValidationConfiguration {

    @XmlElementWrapper(name = "report-document-distinguishing-markers")
    @XmlElement(name = "marker")
    @XmlJavaTypeAdapter(XmlMarkerAdapter.class)
    private Marker[] reportDistinguishingMarkers;
    @XmlAttribute(name = "grade-convention")
    private String gradeConvention;
    @XmlElementWrapper(name = "subject-groups")
    @XmlElementRef
    private SubjectFilter[] subjectGroups;
    @XmlElement(name = "property")
    private Property[] properties;
    @XmlElementRef
    private GradeDoubleConverter toDouble;

    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        DefaultSubjectGroup dsg = null;
        Set<Marker> set = new HashSet<>();
        if (subjectGroups != null) {
            for (SubjectFilter sg : subjectGroups) {
                if (sg.getName().equals("default")) {
                    dsg = (DefaultSubjectGroup) sg;
                } else if (sg instanceof ListSubjectGroup) {
                    Marker[] m = ((ListSubjectGroup) sg).getSubjects();
//                    if (m != null) {
                    Arrays.stream(m).forEach(set::add);
//                    }
                }
            }
        }
        if (dsg == null) {
            dsg = new DefaultSubjectGroup();
            SubjectFilter[] nsg;
            if (subjectGroups != null) {
                nsg = Arrays.copyOf(subjectGroups, subjectGroups.length + 1);
                nsg[nsg.length - 1] = dsg;
            } else {
                nsg = new SubjectFilter[]{dsg};
            }
            setSubjectGroups(nsg);
        }
        dsg.setSubjects(set.stream().toArray(Marker[]::new));
    }

    public Marker[] getReportDistinguishingMarkers() {
        return reportDistinguishingMarkers;
    }

    public void setReportDistinguishingMarkers(Marker[] reportDistinguishingMarkers) {
        this.reportDistinguishingMarkers = reportDistinguishingMarkers;
    }

    public String getGradeConvention() {
        return gradeConvention;
    }

    public void setGradeConvention(String gradeConvention) {
        this.gradeConvention = gradeConvention;
    }

    public SubjectFilter[] getSubjectGroups() {
        return subjectGroups;
    }

    public void setSubjectGroups(SubjectFilter[] subjectGroups) {
        this.subjectGroups = subjectGroups;
    }

    public Property[] getProperties() {
        return properties;
    }

    public void setProperties(Property[] properties) {
        this.properties = properties;

    }

    public GradeDoubleConverter getGradeDoubleConverter() {
        return toDouble;
    }

    public void setGradeDoubleConverter(GradeDoubleConverter toDouble) {
        this.toDouble = toDouble;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Property {

        @XmlAttribute(name = "key")
        private String key;
        @XmlAttribute(name = "value")
        private String value;

        public Property() {
        }

        public Property(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

    }
}
