/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.  http://www.loc.gov/mads/v2
 */
@XmlSchema(namespace = "http://www.thespheres.org/xsd/betula/term-report.xsd",
        elementFormDefault = XmlNsForm.UNQUALIFIED,
        xmlns = {
            @XmlNs(prefix = "r", namespaceURI = "http://www.thespheres.org/xsd/betula/term-report.xsd"),
            @XmlNs(prefix = "ta", namespaceURI = "http://www.thespheres.org/xsd/betula/target-assessment.xsd")
        }
)
package org.thespheres.betula.termreport.xml;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
