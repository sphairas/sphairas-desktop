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
 * <p>Java-Klasse für kontaktart.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="kontaktart">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="kaTelefon"/>
 *     &lt;enumeration value="kaEMail"/>
 *     &lt;enumeration value="kaBank"/>
 *     &lt;enumeration value="kaFax"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "kontaktart")
@XmlEnum
public enum Kontaktart {

    @XmlEnumValue("kaTelefon")
    KA_TELEFON("kaTelefon"),
    @XmlEnumValue("kaEMail")
    KA_E_MAIL("kaEMail"),
    @XmlEnumValue("kaBank")
    KA_BANK("kaBank"),
    @XmlEnumValue("kaFax")
    KA_FAX("kaFax");
    private final String value;

    Kontaktart(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Kontaktart fromValue(String v) {
        for (Kontaktart c: Kontaktart.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
