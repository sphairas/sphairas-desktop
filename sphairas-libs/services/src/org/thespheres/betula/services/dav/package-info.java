@XmlSchema(namespace = "DAV:",
        elementFormDefault = XmlNsForm.QUALIFIED,
        xmlns = {
            @XmlNs(prefix = "C", namespaceURI = "urn:ietf:params:xml:ns:carddav"),
            @XmlNs(prefix = "D", namespaceURI = "DAV:")})
package org.thespheres.betula.services.dav;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
