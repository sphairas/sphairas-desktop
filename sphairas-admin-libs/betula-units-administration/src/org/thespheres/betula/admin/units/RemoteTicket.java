/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units;

import org.thespheres.betula.Ticket;
import org.thespheres.betula.document.util.GenericXmlTicket;
import org.thespheres.ical.CalendarComponent;

/**
 *
 * @author boris.heithecker
 */
public class RemoteTicket {

    protected final GenericXmlTicket ticket;
    private CalendarComponent calendar;
    private String message;
    private final Ticket id;

    public RemoteTicket(Ticket t, GenericXmlTicket ticket) {
        this.id = t;
        this.ticket = ticket;
    }

    public Ticket getTicket() {
        return id;
    }

    public GenericXmlTicket getTicketDocument() {
        return ticket;
    }

    public CalendarComponent getCalendar() {
        return calendar;
    }

    public void setCalendar(CalendarComponent calendar) {
        this.calendar = calendar;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
