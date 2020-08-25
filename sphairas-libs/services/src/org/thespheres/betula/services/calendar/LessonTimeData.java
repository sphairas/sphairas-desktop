/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.calendar;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.services.scheme.spi.PeriodId;
import org.thespheres.betula.util.LocalDateAdapter;
import org.thespheres.betula.util.LocalTimeAdapter;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement
@XmlAccessorType(value = XmlAccessType.FIELD)
public final class LessonTimeData implements Serializable {

    private static final long serialVersionUID = 1L;
    @XmlAttribute(name = "start")
    @XmlJavaTypeAdapter(LocalTimeAdapter.class)
    private LocalTime start;
    @XmlAttribute(name = "end")
    @XmlJavaTypeAdapter(LocalTimeAdapter.class)
    private LocalTime end;
    @XmlAttribute(name = "day-of-week")
    private int dayOfWeek;
    @XmlElement(name = "period")
    private PeriodId period;
    @XmlAttribute(name = "location")
    private String location;
    @XmlElement(name = "since")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate since;
    @XmlElement(name = "until")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate until;
    @XmlElement(name = "vendor-data")
    private VendorData vendorData;
    @XmlList
    @XmlElement(name = "exdates")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate[] exdates;

    public LessonTimeData() {
    }

    public LessonTimeData(final LocalTime start, final LocalTime end, final DayOfWeek day, final PeriodId period) {
        this.start = start;
        this.end = end;
        this.dayOfWeek = day.getValue();
        this.period = period;
    }

    public LocalTime getStart() {
        return start;
    }

    public LocalTime getEnd() {
        return end;
    }

    public PeriodId getPeriod() {
        return period;
    }

    public DayOfWeek getDay() {
        return DayOfWeek.of(dayOfWeek);
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(final String location) {
        this.location = location;
    }

    public LocalDate getSince() {
        return since;
    }

    public void setSince(final LocalDate since) {
        this.since = since;
    }

    public LocalDate getUntil() {
        return until;
    }

    public void setUntil(final LocalDate until) {
        this.until = until;
    }

    public VendorData getVendorData() {
        return vendorData;
    }

    public void setVendorData(VendorData vendorData) {
        this.vendorData = vendorData;
    }

    public LocalDate[] getExdates() {
        return exdates;
    }

    public void setExdates(final LocalDate[] exdates) {
        this.exdates = exdates;
    }

}
