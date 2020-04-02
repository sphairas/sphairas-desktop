/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.project.local.timetbl;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.services.scheme.spi.ClassSchedule;
import org.thespheres.betula.services.scheme.spi.Period;
import org.thespheres.betula.services.scheme.spi.PeriodId;
import org.thespheres.betula.services.scheme.spi.ScheduleType;

/**
 *
 * @author boris.heithecker
 */
@Messages({"LocalClassSchedule.displayName=Stundenschema"})
@XmlRootElement(name = "local-class-schedule")
@XmlType(namespace = "http://www.thespheres.org/xsd/betula/local-class-schedule.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class LocalClassSchedule implements ClassSchedule {

    public static final String CLASS_SCHEDULE_FILE = "local-class-schedule.xml";
    @XmlAttribute(name = "type")
    private String type;
    @XmlAttribute(name = "name")
    private String name;
    @XmlElement(name = "display-name")
    private final String displayName;
    @XmlElementWrapper(name = "times")
    @XmlElement(name = "time")
    private final ArrayList<Time> times = new ArrayList<>();

    public LocalClassSchedule() {
        name = "local-class-schedule";
        displayName = NbBundle.getMessage(LocalClassSchedule.class, "LocalClassSchedule.displayName");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    private String getScheduleType() {
        return type != null ? type : "regular";
    }

    @Override
    public ScheduleType getType() {
        return new ScheduleType(getScheduleType());
    }

    ArrayList<Time> getTimes() {
        return times;
    }

    @Override
    public List<Period> inflate(LocalDateTime start, LocalDateTime end, int maxResults) {
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("end must be after start.");
        }
        final ArrayList<Period> ret = new ArrayList<>();
        loop:
        while (true) {
            for (Time t : times) {
                if (t.getBegin() != null && t.getEnd() != null) {
                    LocalDateTime b = start.with(t.getBegin());
                    LocalDateTime e = start.with(t.getEnd());
                    if (end.isBefore(e) || (maxResults >= 0 && ret.size() >= maxResults)) {
                        break loop;
                    }
                    PeriodId pid = new PeriodId("local", t.getPeriod(), PeriodId.Version.UNSPECIFIED);
                    LocalPeriod lp = new LocalPeriod(b, e, pid);
                    ret.add(lp);
                }
            }
        }
        return ret;
    }

    @Override
    public Period findResolved(LocalDateTime start) {
        for (Time t : times) {
            LocalDateTime b = start.with(t.getBegin());
            if (b.isEqual(start)) {
                PeriodId pid = new PeriodId("local", t.getPeriod(), PeriodId.Version.UNSPECIFIED);
                LocalDateTime e = start.with(t.getEnd());
                return new LocalPeriod(b, e, pid);
            }
        }
        return null;
    }

    public boolean validate() {
        Time before = null;
        synchronized (times) {
            for (int i = 0; i < times.size(); i++) {
                Time t = times.get(i);
                if (!t.isValid()) {
                    return false;
                }
                if (before != null && t.getBegin().isBefore(before.getEnd())) {
                    return false;
                }
                before = t;
            }
        }
        return true;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static class Time {

        @XmlAttribute(name = "begin", required = true)
        @XmlJavaTypeAdapter(LocalTimeAdapter.class)
        private LocalTime begin;
        @XmlAttribute(name = "end", required = true)
        @XmlJavaTypeAdapter(LocalTimeAdapter.class)
        private LocalTime end;
        @XmlAttribute(name = "period", required = true)
        private int period;

        public Time() {
        }

        Time(LocalTime begin, LocalTime end, int period) {
            this.begin = begin;
            this.end = end;
            this.period = period;
        }

        public LocalTime getBegin() {
            return begin;
        }

        public void setBegin(LocalTime begin) {
            this.begin = begin;
        }

        public LocalTime getEnd() {
            return end;
        }

        public void setEnd(LocalTime end) {
            this.end = end;
        }

        public int getPeriod() {
            return period;
        }

        public void setPeriod(int period) {
            this.period = period;
        }

        boolean isValid() {
            return period >= 0
                    && begin != null && end != null
                    && end.isAfter(begin);
        }
    }
}
