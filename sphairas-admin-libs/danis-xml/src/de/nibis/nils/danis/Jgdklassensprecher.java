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
 * <p>Java-Klasse für jgdklassensprecher.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="jgdklassensprecher">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="jgkKeineFunktion"/>
 *     &lt;enumeration value="jgkKlassensprecher"/>
 *     &lt;enumeration value="jgkStellvertreter"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "jgdklassensprecher")
@XmlEnum
public enum Jgdklassensprecher {

    @XmlEnumValue("jgkKeineFunktion")
    JGK_KEINE_FUNKTION("jgkKeineFunktion"),
    @XmlEnumValue("jgkKlassensprecher")
    JGK_KLASSENSPRECHER("jgkKlassensprecher"),
    @XmlEnumValue("jgkStellvertreter")
    JGK_STELLVERTRETER("jgkStellvertreter");
    private final String value;

    Jgdklassensprecher(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Jgdklassensprecher fromValue(String v) {
        for (Jgdklassensprecher c: Jgdklassensprecher.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
