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
 * <p>Java-Klasse für bewertungsart.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="bewertungsart">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="baKeine"/>
 *     &lt;enumeration value="baWert"/>
 *     &lt;enumeration value="baBefreit"/>
 *     &lt;enumeration value="baUnbekannt"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "bewertungsart")
@XmlEnum
public enum Bewertungsart {

    @XmlEnumValue("baKeine")
    BA_KEINE("baKeine"),
    @XmlEnumValue("baWert")
    BA_WERT("baWert"),
    @XmlEnumValue("baBefreit")
    BA_BEFREIT("baBefreit"),
    @XmlEnumValue("baUnbekannt")
    BA_UNBEKANNT("baUnbekannt");
    private final String value;

    Bewertungsart(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Bewertungsart fromValue(String v) {
        for (Bewertungsart c: Bewertungsart.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
