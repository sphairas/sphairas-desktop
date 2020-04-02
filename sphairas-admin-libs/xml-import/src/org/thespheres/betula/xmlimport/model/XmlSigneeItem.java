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
import org.thespheres.betula.document.Signee;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "signee-item") //, namespace = "http://www.thespheres.org/xsd/betula/xml-import.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlSigneeItem extends XmlItem {

    @XmlElement(name = "signee", namespace = "http://www.thespheres.org/xsd/betula/betula.xsd")
    protected Signee signee;
    @XmlElement(name = "source-name")
    protected String sourceName;
    @XmlElement(name = "source-given-names")
    protected String sourceGivenNames;
    @XmlElement(name = "source-gender")
    protected String gender;
    @XmlElement(name = "vCard")
    protected String vCard;
    @XmlList
    @XmlElement(name = "-source-primary-unit")
    protected String[] primaryUnit;
    @XmlElement(name = "source-value")
    protected SourceElement[] source;

    public Signee getSignee() {
        return signee;
    }

    public void setSignee(Signee signee) {
        this.signee = signee;
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
}
