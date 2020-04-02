/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmldefinitions;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.Convention;
import org.thespheres.betula.Tag;

/**
 *
 * @author boris.heithecker
 * @param <T>
 */
public abstract class AbstractXmlConvention<T extends Tag> implements Convention<T> {

    @XmlAttribute(name = "name")
    protected String name;
    @XmlElement(name = "display-name")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String displayName;

    protected AbstractXmlConvention() {
    }

    protected AbstractXmlConvention(String name) {
        this.name = name;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return displayName != null ? displayName : name;
    }

    @Override
    public abstract T find(final String id);

}
