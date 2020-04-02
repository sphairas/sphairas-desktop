//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.03.24 um 08:21:06 PM CET 
//


package de.nibis.nils.danis;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für belegungsart.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="belegungsart">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="baPflicht"/>
 *     &lt;enumeration value="baWahlpflicht"/>
 *     &lt;enumeration value="baWahl"/>
 *     &lt;enumeration value="baWahlfrei"/>
 *     &lt;enumeration value="baAG"/>
 *     &lt;enumeration value="baSchwerpunkt"/>
 *     &lt;enumeration value="baFoerder"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "belegungsart")
@XmlEnum
public enum Belegungsart {

    @XmlEnumValue("baPflicht")
    BA_PFLICHT("baPflicht"),
    @XmlEnumValue("baWahlpflicht")
    BA_WAHLPFLICHT("baWahlpflicht"),
    @XmlEnumValue("baWahl")
    BA_WAHL("baWahl"),
    @XmlEnumValue("baWahlfrei")
    BA_WAHLFREI("baWahlfrei"),
    @XmlEnumValue("baAG")
    BA_AG("baAG"),
    @XmlEnumValue("baSchwerpunkt")
    BA_SCHWERPUNKT("baSchwerpunkt"),
    @XmlEnumValue("baFoerder")
    BA_FOERDER("baFoerder");
    private final String value;

    Belegungsart(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Belegungsart fromValue(String v) {
        for (Belegungsart c: Belegungsart.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
