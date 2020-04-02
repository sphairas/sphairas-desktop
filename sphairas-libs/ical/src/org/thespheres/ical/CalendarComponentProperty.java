/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.ical;

/**
 *
 * @author boris.heithecker
 */
public interface CalendarComponentProperty extends ComponentProperty {

    public static final String DTSTAMP = "DTSTAMP";
    public static final String ATTACH = "ATTACH";
    // ... need to add more.
    public static final String CATEGORIES = "CATEGORIES";
    public static final String DESCRIPTION = "DESCRIPTION";
    public static final String DTEND = "DTEND";
    public static final String DUE = "DUE";
    // properties
    // TODO: do we want to list these here?  the complete list is long.
    public static final String DTSTART = "DTSTART";
    public static final String DURATION = "DURATION";
    public static final String EXDATE = "EXDATE";
    public static final String EXRULE = "EXRULE";
    public static final String RDATE = "RDATE";
    public static final String RRULE = "RRULE";
    public static final String STATUS = "STATUS";
    public static final String SUMMARY = "SUMMARY";
    public static final String TZID = "TZID";
    public static final String PRIORITY = "PRIORITY";
    public static final String LOCATION = "LOCATION";
    public static final String UID = "UID";
    public static final String RECURRENCEID = "RECURRENCE-ID";
}
