/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.calendar;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalTime;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.services.scheme.spi.PeriodId;
import org.thespheres.betula.util.LocalTimeAdapter;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement
@XmlAccessorType(value = XmlAccessType.FIELD)
public final class LessonTimeData implements Serializable {

    @XmlAttribute(name = "start")
    @XmlJavaTypeAdapter(LocalTimeAdapter.class)
    private LocalTime start;
    @XmlAttribute(name = "end")
    @XmlJavaTypeAdapter(LocalTimeAdapter.class)
    private LocalTime end;
    @XmlAttribute(name = "day-of-week")
    private int dayOfWeek;
    @XmlAttribute(name = "time-grid")
    private String timeGrid;
    @XmlAttribute(name = "room")
    private String room;
    @XmlElement(name = "period")
    private PeriodId period;
    @XmlElement(name = "ex-weeks")
    private LessonTimeData.ExWeeks[] exWeeks;

    public LessonTimeData() {
    }

    public LessonTimeData(final LocalTime start, final LocalTime end, final DayOfWeek day, final String timeGrid, final PeriodId period, final LessonTimeData.ExWeeks[] exWeeks, final String room) {
        this.start = start;
        this.end = end;
        this.room = room;
        this.exWeeks = exWeeks;
        this.dayOfWeek = day.getValue();
        this.timeGrid = timeGrid;
        this.period = period;
    }

    public LocalTime getStart() {
        return start;
    }

    public LocalTime getEnd() {
        return end;
    }

    public DayOfWeek getDay() {
        return DayOfWeek.of(dayOfWeek);
    }

    public String getTimeGrid() {
        return timeGrid;
    }

    public PeriodId getPeriod() {
        return period;
    }

    public LessonTimeData.ExWeeks[] getExWeeks() {
        return exWeeks;
    }

    public String getRoom() {
        return room;
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class ExWeeks implements Serializable {

        @XmlAttribute(name = "year")
        private int year;
        @XmlList
        @XmlAttribute(name = "weeks")
        private int[] exWeeks;

        public ExWeeks() {
        }

        public ExWeeks(int year, int[] exWeeks) {
            this.year = year;
            this.exWeeks = exWeeks;
        }

        public int getYear() {
            return year;
        }

        public int[] getExWeeks() {
            return exWeeks;
        }

        public void setExWeeks(int[] exWeeks) {
            this.exWeeks = exWeeks;
        }

    }
}
