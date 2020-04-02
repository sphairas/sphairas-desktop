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
 * <p>Java-Klasse für fremdsprachentyp.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="fremdsprachentyp">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="fstNichtZutreffend"/>
 *     &lt;enumeration value="fstErste"/>
 *     &lt;enumeration value="fstZweite"/>
 *     &lt;enumeration value="fstDritte"/>
 *     &lt;enumeration value="fstHerkSprUnterricht"/>
 *     &lt;enumeration value="fstSonstige"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "fremdsprachentyp")
@XmlEnum
public enum Fremdsprachentyp {

    @XmlEnumValue("fstNichtZutreffend")
    FST_NICHT_ZUTREFFEND("fstNichtZutreffend"),
    @XmlEnumValue("fstErste")
    FST_ERSTE("fstErste"),
    @XmlEnumValue("fstZweite")
    FST_ZWEITE("fstZweite"),
    @XmlEnumValue("fstDritte")
    FST_DRITTE("fstDritte"),
    @XmlEnumValue("fstHerkSprUnterricht")
    FST_HERK_SPR_UNTERRICHT("fstHerkSprUnterricht"),
    @XmlEnumValue("fstSonstige")
    FST_SONSTIGE("fstSonstige");
    private final String value;

    Fremdsprachentyp(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Fremdsprachentyp fromValue(String v) {
        for (Fremdsprachentyp c: Fremdsprachentyp.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
