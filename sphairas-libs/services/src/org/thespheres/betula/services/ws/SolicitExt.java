/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.thespheres.betula.document.Container;

/**
 *
 * @author boris.heithecker
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "solicit", propOrder = {
    "container"
})
public class SolicitExt {

    @XmlElement(namespace = "http://www.thespheres.org/xsd/betula/container.xsd")
    protected Container container;

    public Container getContainer() {
        return container;
    }

    public void setContainer(Container value) {
        this.container = value;
    }
}
