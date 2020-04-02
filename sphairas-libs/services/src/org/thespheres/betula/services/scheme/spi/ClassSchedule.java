/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.scheme.spi;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;

/**
 *
 * @author boris.heithecker
 */
public interface ClassSchedule extends Schedule {

    @Deprecated
    public default Period getCurrentPeriod(Calendar time) {
        throw new UnsupportedOperationException("Deprecated");
    }
    //TODO List<Occurrence> inflate(LocalDateTime start , LDT end, int maxResult); where Occurrence: resolveStart Time, end Time, display Name, PeriodId (PeriodId: authority (untis periode), version, integer: id, recurrence (optional)>
    //Occurrence occurrenceOf(LDT start, PeriodId (optional))
    //oder Period als 

    @Deprecated
    public default Period getNextPeriod(Calendar time) {
        throw new UnsupportedOperationException("Deprecated");
    }

    @Deprecated
    public default Period getPeriod(Calendar time, int skip) {
        throw new UnsupportedOperationException("Deprecated");
    }

    @Deprecated
    public default int getNumPeriods(Calendar begin, Calendar end, boolean keepFirst, boolean keepLast) {
        throw new UnsupportedOperationException("Deprecated");
    }

    public List<Period> inflate(LocalDateTime start, LocalDateTime end, int maxResults);

    public Period findResolved(LocalDateTime start);
}
