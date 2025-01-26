/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.zeugnis;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.niedersachsen.LSchB;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.ical.CalendarResourceProvider;
import org.thespheres.ical.ICalendar;
import org.thespheres.ical.InvalidComponentException;
import org.thespheres.ical.builder.CalendarResourceType;
import org.thespheres.ical.builder.ICalendarBuilder;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = CalendarResourceProvider.class)
public class ZeugnisDatenCalendarProvider implements CalendarResourceProvider {

    static final String FILE = "/org/thespheres/betula/niedersachsen/resources/ausgabetermine.ics";
    private ICalendar calendar;
    private final static CalendarResourceType TYPE = new CalendarResourceType("zeugnisausgabe") {
    };

    public ZeugnisDatenCalendarProvider() throws IOException {
        try (InputStream is = ZeugnisDatenCalendarProvider.class.getResourceAsStream(FILE)) {
            final List<ICalendar> p = ICalendarBuilder.parseCalendars(is);
            if (p.size() != 1) {
                throw new IllegalStateException("Resource " + FILE + " must contain exactly one calender.");
            }
            calendar = p.get(0);
        } catch (ParseException | InvalidComponentException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public CalendarResourceType getType() {
        return TYPE;
    }

    @Override
    public ICalendar getCalendar() {
        return calendar;
    }

    @Override
    public ProviderInfo getProviderInfo() {
        return LSchB.PROVIDER_INFO;
    }
}
