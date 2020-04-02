/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.validation.impl;

import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSeeAlso;
import org.thespheres.betula.document.model.Subject;
import org.thespheres.betula.validation.impl.ListSubjectGroup.DefaultSubjectGroup;

/**
 *
 * @author boris.heithecker
 */
@XmlSeeAlso({ListSubjectGroup.class, DefaultSubjectGroup.class})
@XmlAccessorType(value = XmlAccessType.FIELD)
public abstract class SubjectFilter {

    public static final SubjectFilter DEFAULT = new ListSubjectGroup.DefaultSubjectGroup();
    @XmlID
    @XmlAttribute(name = "name")
    protected String name;

    protected SubjectFilter(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    public abstract String getDisplayName();

    public Set<Subject> filter(Set<Subject> from) {
        return from.stream()
                .filter(this::matches)
                .collect(Collectors.toSet());
    }

    public abstract boolean matches(Subject subject);

}
