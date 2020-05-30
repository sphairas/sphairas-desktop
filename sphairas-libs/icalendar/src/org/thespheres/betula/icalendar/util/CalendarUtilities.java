/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.icalendar.util;

import java.util.Optional;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.services.scheme.spi.LessonId;
import org.thespheres.betula.services.scheme.spi.PeriodId;
import org.thespheres.ical.CalendarComponent;
import org.thespheres.ical.CalendarComponentProperty;
import org.thespheres.ical.ICalendar;

/**
 *
 * @author boris.heithecker
 */
public class CalendarUtilities {

    public static UnitId extractUnitIdFromCalendarComponent(final CalendarComponent cc) {
        return Optional.ofNullable(cc.getAnyProperty("X-UNIT")).flatMap((CalendarComponentProperty unitProp) -> unitProp.getAnyParameter("x-authority").map((String au) -> new UnitId(au, unitProp.getValue()))).orElse(null);
    }

    public static DocumentId extractCalendarIdFromICalendar(final ICalendar cc) {
        return Optional.ofNullable(cc.getAnyProperty("X-CALENDAR-ID")).flatMap((CalendarComponentProperty unitProp) -> unitProp.getAnyParameter("x-calendar-authority").flatMap((String auth) -> unitProp.getAnyParameter("x-calendar-version").map((String v) -> new DocumentId(auth, unitProp.getValue(), DocumentId.Version.parse(v))))).orElse(null);
    }

    public static LessonId extractLessonIdFromCalendarComponent(final CalendarComponent cc) {
        return Optional.ofNullable(cc.getAnyProperty("X-LESSON"))
                .flatMap(p -> p.getAnyParameter("x-authority")
                .map(a -> new LessonId(a, p.getValue())))
                .orElse(null);
    }

    public static PeriodId extractPeriodIdFromCalendarComponent(final CalendarComponent cc) {
        return Optional.ofNullable(cc.getAnyProperty("X-PERIOD"))
                .flatMap((CalendarComponentProperty unitProp) -> unitProp.getAnyParameter("x-authority")
                        .flatMap((String auth) -> unitProp.getAnyParameter("x-version")
                                .map((String v) -> new PeriodId(auth, Integer.parseInt(unitProp.getValue()), PeriodId.Version.parse(v)))))
                .orElse(null);
    }

}
