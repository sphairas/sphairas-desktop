/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula;

import java.io.Serializable;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import org.thespheres.betula.document.util.Identities;

/**
 *
 * @author boris.heithecker
 */
@XmlType(name = "termIdType", namespace = "http://www.thespheres.org/xsd/betula/betula.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class TermId extends Identity<Integer> implements Serializable {

    private static final long serialVersionUID = 1L;
    @XmlAttribute(name = "id", required = true)
    private Integer id;
    @XmlAttribute(name = "authority", required = true)
    private String authority;

    public TermId() {
    }

    public TermId(String authority, int id) {
        this.id = id;
        this.authority = authority;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    public static TermId resolve(final String input) {
        return Identities.resolve(input, (a, i, v) -> new TermId(a, Integer.parseInt(i)), null, null);
    }

    public static TermId resolve(final String input, final String defaultAuthority) {
        return Identities.resolve(input, (a, i, v) -> new TermId(a, Integer.parseInt(i)), defaultAuthority, null);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 23 * hash + (this.authority != null ? this.authority.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TermId other = (TermId) obj;
        if (!Objects.equals(this.id, other.id) && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return !((this.authority == null) ? (other.authority != null) : !this.authority.equals(other.authority));
    }

}
