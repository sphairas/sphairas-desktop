/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.icalendar;

import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.editor.mimelookup.MimeLocation;
import org.openide.nodes.Node;
import org.thespheres.ical.CalendarComponent;
import org.thespheres.ical.ICalendar;

/**
 *
 * @author boris.heithecker
 */
@MimeLocation(subfolderName = "Calendar")
public interface CalendarProvider {

    //DocumentId or URL
    public Object getCalendarId();

    //Should in fact be one calendar
    public List<ICalendar> getCalendars();

    public void addChangeListener(ChangeListener listener);

    public void removeChangeListener(ChangeListener listener);

    public interface Decorator extends CalendarProvider {

        public Node decorate(CalendarComponent component);
    }
}
