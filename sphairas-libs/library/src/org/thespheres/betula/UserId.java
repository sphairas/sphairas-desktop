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

@XmlType(name = "userIdType", namespace = "http://www.thespheres.org/xsd/betula/betula.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public final class UserId extends Identity<String> implements Serializable {

    private static final long serialVersionUID = 1L;
    @XmlAttribute(name = "id", required = true)
    private String unitId;
    @XmlAttribute(name = "authority", required = true)
    private String authority;

    public UserId() {
    }

    public UserId(String authority, String id) {
        if (authority == null || id == null) {
            throw new IllegalArgumentException(); //TODO
        }
        this.authority = authority;
        this.unitId = id;
    }

    @Override
    public String getId() {
        return unitId;
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UserId other = (UserId) obj;
        if ((this.authority == null) ? (other.authority != null) : !this.authority.equals(other.authority)) {
            return false;
        }
        return !((this.unitId == null) ? (other.unitId != null) : !this.unitId.equals(other.unitId));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + (this.authority != null ? this.authority.hashCode() : 0);
        return 59 * hash + (this.unitId != null ? this.unitId.hashCode() : 0);
    }
}
