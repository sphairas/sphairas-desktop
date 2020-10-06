/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import org.apache.commons.lang3.StringUtils;
import org.thespheres.betula.Identity;
import org.thespheres.betula.document.util.Identities;
import org.thespheres.betula.util.DeweyDecimal;

/**
 *
 * @author boris.heithecker
 */
@XmlType(name = "documentIdType", namespace = "http://www.thespheres.org/xsd/betula/betula.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public final class DocumentId extends Identity<String> implements Serializable {

    public static final DocumentId NULL = new DocumentId("null", "null", Version.UNSPECIFIED);

    private static final long serialVersionUID = 1L;
    @XmlAttribute(name = "id", required = true)
    private String id;
    @XmlAttribute(name = "authority", required = true)
    private String authority;
    @XmlAttribute(name = "version", required = true)
    private String version;

    public DocumentId() {//For Unmarshalling only!!!
    }

    public DocumentId(String authority, String id, Version version) {
        this.id = id;
        this.authority = authority;
        this.version = version.value;
    }

    public static boolean isNull(DocumentId d) {
        return d == null || d.equals(NULL) || ("null".equals(d.getId()) && "null".equals(d.getAuthority()) && Objects.equals(d.getVersion(), Version.UNSPECIFIED));
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    public Version getVersion() {
        return new Version(version);
    }

    @Override
    public String toString() {
        final StringJoiner sj = new StringJoiner("@");
        final String prefix = id + (getVersion().equals(Version.UNSPECIFIED) ? "" : Identity.VERSION_DELIMITER + version);
        sj.add(prefix);
        if (authority != null) {
            sj.add(authority);
        }
        return sj.toString();
    }

    public static DocumentId resolve(final String input) {
        return Identities.resolve(input, (a, i, v) -> new DocumentId(a, i, Version.parse(v)), null, null);
    }

    public static DocumentId resolve(final String input, final String defaultAuthority, final Version defaultVersion) {
        if (defaultVersion == null || StringUtils.isBlank(defaultVersion.getVersion())) {
            throw new IllegalArgumentException("Version cannot be null or empty.");
        }
        return Identities.resolve(input, (a, i, v) -> new DocumentId(a, i, Version.parse(v)), defaultAuthority, defaultVersion.getVersion());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 67 * hash + (this.authority != null ? this.authority.hashCode() : 0);
        return 67 * hash + (this.version != null ? this.version.hashCode() : 0);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DocumentId other = (DocumentId) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        if ((this.authority == null) ? (other.authority != null) : !this.authority.equals(other.authority)) {
            return false;
        }
        return !((this.version == null) ? (other.version != null) : !this.version.equals(other.version));
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
            return 17 * hash + Objects.hashCode(this.value);
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
