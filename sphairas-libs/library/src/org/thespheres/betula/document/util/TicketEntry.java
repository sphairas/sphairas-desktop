/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document.util;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.thespheres.betula.Ticket;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.Entry;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "ticket", namespace = "http://www.thespheres.org/xsd/betula/container.xsd")
@XmlType(name = "ticketType", namespace = "http://www.thespheres.org/xsd/betula/container.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class TicketEntry extends Entry<Ticket, GenericXmlTicket> implements Serializable {

    private static final long serialVersionUID = 1L;

    public TicketEntry() {
    }

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    public TicketEntry(Action action, Ticket id, String ticketClass, String version) {
        super(action, id);
        setValue(new GenericXmlTicket(ticketClass, version));
    }

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    public TicketEntry(Action action, Ticket id) {
        super(action, id);
        setValue(null);
    }

    @Override
    public void setValue(GenericXmlTicket value) {
        if (value instanceof GenericXmlTicket || value == null) {
            super.setValue(value);
        } else {
            throw new IllegalArgumentException();
        }
    }
}
