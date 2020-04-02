/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.  
 */
@XmlSchema(namespace = "http://www.thespheres.org/xsd/betula/convention.xsd",
        elementFormDefault = XmlNsForm.UNQUALIFIED,
        xmlns = {
            @XmlNs(prefix = "c", namespaceURI = "http://www.thespheres.org/xsd/betula/convention.xsd"),
            @XmlNs(prefix = "b", namespaceURI = "http://www.thespheres.org/xsd/betula/betula.xsd"),
        }
)
package org.thespheres.betula.xmldefinitions;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
