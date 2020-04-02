/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document;

import java.beans.PropertyChangeSupport;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 *
 * @author boris.heithecker
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class LanguageInfo {

    //skript transliteration
    public static final String PROP_LANGUAGE = "PROP_LANGUAGE";
    public static final String PROP_AUTHORITY = "PROP_AUTHORITY";
    public static final String PROP_AUTHORITYURL = "PROP_AUTHORITYURL";
    public static final String PROP_VALUEURL = "PROP_VALUEURL";
    @XmlValue
    private String language;
    @XmlAttribute
    private String authority;
    @XmlAttribute
    private String authorityUrl;
    @XmlAttribute
    private String valueUrl;
    private final transient PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport(this);

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        java.lang.String oldLanguage = language;
        this.language = language;
        propertyChangeSupport.firePropertyChange(PROP_LANGUAGE, oldLanguage, language);
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        java.lang.String oldAuthority = authority;
        this.authority = authority;
        propertyChangeSupport.firePropertyChange(PROP_AUTHORITY, oldAuthority, authority);
    }

    public String getAuthorityUrl() {
        return authorityUrl;
    }

    public void setAuthorityUrl(String authorityUrl) {
        java.lang.String oldAuthorityUrl = authorityUrl;
        this.authorityUrl = authorityUrl;
        propertyChangeSupport.firePropertyChange(PROP_AUTHORITYURL, oldAuthorityUrl, authorityUrl);
    }

    public String getValueUrl() {
        return valueUrl;
    }

    public void setValueUrl(String valueUrl) {
        java.lang.String oldValueUrl = valueUrl;
        this.valueUrl = valueUrl;
        propertyChangeSupport.firePropertyChange(PROP_VALUEURL, oldValueUrl, valueUrl);
    }
}
