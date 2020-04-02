/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.validation.impl;

import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.model.ReportDocument;
import org.thespheres.betula.document.model.Subject;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "grade-condition", namespace = "http://www.thespheres.org/xsd/niedersachsen/versetzung.xsd")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class GradeCondition extends Condition {

    //overrides configuration gradeConvention
    @XmlAttribute(name = "grade-convention")
    private String gradeConvention;
    @XmlList
    @XmlAttribute(name = "match")
    private String[] match;
    @XmlAttribute(name = "num-occurrence")
    private Integer occurrence;
    @XmlAttribute(name = "min-occurrence")
    private Integer minOccurrence;
    @XmlAttribute(name = "max-occurrence")
    private Integer maxOccurrence;
    @XmlList
    @XmlAttribute(name = "pair")
    private String[] pair;
    @XmlAttribute(name = "min-pairs")
    private Integer minPairs;
    @XmlIDREF
    @XmlAttribute(name = "matcher")
    private Matcher matcher;

    public GradeCondition() {
    }

    public GradeCondition(String match) {
        this(new String[]{match});
    }

    public GradeCondition(String[] test) {
        this.match = test;
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

    public String[] getMatch() {
        return match;
    }

    public void setMatch(String[] m) {
        this.match = m;
    }

    public Integer getOccurrence() {
        return occurrence;
    }

    public void setOccurrence(Integer occurrence) {
        this.occurrence = occurrence;
    }

    public Integer getMinOccurrence() {
        return minOccurrence;
    }

    public void setMinOccurrence(Integer minOccurrence) {
        this.minOccurrence = minOccurrence;
    }

    public Integer getMaxOccurrence() {
        return maxOccurrence;
    }

    public void setMaxOccurrence(Integer maxOccurrence) {
        this.maxOccurrence = maxOccurrence;
    }

    public String[] getPair() {
        return pair;
    }

    public void setPair(String[] pair) {
        this.pair = pair;
    }

    public Integer getMinPairs() {
        return minPairs;
    }

    public void setMinPairs(Integer minPairs) {
        this.minPairs = minPairs;
    }

    public Matcher getMatcher() {
        return matcher == null ? Matcher.DEFAULT : matcher;
    }

    public void setMatcher(Matcher matcher) {
        this.matcher = matcher;
    }

    @Override
    protected boolean evaluate(final Set<Subject> filtered, ReportDocument report, final PolicyRun props, final Policy policy) {
        final Matcher m = getMatcher();
        final Set<Subject> matches = m.match(report, filtered, this, props, policy);
        final long num = matches.size();
        boolean ret = true;
        if (getOccurrence() != null) {
            ret = num == getOccurrence();
        }
        if (getMaxOccurrence() != null) {
            ret = ret && num <= getMaxOccurrence();
        }
        if (getMinOccurrence() != null) {
            ret = ret && num >= getMinOccurrence();
        }
        return ret && m.pair(report, matches, filtered, this, props, policy);
    }
}
