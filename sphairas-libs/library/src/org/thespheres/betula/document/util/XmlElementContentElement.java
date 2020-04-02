/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document.util;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;

/**
 *
 * @author boris.heithecker
 */
@XmlAccessorType(value = XmlAccessType.FIELD)
final class XmlElementContentElement implements XmlContentElement<org.w3c.dom.Element> {

    @XmlAttribute(name = "key")
    protected String key;
    @XmlAnyElement(lax = false)
    protected org.w3c.dom.Element node;

    public XmlElementContentElement() {
    }

    XmlElementContentElement(String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public org.w3c.dom.Element getNode() {
        return node;
    }

    @Override
    public void setNode(org.w3c.dom.Element n) {
        this.node = n;
    }

}
