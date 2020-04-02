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
 * <p>Java-Klasse für jgdkreisschuelerrat.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="jgdkreisschuelerrat">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="jskrKeineFunktion"/>
 *     &lt;enumeration value="jskrKreisschuelerrat"/>
 *     &lt;enumeration value="jskrStellvertreter"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "jgdkreisschuelerrat")
@XmlEnum
public enum Jgdkreisschuelerrat {

    @XmlEnumValue("jskrKeineFunktion")
    JSKR_KEINE_FUNKTION("jskrKeineFunktion"),
    @XmlEnumValue("jskrKreisschuelerrat")
    JSKR_KREISSCHUELERRAT("jskrKreisschuelerrat"),
    @XmlEnumValue("jskrStellvertreter")
    JSKR_STELLVERTRETER("jskrStellvertreter");
    private final String value;

    Jgdkreisschuelerrat(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Jgdkreisschuelerrat fromValue(String v) {
        for (Jgdkreisschuelerrat c: Jgdkreisschuelerrat.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
