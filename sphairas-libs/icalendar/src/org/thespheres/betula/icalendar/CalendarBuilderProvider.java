/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.icalendar;

import java.util.List;
import org.thespheres.ical.builder.ICalendarBuilder;

/**
 *
 * @author boris.heithecker
 */
public interface CalendarBuilderProvider extends CalendarProvider {

    public List<ICalendarBuilder> getCalendarBuilders();

    public void setModified();
}
