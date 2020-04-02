/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.scheme.spi;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import org.thespheres.betula.TermId;
import org.thespheres.betula.services.IllegalAuthorityException;

/**
 *
 * @author boris.heithecker
 */
public interface TermSchedule extends Schedule {

    static final ScheduleType SCHEDULE_TYPE = new ScheduleType("terms");

    @Override
    public default ScheduleType getType() {
        return SCHEDULE_TYPE;
    }

    public Term getCurrentTerm();

    default public Term getTerm(Date d) {
        final LocalDate ld = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return termOf(ld);
    }

    public Term termOf(LocalDate date);

    public Term resolve(TermId id) throws TermNotFoundException, IllegalAuthorityException;

}
