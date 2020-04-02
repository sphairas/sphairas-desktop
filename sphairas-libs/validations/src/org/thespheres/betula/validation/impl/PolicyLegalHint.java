/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.validation.impl;

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
@XmlAccessorType(value = XmlAccessType.FIELD)
public class PolicyLegalHint {

    @XmlAttribute(name = "bundle")
    private String bundle;
    @XmlValue
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String text;

    public PolicyLegalHint() {
    }

    public String getBundleKey() {
        return bundle;
    }

    public void setBundle(String bundle) {
        this.bundle = bundle;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
