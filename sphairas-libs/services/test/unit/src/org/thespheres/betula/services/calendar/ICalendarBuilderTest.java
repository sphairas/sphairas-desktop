/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.calendar;

import org.thespheres.ical.builder.ICalendarBuilder;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.thespheres.ical.CalendarComponent;
import org.thespheres.ical.CalendarComponentProperty;
import org.thespheres.ical.ICalendar;
import org.thespheres.ical.impl.CalendarComponentImpl;
import org.thespheres.ical.util.IComponentUtilities;

/**
 *
 * @author boris.heithecker
 */
public class ICalendarBuilderTest {

    public ICalendarBuilderTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testParseCalendars_InputStream() throws Exception {
        System.out.println("parseCalendars");
        InputStream source = ICalendarBuilderTest.class.getResourceAsStream("payday.ics");
        List<ICalendar> result = ICalendarBuilder.parseCalendars(source);
        CalendarComponent cc = result.get(0).getComponents().get(1);
        ICalendar inflate = ((CalendarComponentImpl) cc).inflate(LocalDateTime.now());
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        for (CalendarComponent ccp : inflate.getComponents()) {
            LocalDateTime ldt = IComponentUtilities.parseLocalDateTimeProperty(ccp, CalendarComponentProperty.DTSTART);
            System.out.println(ldt.format(f));
        }
    }

    @Test
    public void testParseCalendars2_InputStream() throws Exception {
        System.out.println("parseCalendars");
        InputStream source = ICalendarBuilderTest.class.getResourceAsStream("stundenplan.ics");
        List<ICalendar> result = ICalendarBuilder.parseCalendars(source);
        CalendarComponent cc = result.get(0).getComponents().get(0);
        ICalendar inflate = ((CalendarComponentImpl) cc).inflate(LocalDateTime.now());
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        for (CalendarComponent ccp : inflate.getComponents()) {
            LocalDateTime ldt = IComponentUtilities.parseLocalDateTimeProperty(ccp, CalendarComponentProperty.DTSTART);
            System.out.println(ldt.format(f));
        }
    }
}
