/*
 * To change this template, choose Tools | Templates
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
import org.thespheres.betula.util.DeweyDecimal;

/**
 *
 * @author boris.heithecker
 */
@XmlType(name = "periodIdType", namespace = "http://www.thespheres.org/xsd/betula/betula.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public final class PeriodId extends Identity<Integer> implements Serializable {

    private static final long serialVersionUID = 1L;
    @XmlAttribute(name = "period", required = true)
    private Integer period;
    @XmlAttribute(name = "authority", required = true)
    private String authority;
    @XmlAttribute(name = "version", required = true)
    private String version;

    public PeriodId() {//For Unmarshalling only!!!
    }

    public PeriodId(String authority, int period, Version version) {
        this.period = period;
        this.authority = authority;
        this.version = version.value;
    }

    @Override
    public Integer getId() {
        return period;
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    public PeriodId.Version getVersion() {
        return new PeriodId.Version(version);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.period != null ? this.period.hashCode() : 0);
        hash = 67 * hash + (this.authority != null ? this.authority.hashCode() : 0);
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
        final PeriodId other = (PeriodId) obj;
        if ((this.period == null) ? (other.period != null) : !this.period.equals(other.period)) {
            return false;
        }
        return !((this.authority == null) ? (other.authority != null) : !this.authority.equals(other.authority));
    }

    public static final class Version implements Serializable {

        public static final Version LATEST = new Version("latest");
        public static final Version UNSPECIFIED = new Version("unspecified");
        private String value;

        public Version() {
            this("unspecified");
        }

        private Version(String version) {
            this.value = version;
        }

        Version(DeweyDecimal dd) {
            this.value = dd.toString();
        }

        public static Version parse(String version) {
            if (version == null || version.equals(UNSPECIFIED.getVersion())) {
                return UNSPECIFIED;
            } else if (version.equals(LATEST.getVersion())) {
                return LATEST;
            } else {
                DeweyDecimal dd = null;
                try {
                    dd = new DeweyDecimal(version);
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException(ex);
                }
                return new Version(dd.toString());
            }
        }

        public String getVersion() {
            return value;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 17 * hash + Objects.hashCode(this.value);
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
            final Version other = (Version) obj;
            return Objects.equals(this.value, other.value);
        }
    }
}
