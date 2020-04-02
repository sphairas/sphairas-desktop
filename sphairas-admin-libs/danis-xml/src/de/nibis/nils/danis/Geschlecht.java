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
 * <p>Java-Klasse für geschlecht.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="geschlecht">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="gUnbestimmt"/>
 *     &lt;enumeration value="gMaennlich"/>
 *     &lt;enumeration value="gWeiblich"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "geschlecht")
@XmlEnum
public enum Geschlecht {

    @XmlEnumValue("gUnbestimmt")
    G_UNBESTIMMT("gUnbestimmt"),
    @XmlEnumValue("gMaennlich")
    G_MAENNLICH("gMaennlich"),
    @XmlEnumValue("gWeiblich")
    G_WEIBLICH("gWeiblich");
    private final String value;

    Geschlecht(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Geschlecht fromValue(String v) {
        for (Geschlecht c: Geschlecht.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
