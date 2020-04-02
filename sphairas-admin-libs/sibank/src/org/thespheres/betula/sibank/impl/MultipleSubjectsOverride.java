/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.sibank.impl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.util.MarkerAdapter.XmlMarkerAdapter;
import org.thespheres.betula.ui.util.JAXBUtil.JAXBRegistration;
import org.thespheres.betula.xmlimport.utilities.ColumnProperty;

/**
 *
 * @author boris.heithecker
 */
@JAXBRegistration(target = "SiBankSourceTargetAccess")
@XmlRootElement(name = "subjects")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class MultipleSubjectsOverride extends ColumnProperty {

    public static final String COLUMNID_MULTIPLESUBJECTS = "subjects";

    @XmlJavaTypeAdapter(XmlMarkerAdapter.class)
    @XmlElement(name = "subject")
    private Marker[] subjects;

    public MultipleSubjectsOverride() {
    }

    public MultipleSubjectsOverride(Marker[] marker) {
        this.subjects = marker;
    }

    @Override
    public String getColumnId() {
        return COLUMNID_MULTIPLESUBJECTS;
    }

    public Marker[] getSubjects() {
        return subjects;
    }

}
