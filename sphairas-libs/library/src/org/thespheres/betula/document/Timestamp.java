/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document;

import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlValue;

/**
 *
 * @author boris.heithecker
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Timestamp implements Serializable {

    private static final long serialVersionUID = 1L;
    @XmlValue
    private String date;

    private Timestamp() {
        this(new java.sql.Timestamp(System.currentTimeMillis()));
    }

    public static Timestamp now() {
        return new Timestamp();
    }

    public Timestamp(Date d) {
        this(new java.sql.Timestamp(d.getTime()));
    }

    public Timestamp(java.sql.Timestamp date) {
        this.date = date.toString();
    }

    public Timestamp(long time) {
        this(new java.sql.Timestamp(time));
    }

    public Date getDate() {
        return date != null ? java.sql.Timestamp.valueOf(date) : null;
    }

    public java.sql.Timestamp getValue() {
        return java.sql.Timestamp.valueOf(date);
    }

    public ZonedDateTime getDateTime() {
        return ZonedDateTime.ofInstant(getValue().toInstant(), ZoneId.systemDefault());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return 97 * hash + Objects.hashCode(this.date);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Timestamp other = (Timestamp) obj;
        return Objects.equals(this.date, other.date);
    }
}
