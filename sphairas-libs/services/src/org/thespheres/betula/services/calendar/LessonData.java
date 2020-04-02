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

    private static final long serialVersionUID = 1L;
    private LessonId lesson;
    private UnitId unit;
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
    @XmlElement(name = "course-time")
    private LessonTimeData[] times;
    private VendorData vendorData;

    public LessonData() {
    }

    public LessonData(final LessonId lesson, final UnitId unit, final Signee signee, final Marker[] subject, final LocalDate effectiveBegin, final LocalDate effectiveEnd, final String timegrid, final LessonTimeData[] times, final VendorData vendorData, final String text) {
        this.lesson = lesson;
        this.unit = unit;
        this.signee = signee;
        this.definition = subject;
        this.beginDate = effectiveBegin;
        this.endDate = effectiveEnd;
        this.message = text;
        this.times = times;
        this.vendorData = vendorData;
    }

    public LessonId getLesson() {
        return lesson;
    }

    public UnitId getUnit() {
        return unit;
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

    public String getText() {
        return message;
    }

    public LessonTimeData[] getTimes() {
        return times;
    }

    public VendorData getVendorData() {
        return vendorData;
    }

}
