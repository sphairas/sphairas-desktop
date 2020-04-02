/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSeeAlso;
import org.thespheres.betula.xmlimport.ImportTargetsItem;

/**
 *
 * @author boris.heithecker
 */
@XmlSeeAlso(XmlPatternTargetSelector.class)
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class XmlTargetSelector {

    @XmlID
    @XmlAttribute(name = "name", required = true)
    private String id;

    public XmlTargetSelector() {
    }

    protected XmlTargetSelector(String id) {
        this.id = id;
    }

    public abstract boolean applies(ImportTargetsItem base);
}
