/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.calendar;

import java.io.Serializable;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.services.scheme.spi.LessonId;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement
@XmlAccessorType(value = XmlAccessType.FIELD)
public class VendorData implements Serializable {

    @XmlElement(name = "vendor-lesson")
    private LessonId vendorLesson;
    @XmlAttribute(name = "vendor-link")
    private Integer vendorLink;
    @XmlElement(name = "signee-name")
    @XmlJavaTypeAdapter(value = CollapsedStringAdapter.class)
    private String signeeName;

    public VendorData() {
    }

    public VendorData(final LessonId vendorId, final Integer vendorLink, final String vendorSigneeName) {
        this.vendorLesson = vendorId;
        this.vendorLink = vendorLink;
        this.signeeName = vendorSigneeName;
    }

    public LessonId getVendorLesson() {
        return vendorLesson;
    }

    public Integer getVendorLink() {
        return vendorLink != null ? vendorLink : 0;
    }

    public String getSigneeName() {
        return signeeName;
    }

}
