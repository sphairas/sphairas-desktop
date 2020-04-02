/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author boris.heithecker
 */
@XmlType(name = "ticketType", namespace = "http://www.thespheres.org/xsd/betula/betula.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public final class Ticket extends Identity<Long> implements Serializable {

    private static final long serialVersionUID = 1L;
    @XmlAttribute(name = "id", required = true)
    private Long id;
    @XmlAttribute(name = "authority", required = true)
    private String authority;

    public Ticket() {//For Unmarshalling only!!!
    }

    public Ticket(String authority, Long id) {
        if (authority == null || authority.isEmpty() || id == null || id < 1) {
            throw new IllegalArgumentException();
        }
        this.id = id;
        this.authority = authority;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.id != null ? this.id.hashCode() : 0);
        return 67 * hash + (this.authority != null ? this.authority.hashCode() : 0);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Ticket other = (Ticket) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        return !((this.authority == null) ? (other.authority != null) : !this.authority.equals(other.authority));
    }

}
