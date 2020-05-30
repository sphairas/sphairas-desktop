/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.calendar;

import java.io.Serializable;
import java.time.LocalDate;
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
import org.thespheres.betula.util.LocalDateAdapter;

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
    @XmlElement(name = "course-begin")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate beginDate;
    @XmlElement(name = "course-end")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate endDate;
    private String message;
    @XmlElementWrapper(name = "course-times")
    @XmlElement(name = "course-times")
    private LessonTimeData[] times;
    @XmlElement(name = "vendor-data")
    private VendorData vendorData;

    public LessonData() {
    }

    public LessonData(final LessonId lesson, final String method, final UnitId[] units, final Signee signee, final Marker[] subject, final LocalDate effectiveBegin, final LocalDate effectiveEnd, final LessonTimeData[] times) {
        this.lesson = lesson;
        this.method = method;
        this.units = units;
        this.signee = signee;
        this.definition = subject;
        this.beginDate = effectiveBegin;
        this.endDate = effectiveEnd;
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

    public LocalDate getCourseBegin() {
        return beginDate;
    }

    public LocalDate getCourseEnd() {
        return endDate;
    }

    public LessonTimeData[] getTimes() {
        return times;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public VendorData getVendorData() {
        return vendorData;
    }

    public void setVendorData(VendorData vendorData) {
        this.vendorData = vendorData;
    }

}
