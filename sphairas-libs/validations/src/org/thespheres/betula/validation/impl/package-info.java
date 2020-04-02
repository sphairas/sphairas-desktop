/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
@XmlSchema(namespace = "http://www.thespheres.org/xsd/niedersachsen/versetzung.xsd",
        elementFormDefault = XmlNsForm.UNQUALIFIED,
        xmlns = {
            @XmlNs(prefix = "c", namespaceURI = "http://www.thespheres.org/xsd/betula/container.xsd"),
            @XmlNs(prefix = "b", namespaceURI = "http://www.thespheres.org/xsd/betula/betula.xsd"),
            @XmlNs(prefix = "v", namespaceURI = "http://www.thespheres.org/xsd/niedersachsen/versetzung.xsd")
        }
)
package org.thespheres.betula.validation.impl;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
