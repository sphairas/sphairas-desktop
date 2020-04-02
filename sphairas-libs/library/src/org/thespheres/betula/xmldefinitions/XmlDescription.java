/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmldefinitions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 * @author boris.heithecker
 */
@XmlAccessorType(XmlAccessType.FIELD)
public final class XmlDescription {

    @XmlAttribute(name = "lang")
    private String languageAttribute;
    @XmlValue
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String text;

    //JAXB
    public XmlDescription() {
    }

    public XmlDescription(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLanguageAttribute() {
        return languageAttribute;
    }

    public void setLanguageAttribute(String languageAttribute) {
        this.languageAttribute = languageAttribute;
    }

}
