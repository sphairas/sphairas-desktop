/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.impl;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 *
 * @author boris.heithecker
 */
@XmlSeeAlso(XmlProviderInfoEntry.class)
@XmlRootElement(name = "provider-registry")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlRegistry {

    @XmlElementWrapper(name = "entries")
    @XmlElementRef
    private final List<XmlProviderInfoEntry> entries = new ArrayList<>();

    public List<XmlProviderInfoEntry> entries() {
        return entries;
    }

}
