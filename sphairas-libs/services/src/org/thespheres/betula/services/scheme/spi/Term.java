/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.scheme.spi;

import java.time.LocalDate;
import java.time.ZoneId;
import org.thespheres.betula.TermId;
import java.util.Date;

/**
 *
 * @author boris.heithecker
 */
public interface Term extends ScheduledItem<TermId, TermSchedule> {

    default public Date getBegin() {
        return Date.from(getBeginDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public LocalDate getBeginDate();

    default public Date getEnd() {
        return Date.from(getEndDate().atStartOfDay().plusDays(1l).atZone(ZoneId.systemDefault()).toInstant());
    }

    public LocalDate getEndDate();

    //Format parameter
    public Object getParameter(String parameterName);

}
