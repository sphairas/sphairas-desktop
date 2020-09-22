/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import org.thespheres.betula.Identity;

/**
 *
 * @author boris.heithecker
 */
@XmlType(name = "languageIdType", namespace = "http://www.thespheres.org/xsd/betula/betula.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class Language extends Identity<String> implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final String IETF = "ietf";

    @XmlAttribute(name = "id", required = true)
    private String id;
    @XmlAttribute(name = "authority", required = true)
    private String authority;

    public Language() {
    }

    public Language(final String authority, final String lang) {
        this.id = lang;
        this.authority = authority;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getAuthority() {
        return authority;
    }

}
