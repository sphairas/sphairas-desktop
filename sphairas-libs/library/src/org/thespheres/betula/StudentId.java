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
@XmlType(name = "studentIdType", namespace = "http://www.thespheres.org/xsd/betula/betula.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public final class StudentId extends Identity<Long> implements Serializable {

    private static final long serialVersionUID = 1L;
    @XmlAttribute(name = "id", required = true)
    private long id;
    @XmlAttribute(name = "authority", required = true)
    private String authority;

    //JAXB only!
    public StudentId() {
    }

    public StudentId(String authority, Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null.");
        }
        this.id = id;
        this.authority = authority;
    }

    public StudentId(Student s) {
        this.id = s.getStudentId().getId();
        this.authority = s.getStudentId().getAuthority();
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StudentId other = (StudentId) obj;
        if (this.id != other.id) {
            return false;
        }
        return !((this.authority == null) ? (other.authority != null) : !this.authority.equals(other.authority));
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + (int) (this.id ^ (this.id >>> 32));
        return 59 * hash + (this.authority != null ? this.authority.hashCode() : 0);
    }

    @Override
    public String toString() {
        return "{" + getAuthority() + "}" + Long.toString(getId());
    }

}
