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
 * <p>Java-Klasse für verantwortlichenArt.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="verantwortlichenArt">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="vaMutter"/>
 *     &lt;enumeration value="vaVater"/>
 *     &lt;enumeration value="vaGrossmutter"/>
 *     &lt;enumeration value="vaGrossvater"/>
 *     &lt;enumeration value="vaSchwester"/>
 *     &lt;enumeration value="vaBruder"/>
 *     &lt;enumeration value="vaTante"/>
 *     &lt;enumeration value="vaOnkel"/>
 *     &lt;enumeration value="vaVormund"/>
 *     &lt;enumeration value="vaJugendamt"/>
 *     &lt;enumeration value="vaSonstige"/>
 *     &lt;enumeration value="vaPflegeMutter"/>
 *     &lt;enumeration value="vaPflegeVater"/>
 *     &lt;enumeration value="vaStiefMutter"/>
 *     &lt;enumeration value="vaStiefVater"/>
 *     &lt;enumeration value="vaGastMutter"/>
 *     &lt;enumeration value="vaGastVater"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "verantwortlichenArt")
@XmlEnum
public enum VerantwortlichenArt {

    @XmlEnumValue("vaMutter")
    VA_MUTTER("vaMutter"),
    @XmlEnumValue("vaVater")
    VA_VATER("vaVater"),
    @XmlEnumValue("vaGrossmutter")
    VA_GROSSMUTTER("vaGrossmutter"),
    @XmlEnumValue("vaGrossvater")
    VA_GROSSVATER("vaGrossvater"),
    @XmlEnumValue("vaSchwester")
    VA_SCHWESTER("vaSchwester"),
    @XmlEnumValue("vaBruder")
    VA_BRUDER("vaBruder"),
    @XmlEnumValue("vaTante")
    VA_TANTE("vaTante"),
    @XmlEnumValue("vaOnkel")
    VA_ONKEL("vaOnkel"),
    @XmlEnumValue("vaVormund")
    VA_VORMUND("vaVormund"),
    @XmlEnumValue("vaJugendamt")
    VA_JUGENDAMT("vaJugendamt"),
    @XmlEnumValue("vaSonstige")
    VA_SONSTIGE("vaSonstige"),
    @XmlEnumValue("vaPflegeMutter")
    VA_PFLEGE_MUTTER("vaPflegeMutter"),
    @XmlEnumValue("vaPflegeVater")
    VA_PFLEGE_VATER("vaPflegeVater"),
    @XmlEnumValue("vaStiefMutter")
    VA_STIEF_MUTTER("vaStiefMutter"),
    @XmlEnumValue("vaStiefVater")
    VA_STIEF_VATER("vaStiefVater"),
    @XmlEnumValue("vaGastMutter")
    VA_GAST_MUTTER("vaGastMutter"),
    @XmlEnumValue("vaGastVater")
    VA_GAST_VATER("vaGastVater");
    private final String value;

    VerantwortlichenArt(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static VerantwortlichenArt fromValue(String v) {
        for (VerantwortlichenArt c: VerantwortlichenArt.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
