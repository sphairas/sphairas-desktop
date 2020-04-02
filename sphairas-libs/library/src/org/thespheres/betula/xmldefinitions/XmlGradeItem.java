/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmldefinitions;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.assess.AbstractGrade;
import org.thespheres.betula.assess.Grade;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "grade-definition")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class XmlGradeItem implements Serializable, Grade.Biasable {

    @XmlTransient
    protected XmlAssessmentConventionDefintion cnv;
    @XmlAttribute(name = "id")
    protected String id;
    @XmlAttribute(name = "label")
    protected String label;
    @XmlElement(name = "message")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String message;
    @XmlElement(name = "description")
    private final List<XmlDescription> description = new ArrayList<>();

    //JAXB only
    public XmlGradeItem() {
    }

    public XmlGradeItem(final XmlAssessmentConventionDefintion cnv, final String id, final String sl, final String ll) {
        this.cnv = cnv;
        this.id = id;
        this.label = sl;
        this.message = ll;
    }

    @Override
    public String getConvention() {
        return cnv.getName();
    }

    public List<XmlDescription> getDescription() {
        return description;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getShortLabel() {
        return label;
    }

    @Override
    public String getLongLabel(Object... args) {
        return MessageFormat.format(getMessage(), args);
    }

    protected String getMessage() {
        return message;
    }

    public void setMessage(String msg) {
        this.message = msg;
    }

    @Override
    public Grade getNextLower() {
        return null;
    }

    @Override
    public Grade getNextHigher() {
        return null;
    }

    @Override
    public Grade getUnbiased() {
        return this;
    }

    void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        this.cnv = (XmlAssessmentConventionDefintion) parent;
        if (this.cnv == null) {
            throw new IllegalStateException("Parent XmlAssessmentConventionDefintion cannot be null.");
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + Objects.hashCode(this.cnv.getName());
        return 73 * hash + Objects.hashCode(this.id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Grade)) {
            return false;
        }
        final Grade other = (Grade) obj;
        if (!Objects.equals(this.id, other.getId())) {
            return false;
        }
        return Objects.equals(this.cnv.getName(), other.getConvention());
    }

    @Override
    public String toString() {
        return "{" + getConvention() + "}" + getId();
    }

    public Object writeReplace() {
        return new AbstractGrade(getConvention(), getId());
    }

}
