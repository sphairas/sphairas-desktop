/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document;

import java.io.Serializable;
import java.security.Principal;
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
@XmlType(name = "signeeType", namespace = "http://www.thespheres.org/xsd/betula/betula.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public final class Signee extends Identity<String> implements Serializable, Principal {

    public static final Signee NULL = new Signee("null", "null", false);

    private static final long serialVersionUID = 1L;
    @XmlAttribute(required = true)
    private String prefix;
    @XmlAttribute(required = true)
    private String suffix;
    @XmlAttribute(required = true)
    private boolean alias = false;

    public Signee() {
    }

    public Signee(final String prefix, final String suffix, final boolean alias) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.alias = alias;
    }

    public static Signee parse(final String email) {
        if (email == null || email.trim().isEmpty()) {
            return NULL;
        }
        final String[] elements = email.split("@");
        if (elements.length == 2) {
            final String prefix = elements[0].trim();
            final String suffix = elements[1].trim();
            if (!prefix.isEmpty() && !suffix.isEmpty()) {
                return new Signee(prefix, suffix, true);
            }
        }
        throw new IllegalArgumentException("Not a valid signee representation: " + email);
    }

    public static boolean isNull(Signee s) {
        return s == null || s.equals(NULL) || ("null".equals(s.getId()) && "null".equals(s.getAuthority()));
    }

    @Override
    public String getId() {
        return prefix;
    }

    @Override
    public String getAuthority() {
        return suffix;
    }

    public boolean isAlias() {
        return alias;
    }

    public String getPrefix() {
        return getId();
    }

    public String getSuffix() {
        return getAuthority();
    }

    @Override
    public String getName() {
        return getId();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(this.prefix);
        hash = 89 * hash + Objects.hashCode(this.suffix);
        return 89 * hash + (this.alias ? 1 : 0);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Signee other = (Signee) obj;
        if (!Objects.equals(this.prefix, other.prefix)) {
            return false;
        }
        if (!Objects.equals(this.suffix, other.suffix)) {
            return false;
        }
        return this.alias == other.alias;
    }

    @Override
    public String toString() {
        return prefix + "@" + suffix;
    }

}
