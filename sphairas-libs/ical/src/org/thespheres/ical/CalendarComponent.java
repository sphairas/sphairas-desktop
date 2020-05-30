/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.ical;

import java.time.LocalDateTime;
import java.util.Date;

/**
 *
 * @author boris.heithecker
 */
public interface CalendarComponent extends IComponent<CalendarComponentProperty> {

    public static final String VALARM = "VALARM";
    public static final String VCALENDAR = "VCALENDAR";
    public static final String VEVENT = "VEVENT";
    public static final String VFREEBUSY = "VFREEBUSY";
    public static final String VJOURNAL = "VJOURNAL";
    public static final String VTIMEZONE = "VTIMEZONE";
    public static final String VTODO = "VTODO";

    public static final String PRODID = "PRODID";
    public static final String VERSION = "VERSION";
    public static final String METHOD = "METHOD";

    public static final String METHOD_PUBLISH = "PUBLISH";

    public ICalendar getICalendar();

    public UID getUID();

    /*
     *Try to resolve dtStart property to java.util.Date
     */
    //no InvalidComponentException > implementation can also implement Period from classschedule
    public LocalDateTime resolveStart() throws InvalidComponentException;

    /*
     *Try to resolve dtEnd/Duration to java.util.Date
     */
    public LocalDateTime resolveEnd() throws InvalidComponentException;

    /*
     *Try to resolve the recurrence set of this component to a new calendar object
     *The resultung calendar's components share one common (this component's) UID with distict recurrence ids. 
     */
    @Deprecated
    public ICalendar inflate(Date limit) throws InvalidComponentException;
    
    /*
     *Try to resolve the recurrence set of this component to a new calendar object
     *The resultung calendar's components share one common (this component's) UID with distict recurrence ids. 
     */
    public ICalendar inflate(LocalDateTime limit) throws InvalidComponentException;

}
