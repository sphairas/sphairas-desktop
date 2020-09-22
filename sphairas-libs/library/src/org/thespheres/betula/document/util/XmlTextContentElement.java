/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document.util;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 *
 * @author boris.heithecker
 */
//@XmlSeeAlso(value = XmlTextContentElement.StringValue.class)
@XmlAccessorType(value = XmlAccessType.FIELD)
final class XmlTextContentElement implements XmlContentElement<String>, Serializable {

    private static final long serialVersionUID = 1L;
    @XmlAttribute(name = "key")
    protected String key;
    @XmlValue
    protected String node;

    XmlTextContentElement() {
    }

    XmlTextContentElement(String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getNode() {
        return node;
    }

    @Override
    public void setNode(String n) {
        this.node = n;
    }

}
