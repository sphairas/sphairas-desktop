/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document.util;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "content-value", namespace = "http://www.thespheres.org/xsd/betula/container.xsd")
@XmlType(name = "xmlContentValueType", namespace = "http://www.thespheres.org/xsd/betula/container.xsd")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class XmlContentValue extends XmlContent implements Content, Serializable {

    private static final long serialVersionUID = 1L;
    public static final String VALUE = "value";

    public XmlContentValue() {
    }

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    XmlContentValue(String content) {
        setContent(VALUE, content);
    }

}
