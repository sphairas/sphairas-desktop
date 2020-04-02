/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Signee;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "target-item") //, namespace = "http://www.thespheres.org/xsd/betula/xml-import.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlTargetItem extends XmlUnitItem {

    @XmlElement(name = "source-subject")
    protected String sourceSubject;
    @XmlElement(name = "subject-alternative-name")
    protected String subjectAlternativeName;
    @XmlElement(name = "signee", namespace = "http://www.thespheres.org/xsd/betula/betula.xsd")
    protected Signee signee;
    @Deprecated
    @XmlElement(name = "signee")
    protected Signee signeeDeprecated;
    @XmlElement(name = "source-signee")
    protected String sourceSignee;
    @XmlElement(name = "target-document", namespace = "http://www.thespheres.org/xsd/betula/betula.xsd")
    protected DocumentId target;
    @Deprecated
    @XmlElement(name = "target-document")
    protected DocumentId targetDeprecated;
    @XmlElement(name = "source-target")
    protected String sourceTarget; //kurs, klasse
    @XmlElement(name = "assessment-convention")
    protected String assessmentConvention;
    @XmlElement(name = "target-type")
    protected String targetType;
    @XmlElementWrapper(name = "entries")
    @XmlElementRef
    protected List<XmlTargetEntryItem> entries;

    public String getSourceSubject() {
        return sourceSubject;
    }

    public Signee getSignee() {
        return signee != null ? signee : signeeDeprecated;
    }

    public void setSignee(Signee signee) {
        this.signee = signee;
    }

    public String getSourceSignee() {
        return sourceSignee;
    }

    public DocumentId getTarget() {
        return target != null ? target : targetDeprecated;
    }

    public void setTarget(DocumentId target) {
        this.target = target;
    }

    public String getSourceTarget() {
        return sourceTarget;
    }

    public String getAssessmentConvention() {
        return assessmentConvention;
    }

    public void setAssessmentConvention(String convention) {
        this.assessmentConvention = convention;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getSubjectAlternativeName() {
        return subjectAlternativeName;
    }

    public void setSubjectAlternativeName(String subjectAlternativeName) {
        this.subjectAlternativeName = subjectAlternativeName;
    }

    public List<XmlTargetEntryItem> getEntries() {
        if (entries == null) {
            entries = new ArrayList<>();
        }
        return entries;
    }

}
