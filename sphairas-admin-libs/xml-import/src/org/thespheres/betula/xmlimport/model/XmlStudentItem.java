/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import org.thespheres.betula.StudentId;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "student-item") //, namespace = "http://www.thespheres.org/xsd/betula/xml-import.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlStudentItem extends XmlItem {

    @XmlElement(name = "student", namespace = "http://www.thespheres.org/xsd/betula/betula.xsd")
    protected StudentId student;
    @XmlElement(name = "source-name")
    protected String sourceName;
    @XmlElement(name = "source-given-names")
    protected String sourceGivenNames;
    @XmlElement(name = "source-gender")
    protected String gender;
    @XmlElement(name = "vCard")
    protected String vCard;
    @XmlElement(name = "date-of-birth")
    protected SourceDateTime dateOfBirth;
    @XmlElement(name = "place-of-birth")
    protected String placeOfBirth;
    @XmlList
    @XmlElement(name = "source-unit")
    protected String[] primaryUnit;
    @XmlElement(name = "source-value")
    protected SourceElement[] source;

    public StudentId getStudent() {
        return student;
    }

    public void setStudent(StudentId student) {
        this.student = student;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getSourceGivenNames() {
        return sourceGivenNames;
    }

    public String getSourceGender() {
        return gender;
    }

    public String getVCard() {
        return vCard;
    }

    public void setvCard(String vCard) {
        this.vCard = vCard;
    }

    public SourceDateTime getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(SourceDateTime dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPlaceOfBirth() {
        return placeOfBirth;
    }

    public void setPlaceOfBirth(String placeOfBirth) {
        this.placeOfBirth = placeOfBirth;
    }

    public String[] getPrimaryUnit() {
        return primaryUnit;
    }

    public void setPrimaryUnit(String[] primaryUnit) {
        this.primaryUnit = primaryUnit;
    }

    public SourceElement[] getSource() {
        return source;
    }

    public void setSource(SourceElement[] source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return sourceName + " (" + (dateOfBirth == null ? "unknown" : dateOfBirth) + ")";
    }

}
