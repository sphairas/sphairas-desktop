/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.clients.calendar;

import org.thespheres.betula.icalendar.util.AbstractCalendarProvider;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.ical.ICalendar;

/**
 *
 * @author boris.heithecker
 */
class CalendarProviderImpl extends AbstractCalendarProvider {

    private final DocumentId id;

    CalendarProviderImpl(DocumentId did) {
        this.id = did;
    }

    @Override
    public Object getCalendarId() {
        return id;
    }

    void addCalendar(ICalendar found) {
        synchronized (calendars) {
            calendars.add(found);
        }
        cSupport.fireChange();
    }
}
