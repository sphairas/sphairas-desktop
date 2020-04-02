/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.kgs.ui.imports;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerFactory;
import org.thespheres.betula.ui.util.JAXBUtil.JAXBRegistration;
import org.thespheres.betula.xmlimport.utilities.ColumnProperty;

/**
 *
 * @author boris.heithecker
 */
@JAXBRegistration(target = "SiBankSourceTargetAccess")
@XmlRootElement(name = "kursart")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class KursartOverride extends ColumnProperty {

    @XmlAttribute(name = "convention", required = true)
    private String convention;
    @XmlAttribute(name = "id", required = true)
    private String id;
    @XmlAttribute(name = "subset")
    private String subset = null;

    public KursartOverride() {
    }

    public KursartOverride(Marker marker) {
        this.convention = marker.getConvention();
        this.id = marker.getId();
        this.subset = marker.getSubset();
    }

    @Override
    public String getColumnId() {
        return "kursart";
    }

    public Marker getMarker() {
        return MarkerFactory.find(convention, id, subset);
    }

}
