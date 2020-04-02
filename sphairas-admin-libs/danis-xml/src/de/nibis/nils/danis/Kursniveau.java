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
 * <p>Java-Klasse für kursniveau.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="kursniveau">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="knGrundlegend"/>
 *     &lt;enumeration value="knErhoeht"/>
 *     &lt;enumeration value="knGKurs"/>
 *     &lt;enumeration value="knEKurs"/>
 *     &lt;enumeration value="knZKurs"/>
 *     &lt;enumeration value="knUnbestimmt"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "kursniveau")
@XmlEnum
public enum Kursniveau {

    @XmlEnumValue("knGrundlegend")
    KN_GRUNDLEGEND("knGrundlegend"),
    @XmlEnumValue("knErhoeht")
    KN_ERHOEHT("knErhoeht"),
    @XmlEnumValue("knGKurs")
    KN_G_KURS("knGKurs"),
    @XmlEnumValue("knEKurs")
    KN_E_KURS("knEKurs"),
    @XmlEnumValue("knZKurs")
    KN_Z_KURS("knZKurs"),
    @XmlEnumValue("knUnbestimmt")
    KN_UNBESTIMMT("knUnbestimmt");
    private final String value;

    Kursniveau(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Kursniveau fromValue(String v) {
        for (Kursniveau c: Kursniveau.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
