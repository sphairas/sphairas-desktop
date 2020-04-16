/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.jms;

import org.thespheres.betula.Ticket;

/**
 *
 * @author boris.heithecker
 */
public class TicketEvent extends AbstractJMSEvent<Ticket> {

    private static final long serialVersionUID = 1L;

    public enum TicketEventType {

        ADD, REMOVE
    }
    protected final TicketEventType type;

    public TicketEvent(Ticket source, TicketEventType type) {
        super(source);
        this.type = type;
    }

    public TicketEventType getType() {
        return type;
    }
}
