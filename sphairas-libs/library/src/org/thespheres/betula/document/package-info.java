@XmlSchema(namespace = "http://www.thespheres.org/xsd/betula/container.xsd",
        elementFormDefault = XmlNsForm.UNQUALIFIED,
        xmlns = {
            @XmlNs(prefix = "c", namespaceURI = "http://www.thespheres.org/xsd/betula/container.xsd"),
            @XmlNs(prefix = "b", namespaceURI = "http://www.thespheres.org/xsd/betula/betula.xsd"),
            @XmlNs(prefix = "mads", namespaceURI = "http://www.loc.gov/mads/v2"),
            @XmlNs(prefix = "ta", namespaceURI = "http://www.thespheres.org/xsd/betula/target-assessment.xsd")
        }
)
package org.thespheres.betula.document;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
