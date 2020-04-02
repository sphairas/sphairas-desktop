/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.validation.impl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.util.MarkerAdapter;

/**
 *
 * @author boris.heithecker
 */
@XmlSeeAlso({GradeCondition.class})
@XmlRootElement(name = "policy") //, namespace = "http://www.thespheres.org/xsd/niedersachsen/versetzung.xsd")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class Policy {

    @XmlID
    @XmlAttribute(name = "name")
    private String policyId;
    @XmlElementRef //(name = "condition")
    private Condition[] conditions;
    @XmlElement(name = "grouping-condition")
    private GroupingCondition[] groupingConditions;
    @XmlElement(name = "hint")
    private PolicyLegalHint hint;
//    @XmlElementWrapper(name = "markers")
    @XmlElement(name = "marker")
    @XmlJavaTypeAdapter(value = MarkerAdapter.XmlMarkerAdapter.class)
    private Marker[] markers;
//    @XmlElementWrapper(name = "properties")
    @XmlElement(name = "property")
    private VersetzungsValidationConfiguration.Property[] properties;

    public Policy() {
    }

    public Policy(String policyId) {
        this.policyId = policyId;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public Condition[] getConditions() {
        return conditions;
    }

    public void setConditions(Condition[] conditions) {
        this.conditions = conditions;
    }

    public GroupingCondition[] getGroupingCondition() {
        return groupingConditions;
    }

    public void setGroupingCondition(GroupingCondition[] groupingCondition) {
        this.groupingConditions = groupingCondition;
    }

    public PolicyLegalHint getHint() {
        return hint;
    }

    public void setHint(PolicyLegalHint hint) {
        this.hint = hint;
    }

    public Marker[] getMarkers() {
        return markers;
    }

    public void setMarkers(Marker[] markers) {
        this.markers = markers;
    }

    public VersetzungsValidationConfiguration.Property[] getProperties() {
        return properties;
    }

    public void setProperties(VersetzungsValidationConfiguration.Property[] properties) {
        this.properties = properties;
    }

    @XmlAccessorType(value = XmlAccessType.FIELD)
    public static class GroupingCondition {

        public enum Type {
            @XmlEnumValue("occurrences")
            OCCURRENCES,
            @XmlEnumValue("policy")
            POLICY,
            @XmlEnumValue("none")
            NONE
        }

        @XmlIDREF
        @XmlAttribute(name = "subject-groups")
        private SubjectFilter[] subjectGroups;
        @XmlAttribute(name = "type")
        private Type type;
        @XmlElementRef //(name = "condition")
        private Condition[] conditions;

        public GroupingCondition() {
        }

        public GroupingCondition(Type type, SubjectFilter[] subjectGroups) {
            this.type = type;
            this.subjectGroups = subjectGroups;
        }

        public SubjectFilter[] getSubjectGroups() {
            return subjectGroups;
        }

        public void setSubjectGroups(SubjectFilter[] subjectGroups) {
            this.subjectGroups = subjectGroups;
        }

        public Type getType() {
            return type;
        }

        public Condition[] getConditions() {
            return conditions;
        }

        public void setConditions(Condition[] conditions) {
            this.conditions = conditions;
        }
    }

}
