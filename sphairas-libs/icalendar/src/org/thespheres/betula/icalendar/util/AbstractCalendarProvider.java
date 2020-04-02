/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.icalendar.util;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;
import org.thespheres.betula.icalendar.CalendarProvider;
import org.thespheres.ical.ICalendar;

/**
 *
 * @author boris.heithecker
 */
public abstract class AbstractCalendarProvider implements CalendarProvider {
    
    protected final List<ICalendar> calendars = new ArrayList<>();
    protected final ChangeSupport cSupport = new ChangeSupport(this);

    protected AbstractCalendarProvider() {
    }

    @Override
    public List<ICalendar> getCalendars() {
        return calendars;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        cSupport.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        cSupport.removeChangeListener(listener);
    }
    
}
