/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.scheme.spi;

import java.time.LocalDateTime;
import java.util.Calendar;

/**
 *
 * @author boris.heithecker
 */
public interface Period extends ScheduledItem<PeriodId, ClassSchedule> {//TODO: wie Term, PeriodId

    public static final int RECURRENCE_DAILY = 1;
    public static final int RECURRENCE_WEEKLY = 2;
    public static final int RECURRENCE_MONTHLY = 4;
    public static final int RECURRENCE_YEARLY = 8;

    @Deprecated
    default public Calendar getBegin() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Deprecated
    default public Calendar getEnd() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Deprecated
    default public String format(int calendarStyle) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Deprecated
    default public String formatTime() {
        throw new UnsupportedOperationException("Not supported.");
    }

    //optional!! -1 = not supported
    public int getRecurrence();

    public LocalDateTime resolveStart();

    public LocalDateTime resolveEnd();
}
