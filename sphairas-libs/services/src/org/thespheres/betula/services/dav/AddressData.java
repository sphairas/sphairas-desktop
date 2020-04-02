/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.dav;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlValue;

/**
 *
 * @author boris.heithecker
 */
//@XmlType(namespace = "urn:ietf:params:xml:ns:carddav")
@XmlAccessorType(XmlAccessType.FIELD)
public class AddressData {

    @XmlValue
    private String value;

    public AddressData() {
    }

    public AddressData(String v) {
        value = v;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
