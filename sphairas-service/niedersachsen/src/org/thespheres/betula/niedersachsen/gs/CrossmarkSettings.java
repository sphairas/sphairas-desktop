/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.gs;

import java.util.Arrays;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.assess.GradeFactory;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerConvention;
import org.thespheres.betula.document.MarkerFactory;
import org.thespheres.betula.document.util.MarkerAdapter;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "niedersachsen-grundschule-ankreuzzeugnisse")
@XmlAccessorType(XmlAccessType.FIELD)
public class CrossmarkSettings {

    @XmlElement(name = "assessment-convention")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String assessmentConvention;

    @XmlElementWrapper(name = "ankreuzfaecher")
    @XmlElement(name = "ankreuzfach")
    private Mapping[] mappings;

    public Mapping[] getMappings() {
        return mappings;
    }

    public void setMappings(Mapping[] mappings) {
        this.mappings = mappings;
    }

    public String getAssessmentConventionName() {
        return assessmentConvention;
    }

    public AssessmentConvention getAssessmentConvention() {
        final String acn = getAssessmentConventionName();
        return acn == null ? null : GradeFactory.findConvention(acn);
    }

    public void setAssessmentConvention(String assessmentConvention) {
        this.assessmentConvention = assessmentConvention;
    }

    public String findConventionName(final int level, final Marker correspondingSubject) {
        if (mappings != null) {
            return Arrays.stream(mappings)
                    .filter(m -> m.getLevel() == level)
                    .filter(m -> Objects.equals(m.getCorrespondingSubject(), correspondingSubject))
                    .map(Mapping::getConvention)
                    .collect(CollectionUtil.singleOrNull());
        }
        return null;
    }

    public MarkerConvention findConvention(final int level, final Marker correspondingSubject) {
        final String cn = findConventionName(level, correspondingSubject);
        return cn == null ? null : MarkerFactory.findConvention(cn);
    }

    public MarkerConvention[] conventions(final int level) {
        if (mappings != null) {
            return Arrays.stream(mappings)
                    .filter(m -> m.getLevel() == level)
                    .map(Mapping::getConvention)
                    .map(MarkerFactory::findConvention)
                    .toArray(MarkerConvention[]::new);

        }
        return new MarkerConvention[0];
    }

    public String[] conventions() {
        if (mappings != null) {
            return Arrays.stream(mappings)
                    .map(Mapping::getConvention)
                    .toArray(String[]::new);
        }
        return new String[0];
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Mapping {

        @XmlAttribute(name = "stufe", required = true)
        private int level;

        @XmlElement(name = "convention")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        private String convention;

        @XmlElement(name = "fach-entsprechung")
        @XmlJavaTypeAdapter(MarkerAdapter.XmlMarkerAdapter.class)
        private Marker correspondingSubject;

        public Mapping() {
        }

        public Mapping(int level, String convention, Marker correspondingSubject) {
            this.level = level;
            this.convention = convention;
            this.correspondingSubject = correspondingSubject;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public String getConvention() {
            return convention;
        }

        public void setConvention(String convention) {
            this.convention = convention;
        }

        public Marker getCorrespondingSubject() {
            return correspondingSubject;
        }

        public void setCorrespondingSubject(Marker correspondingSubject) {
            this.correspondingSubject = correspondingSubject;
        }

    }
}
