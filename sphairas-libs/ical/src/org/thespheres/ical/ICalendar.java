/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.ical;

import java.util.List;

/**
 *
 * @author boris.heithecker
 */
public interface ICalendar extends IComponent<CalendarComponentProperty> {

    public static final ICalendar EMPTY = new EmptyCalendar();
    public static final String MIME = "text/calendar";

    public List<CalendarComponent> getComponents();
}
