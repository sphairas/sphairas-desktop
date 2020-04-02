/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.project.local.timetbl;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.services.scheme.spi.LessonId;
import org.thespheres.ical.builder.ICalendarBuilder;
import org.thespheres.ical.builder.ICalendarBuilder.CalendarComponentBuilder;
import org.thespheres.betula.services.scheme.spi.Period;
import org.thespheres.betula.services.scheme.spi.PeriodId;
import org.thespheres.betula.util.LocalDateAdapter;
import org.thespheres.ical.CalendarComponent;
import org.thespheres.ical.CalendarComponentProperty;
import org.thespheres.ical.InvalidComponentException;
import org.thespheres.ical.Parameter;
import org.thespheres.ical.UID;
import org.thespheres.ical.util.IComponentUtilities;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "local-timetable")
@XmlType(namespace = "http://www.thespheres.org/xsd/betula/local-timetable.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class LocalTimetable {

    public static final String TIMETABLE_FILE = "local-timetable.xml";
    @XmlAttribute(name = "begin")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate begin;
    @XmlAttribute(name = "end")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate end;
    @XmlElementWrapper(name = "lessons")
    @XmlElement(name = "lesson")
    private Set<Lesson> lessons = new HashSet<>();

    public LocalTimetable() {
    }

    LocalTimetable(LocalDate begin, LocalDate end, Set<Lesson> periods) {
        if (end.isBefore(begin)) {
            throw new IllegalArgumentException("end before begin");
        }
        this.begin = begin;
        this.end = end;
        this.lessons = periods;
    }

    LocalDate getStart() {
        return begin;
    }

    void setStart(LocalDate begin) {
        this.begin = begin;
    }

    LocalDate getEnd() {
        return end;
    }

    void setEnd(LocalDate end) {
        this.end = end;
    }

    Set<Lesson> getLessons() {
        return lessons;
    }

    void toCalendar(final LocalClassSchedule schedule, final UnitId unit, final LessonId lesson, final ICalendarBuilder cb) throws IOException {
//        ICalendarBuilder cb = new ICalendarBuilder();
//        try {
//            cb.addProperty(CalendarComponent.PRODID, "local");
//            cb.addProperty(CalendarComponent.VERSION, "2.0");
//        } catch (InvalidComponentException ex) {
//        }
        final Iterator<CalendarComponentBuilder> it = cb.iterator();
        components:
        while (it.hasNext()) {
            ICalendarBuilder.PropertyIterator pi = it.next().iterator();
            while (pi.hasNext()) {
                final CalendarComponentProperty ccp = pi.next();
                if (ccp.getName().equals(CalendarComponentProperty.CATEGORIES)) {
                    if (Arrays.stream(ccp.getValue().split(",")).anyMatch("local-timetable"::equals)) {
                        it.remove();
                        continue components;
                    }
                }
            }
        }
        if (lessons != null && begin != null && end != null) {
            for (Lesson l : lessons) {
                List<Period> i = schedule.inflate(begin.atStartOfDay(), end.plusDays(1l).atStartOfDay(), schedule.getTimes().size());
                if (!i.isEmpty()) {
                    Period p = i.stream().filter(pe -> pe.getScheduledItemId().equals(l.getPeriod())).findAny().get();
                    LocalDateTime startLesson = p.resolveStart().with(TemporalAdjusters.nextOrSame(l.getDay()));
                    LocalDateTime endLesson = p.resolveEnd().with(TemporalAdjusters.nextOrSame(l.getDay()));
                    try {
                        addComponent(cb, startLesson, endLesson, p.getScheduledItemId(), unit, lesson);
                    } catch (InvalidComponentException ex) {
                        throw new IOException(ex);
//                        Logger.getLogger(LocalTimetable.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                    }
                }
            }
        }
//        return cb.toICalendar();
    }

    private void addComponent(ICalendarBuilder cb, LocalDateTime start, LocalDateTime endLesson, PeriodId periodId, UnitId unit, LessonId lesson) throws IOException, InvalidComponentException {
        CalendarComponentBuilder ccb = cb.addComponent(CalendarComponent.VEVENT, UID.create());
        ccb.addProperty(CalendarComponentProperty.DTSTART, IComponentUtilities.DATETIME_FORMATTER.format(start));
        ccb.addProperty(CalendarComponentProperty.DTEND, IComponentUtilities.DATETIME_FORMATTER.format(endLesson));
        ccb.addProperty(CalendarComponentProperty.RRULE, "FREQ=WEEKLY;UNTIL=" + IComponentUtilities.DATETIME_FORMATTER.format(end.plusDays(1l).atStartOfDay()));
        ccb.addProperty(CalendarComponentProperty.CATEGORIES, "regular,local-timetable");
        ccb.addProperty("X-PERIOD", Integer.toString(periodId.getId()), new Parameter("x-period-authority", periodId.getAuthority()), new Parameter("x-period-version", periodId.getVersion().getVersion()));
        ccb.addProperty("X-UNIT", unit.getId(), new Parameter("x-authority", unit.getAuthority()));
        if (lesson != null) {
            ccb.addProperty("X-TARGET-BASE", lesson.getId(), new Parameter("x-authority", lesson.getAuthority()));
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static class Lesson {

        @XmlElement(name = "period")
        private PeriodId period;
        @XmlElement(name = "day")
        private DayOfWeek day;

        public Lesson() {
        }

        Lesson(PeriodId period, DayOfWeek day) {
            this.period = period;
            this.day = day;
        }

        PeriodId getPeriod() {
            return period;
        }

        DayOfWeek getDay() {
            return day;
        }

    }
}
