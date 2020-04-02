/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.xml.fi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class XmlFileInfoElement {

    @XmlAttribute(name = "file-info-element-name")
    private String name;

    public XmlFileInfoElement() {
    }

    protected XmlFileInfoElement(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
