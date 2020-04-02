/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.impl;

import java.util.Map;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.thespheres.betula.services.ProviderInfo;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "provider")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class XmlProviderInfoEntry implements ProviderInfo {

    @XmlAttribute(name = "url")
    private String url;
    @XmlElement(name = "display-name")
    private String displayName;
    @XmlElement(name = "display-name")
    private String description;
    @XmlAttribute(name = "code-name-base")
    private String codeNameBase;

    public XmlProviderInfoEntry() {
    }

    public XmlProviderInfoEntry(String url, String displayName) {
        this.url = url;
        this.displayName = displayName;
    }

    public XmlProviderInfoEntry(String url, String displayName, String codeNameBase) {
        this.url = url;
        this.displayName = displayName;
        this.codeNameBase = codeNameBase;
    }

    public static XmlProviderInfoEntry create(final Map<String, ?> args) {
        final String url = (String) args.get("url");
        final String dn = (String) args.get("display-name");
        final String desc = (String) args.get("description");
        final String cnb = (String) args.get("code-name-base");
        final XmlProviderInfoEntry ret = new XmlProviderInfoEntry(url, dn, cnb);
        ret.setDescription(desc);
        return ret;
    }

    @Override
    public String getURL() {
        return url;
    }

    public String getCodeNameBase() {
        return codeNameBase;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return 89 * hash + Objects.hashCode(this.url);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ProviderInfo)) {
            return false;
        }
        final ProviderInfo other = (ProviderInfo) obj;
        return Objects.equals(this.url, other.getURL());
    }

    @Override
    public String toString() {
        return "XmlProviderInfoEntry{" + "url=" + url + ", displayName=" + displayName + ", description=" + description + ", codeNameBase=" + codeNameBase + '}';
    }

}
