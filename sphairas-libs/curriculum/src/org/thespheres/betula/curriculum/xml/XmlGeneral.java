/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculum.xml;

import org.thespheres.betula.curriculum.General;
import java.time.LocalDate;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.util.LocalDateAdapter;

/**
 *
 * @author boris.heithecker
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlGeneral implements General {

    @XmlAttribute(name = "value-from")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate begin;
    @XmlAttribute(name = "valid-until")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate end;
    @XmlElement(name = "display-name")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String displayName;
    @XmlElement(name = "preferred-convention")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String preferredConvention;
    @XmlElementWrapper(name = "preferred-realm-conventions")
    @XmlElement(name = "marker-convention")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String[] preferredRealms;
    @XmlElementWrapper(name = "preferred-subject-conventions")
    @XmlElement(name = "marker-convention")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String[] preferredSubjects;

    @Override
    public String getPreferredAssessmenConvention() {
        return preferredConvention;
    }

    public void setPreferredAssessmenConvention(String preferredConvention) {
        this.preferredConvention = preferredConvention;
    }

    @Override
    public LocalDate getValidFrom() {
        return begin;
    }

    public void setValidFrom(LocalDate from) {
        begin = from;
    }

    @Override
    public LocalDate getValidUntil() {
        return end;
    }

    public void setValidUntil(LocalDate to) {
        end = to;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String name) {
        displayName = name;
    }

    @Override
    public String[] getPreferredSubjectConventions(boolean realm) {
        return realm ? this.preferredRealms : this.preferredSubjects;
    }

    public void setPreferredSubjectConventions(String[] value, boolean realm) {
        if (realm) {
            this.preferredRealms = value;
        } else {
            this.preferredSubjects = value;
        }
    }
}
