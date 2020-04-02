/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
@XmlSchema(namespace = "http://www.thespheres.org/xsd/betula/xml-import.xsd",
//        elementFormDefault = XmlNsForm.QUALIFIED,
        elementFormDefault = XmlNsForm.UNQUALIFIED,
        xmlns = {
//            @XmlNs(prefix = XMLConstants.DEFAULT_NS_PREFIX, namespaceURI = "http://www.thespheres.org/xsd/betula/xml-import.xsd"),
            @XmlNs(prefix = "b", namespaceURI = "http://www.thespheres.org/xsd/betula/betula.xsd"),
            @XmlNs(prefix = "ts", namespaceURI = "http://www.thespheres.org/xsd/betula/target-import-settings.xsd")
        })
package org.thespheres.betula.xmlimport.model;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
