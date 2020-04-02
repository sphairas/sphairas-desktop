/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;
import org.thespheres.betula.Identity;
import org.thespheres.betula.RecordId;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.Ticket;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.UserId;

/**
 *
 * @author boris.heithecker
 * @param <I>
 * @param <V>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "entryType", 
        propOrder = {"identity", "timestamp"})
public class Entry<I extends Identity, V> extends Template<V> {

    public static final String PROP_IDENTITY = "PROP_IDENTITY";
    public static final String PROP_TIMESTAMP = "PROP_TIMESTAMP";
    public static final String PROP_SIGNATURE = "PROP_SIGNATURE";
    public static final String PROP_NOTE = "PROP_NOTE";
    @XmlElements({
        @XmlElement(name = "document", type = DocumentId.class),
        @XmlElement(name = "student", type = StudentId.class),
        @XmlElement(name = "unit", type = UnitId.class),
        @XmlElement(name = "record", type = RecordId.class),
        @XmlElement(name = "term", type = TermId.class),
        @XmlElement(name = "signee", type = Signee.class),
        @XmlElement(name = "ticket", type = Ticket.class),
        @XmlElement(name = "user", type =UserId.class)
    })
    private  I identity;
    @XmlAttribute(name = "timestamp")
    private Timestamp timestamp;

    public Entry() {
    }

    public Entry(Action action, I id) {
        super(action);
        this.identity = id;
    }

    public Entry(Action action, I id, V value) {
        super(action);
        this.identity = id;
        setValueImpl(value);
    }

    public I getIdentity() {
        return identity;
    }

    public void setIdentity(I identity) {
        this.identity = identity;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

}
