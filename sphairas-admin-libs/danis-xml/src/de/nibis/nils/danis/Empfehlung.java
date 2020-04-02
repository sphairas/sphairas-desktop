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
 * <p>Java-Klasse für empfehlung.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="empfehlung">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="empfKeine"/>
 *     &lt;enumeration value="empfHauptschule"/>
 *     &lt;enumeration value="empfRealschule"/>
 *     &lt;enumeration value="empfGymnasium"/>
 *     &lt;enumeration value="empfIGS"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "empfehlung")
@XmlEnum
public enum Empfehlung {

    @XmlEnumValue("empfKeine")
    EMPF_KEINE("empfKeine"),
    @XmlEnumValue("empfHauptschule")
    EMPF_HAUPTSCHULE("empfHauptschule"),
    @XmlEnumValue("empfRealschule")
    EMPF_REALSCHULE("empfRealschule"),
    @XmlEnumValue("empfGymnasium")
    EMPF_GYMNASIUM("empfGymnasium"),
    @XmlEnumValue("empfIGS")
    EMPF_IGS("empfIGS");
    private final String value;

    Empfehlung(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Empfehlung fromValue(String v) {
        for (Empfehlung c: Empfehlung.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
