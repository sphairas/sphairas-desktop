/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.calendar;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.document.util.MarkerAdapter.XmlMarkerAdapter;
import org.thespheres.betula.services.scheme.spi.LessonId;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class LessonData implements Serializable {

    public static final String METHOD_PUBLISH = "PUBLISH";
    public static final String METHOD_UPDATE = "UPDATE";
    private static final long serialVersionUID = 1L;
    @XmlAttribute(name = "method")
    private String method;
    @XmlElement(name = "lesson", required = true)
    private LessonId lesson;
    @XmlElement(name = "unit")
    private UnitId[] units;
    private Signee signee;
    @XmlElementWrapper(name = "subject")
    @XmlJavaTypeAdapter(XmlMarkerAdapter.class)
    private Marker[] definition;
    @XmlElementWrapper(name = "course-times")
    @XmlElement(name = "course-times")
    private LessonTimeData[] times;
    @XmlElement(name = "vendor-data")
    private VendorData vendorData;

    public LessonData() {
    }

    public LessonData(final LessonId lesson, final String method, final UnitId[] units, final Signee signee, final Marker[] subject, final LessonTimeData[] times) {
        this.lesson = lesson;
        this.method = method;
        this.units = units;
        this.signee = signee;
        this.definition = subject;
        this.times = times;
    }

    public LessonId getLesson() {
        return lesson;
    }

    public String getMethod() {
        return method;
    }

    public UnitId[] getUnits() {
        return units;
    }

    public Signee getSignee() {
        return signee;
    }

    public Marker[] getSubject() {
        return definition;
    }

    public LessonTimeData[] getTimes() {
        return times;
    }

    public VendorData getVendorData() {
        return vendorData;
    }

    public void setVendorData(VendorData vendorData) {
        this.vendorData = vendorData;
    }

}
