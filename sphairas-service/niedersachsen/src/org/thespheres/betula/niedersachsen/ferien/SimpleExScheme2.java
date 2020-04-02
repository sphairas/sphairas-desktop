/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.ferien;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.thespheres.ical.builder.ICalendarBuilder;
import org.thespheres.betula.services.scheme.spi.ScheduleType;
import org.thespheres.betula.services.scheme.spi.ExemptDatesScheme;
import org.thespheres.ical.CalendarComponent;
import org.thespheres.ical.CalendarComponentProperty;
import org.thespheres.ical.ICalendar;
import org.thespheres.ical.InvalidComponentException;
import org.thespheres.ical.util.IComponentUtilities;

/**
 *
 * @author boris.heithecker
 */
class SimpleExScheme2 implements ExemptDatesScheme {

    private final ICalendar calendar;
    private final List<ExemptPeriod> exemptDates;
    private String displayName;
    private String name;

    SimpleExScheme2(String name, URL url) throws IOException {
        this.name = name;
        ICalendar fetch = null;
        try {
            List<ICalendar> l = ICalendarBuilder.parseCalendars(url);
            if (l.size() == 1) {
                fetch = l.get(0);
            }
        } catch (ParseException | InvalidComponentException ex) {
            throw new IOException(ex);
        }
        if ((this.calendar = fetch) == null) {
            throw new IOException("Could not load " + url);
        }
        exemptDates = calendar.getComponents().stream()
                .filter(c -> c.getName().equals(CalendarComponent.VEVENT))
                .map(ve -> {
                    Optional<LocalDate> dtStart = ve.getAnyPropertyValue(CalendarComponentProperty.DTSTART).map(s -> {
                        try {
                            return LocalDate.parse(s, IComponentUtilities.DATE_FORMATTER);
                        } catch (DateTimeParseException ex) {
                            return null;
                        }
                    });
                    Optional<LocalDate> dtEnd = ve.getAnyPropertyValue(CalendarComponentProperty.DTEND).map(s -> {
                        try {
                            return LocalDate.parse(s, IComponentUtilities.DATE_FORMATTER);
                        } catch (DateTimeParseException ex) {
                            return null;
                        }
                    });
                    if (dtStart.isPresent() && dtEnd.isPresent()) {
                        return new ExemptPeriod(dtStart.get(), dtEnd.get());
                    }
                    return null;

                })
                .collect(Collectors.toList());
    }

    @Override
    public ScheduleType getType() {
        return new ScheduleType(ExemptDatesScheme.HOLIDAYS);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return displayName != null ? displayName : getName();
    }

    @Override
    public boolean isExemptDate(LocalDate date) {
        return exemptDates.stream()
                .anyMatch(h -> h.includes(date));
    }

//    @Override
//    public boolean isExDate(Date date) {
//        return holidays.stream().anyMatch(h -> h.includes(date));
//    }
//
//    @Override
//    public Date getNextExAfter(Date beforeEx) {
//        Holidays ret = null;
//        for (Holidays p : holidays) {
//            if (p.start.after(beforeEx)) {
//                if (ret == null || ret.start.after(p.start)) {
//                    ret = p;
//                }
//            }
//        }
//        return ret != null ? ret.start : null;
//    }
//
//    @Override
//    public Date getUntil(Date exDate) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
    private static class ExemptPeriod {

        private final LocalDate start;
        private final LocalDate end;

        private ExemptPeriod(LocalDate start, LocalDate end) {
            this.start = start;
            this.end = end;
        }

        private boolean includes(LocalDate date) {
            return date.isAfter(start) && date.isBefore(end);
        }

    }
}
