/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.TermId;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.util.GradeAdapter;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "target-entry-item") //, namespace = "http://www.thespheres.org/xsd/betula/xml-import.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlTargetEntryItem extends XmlStudentItem {

    @XmlElement(name = "target-document", namespace = "http://www.thespheres.org/xsd/betula/betula.xsd")
    private DocumentId target;
    @XmlElement(name = "grade", namespace = "http://www.thespheres.org/xsd/betula/betula.xsd")
    @XmlJavaTypeAdapter(GradeAdapter.class)
    private Grade grade;
    @XmlElement(name = "source-grade")
    private String sourceGrade;
    @XmlElement(name = "term", namespace = "http://www.thespheres.org/xsd/betula/betula.xsd")
    private TermId term;
    @XmlElement(name = "timestamp")
    private SourceDateTime timestamp;

    public DocumentId getTarget() {
        return target;
    }

    public void setTarget(DocumentId target) {
        this.target = target;
    }

    public String getSourceGrade() {
        return sourceGrade;
    }

    public Grade getGrade() {
        return grade;
    }

    public void setGrade(Grade grade) {
        this.grade = grade;
    }

    public TermId getTerm() {
        return term;
    }

    public void setTerm(TermId term) {
        this.term = term;
    }

    public SourceDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(SourceDateTime timestamp) {
        this.timestamp = timestamp;
    }

}
