/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
@XmlSchema(namespace = "http://www.thespheres.org/xsd/niedersachsen/zeugnisse.xsd",
        elementFormDefault = XmlNsForm.QUALIFIED,//must be qualified for xslt to xsl-fo to work
        xmlns = {
            @XmlNs(prefix = XMLConstants.DEFAULT_NS_PREFIX, namespaceURI = "http://www.thespheres.org/xsd/niedersachsen/zeugnisse.xsd"),
            @XmlNs(prefix = "b", namespaceURI = "http://www.thespheres.org/xsd/betula/betula.xsd")
        })
package org.thespheres.betula.niedersachsen.gs;

import javax.xml.XMLConstants;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
