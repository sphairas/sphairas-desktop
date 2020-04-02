/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.dav;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"owner"})
@XmlRootElement(name = "lockinfo")
public class LockInfo extends LockEntry {

    @XmlElement
    protected Owner owner;

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner value) {
        this.owner = value;
    }

}
