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
@XmlType(name = "solicitResponse", propOrder = {
    "_return"
})
public class SolicitResponseExt {

    @XmlElement(name = "return", namespace = "http://www.thespheres.org/xsd/betula/container.xsd")
    protected Container _return;

    public Container getReturn() {
        return _return;
    }

    public void setReturn(Container value) {
        this._return = value;
    }
}
