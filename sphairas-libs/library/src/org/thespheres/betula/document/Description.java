/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author boris.heithecker
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Description {

    @XmlAttribute(name = "description-key")
    private String key;
    @XmlAttribute(name = "language-code")
    private String lang;
    @XmlAttribute(name = "language-authority")
    private String authority;
    @XmlValue
    private String value;
    @XmlAttribute(name = "is-href")
    private boolean href;

    public Description() {
    }

    public Description(final String key, final String text) {
        this.key = key;
        this.value = text;
    }

    public Description(final String key, final String text, final Language lang) {
        this.key = key;
        this.value = text;
        this.lang = lang.getId();
        this.authority = lang.getAuthority();
    }

    public String key() {
        return key;
    }

    public String getDescription() {
        return value;
    }

    public boolean isDescriptionHref() {
        return href;
    }

    public Language getLanguage() {
        if (!StringUtils.isBlank(lang) && !StringUtils.isBlank(authority)) {
            return new Language(authority, lang);
        }
        return null;
    }
}
