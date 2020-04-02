/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.scheme.spi;

import java.io.Serializable;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import org.thespheres.betula.Identity;

/**
 *
 * @author boris.heithecker
 */
@XmlType(name = "lessonIdType", namespace = "http://www.thespheres.org/xsd/betula/betula.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class LessonId extends Identity<String> implements Serializable {

    private static final long serialVersionUID = 1L;
    @XmlAttribute(name = "id", required = true)
    private String id;
    @XmlAttribute(name = "authority", required = true)
    private String authority;

    public LessonId() {
    }

    public LessonId(String authority, String id) {
        this.id = id;
        this.authority = authority;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.id);
        return 59 * hash + Objects.hashCode(this.authority);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LessonId other = (LessonId) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return Objects.equals(this.authority, other.authority);
    }

}
