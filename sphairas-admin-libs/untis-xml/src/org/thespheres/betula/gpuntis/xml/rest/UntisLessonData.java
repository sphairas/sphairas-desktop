/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis.xml.rest;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.document.util.MarkerAdapter.XmlMarkerAdapter;
import org.thespheres.betula.gpuntis.xml.General;
import org.thespheres.betula.util.LocalDateAdapter;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class UntisLessonData implements Serializable {

    private static final long serialVersionUID = 1L;
    @XmlAttribute
    private int untisLesson;
    @XmlAttribute
    private int untisKopplung;
    private UnitId unit;
    private DocumentId targetBase;
    private Signee signee;
    private String untisTeacherName;
    @XmlJavaTypeAdapter(XmlMarkerAdapter.class)
    private Marker subject;
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate effectiveBegin;
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate effectiveEnd;
    private String untisText;
    @XmlAttribute
    private String timegrid;
    private LessonTime[] times;
    private General general;

    public UntisLessonData() {
    }

    public UntisLessonData(int untisLesson, int untisKopplung, UnitId unit, DocumentId targetBase, Signee signee, String untisTeacherName, Marker subject, LocalDate effectiveBegin, LocalDate effectiveEnd, String untisText, String timegrid, LessonTime[] times, General gen) {
        this.untisLesson = untisLesson;
        this.untisKopplung = untisKopplung;
        this.unit = unit;
        this.targetBase = targetBase;
        this.signee = signee;
        this.untisTeacherName = untisTeacherName;
        this.subject = subject;
        this.effectiveBegin = effectiveBegin;
        this.effectiveEnd = effectiveEnd;
        this.untisText = untisText;
        this.timegrid = timegrid;
        this.times = times;
        this.general = gen;
    }

    public General getGeneral() {
        return general;
    }

    public String getUntisAuthority() {
        return "untis" + Integer.toString(getGeneral().getSchoolNumber());
    }

    public int getUntisLesson() {
        return untisLesson;
    }

    public int getUntisKopplung() {
        return untisKopplung;
    }

    public UnitId getUnit() {
        return unit;
    }

    public DocumentId getTargetBase() {
        return targetBase;
    }

    public Signee getSignee() {
        return signee;
    }

    public String getUntisTeacherName() {
        return untisTeacherName;
    }

    public Marker getSubject() {
        return subject;
    }

    public LocalDate getEffectiveBegin() {
        return effectiveBegin;
    }

    public LocalDate getEffectiveEnd() {
        return effectiveEnd;
    }

    public String getUntisText() {
        return untisText;
    }

    public String getTimegrid() {
        return timegrid;
    }

    public LessonTime[] getTimes() {
        return times;
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class LessonTime implements Serializable {

        private Date start;
        private Date end;
        @XmlAttribute
        private String room;
        private ExWeeks[] exWeeks;
        @XmlAttribute
        private int untisDay;
        @XmlAttribute
        private int untisPeriod;

        public LessonTime() {
        }

        public LessonTime(Date start, Date end, String room, int untisDay, int untisPeriod, ExWeeks[] exWeeks) {
            super();
            this.start = start;
            this.end = end;
            this.room = room;
            this.exWeeks = exWeeks;
            this.untisDay = untisDay;
            this.untisPeriod = untisPeriod;
        }

        public Date getStart() {
            return start;
        }

        public Date getEnd() {
            return end;
        }

        public String getRoom() {
            return room;
        }

        public int getUntisDay() {
            return untisDay;
        }

        public int getUntisPeriod() {
            return untisPeriod;
        }

        public ExWeeks[] getExWeeks() {
            return exWeeks;
        }
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class ExWeeks implements Serializable {

        @XmlAttribute
        private int year;
        @XmlAttribute
        @XmlList
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
