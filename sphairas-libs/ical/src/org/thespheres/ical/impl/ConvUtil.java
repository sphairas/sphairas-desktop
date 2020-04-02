/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.ical.impl;

import java.time.DayOfWeek;
import java.time.temporal.ChronoField;
import static java.time.temporal.ChronoUnit.DAYS;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.util.Objects;

/**
 *
 * @author boris.heithecker
 */
public class ConvUtil {

    static DayOfWeek convert(int calendarInt) {
        if (calendarInt > 1 || calendarInt <= 7) {
            return DayOfWeek.of(calendarInt - 1);
        } else if (calendarInt == 1) {
            return DayOfWeek.SUNDAY;
        }
        throw new IllegalArgumentException();
    }

    public static TemporalAdjuster dayOfWeekInYear(int ordinal, DayOfWeek dayOfWeek) {
        Objects.requireNonNull(dayOfWeek, "dayOfWeek");
        int dowValue = dayOfWeek.getValue();
        if (ordinal >= 0) {
            return (temporal) -> {
                Temporal temp = temporal.with(ChronoField.DAY_OF_YEAR, 1);
                int curDow = temp.get(ChronoField.DAY_OF_WEEK);
                int dowDiff = (dowValue - curDow + 7) % 7;
                dowDiff += (ordinal - 1L) * 7L;  // safe from overflow
                return temp.plus(dowDiff, DAYS);
            };
        } else {
            return (temporal) -> {
                Temporal temp = temporal.with(ChronoField.DAY_OF_YEAR, temporal.range(ChronoField.DAY_OF_YEAR).getMaximum());
                int curDow = temp.get(ChronoField.DAY_OF_WEEK);
                int daysDiff = dowValue - curDow;
                daysDiff = daysDiff == 0 ? 0 : (daysDiff > 0 ? daysDiff - 7 : daysDiff);
                daysDiff -= (-ordinal - 1L) * 7L;  // safe from overflow
                return temp.plus(daysDiff, DAYS);
            };
        }
    }
}
