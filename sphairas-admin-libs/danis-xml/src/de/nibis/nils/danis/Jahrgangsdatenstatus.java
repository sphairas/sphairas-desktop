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
 * <p>Java-Klasse für jahrgangsdatenstatus.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="jahrgangsdatenstatus">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="jgdsNormal"/>
 *     &lt;enumeration value="jgdsVersetzung"/>
 *     &lt;enumeration value="jgdsUeberspringen"/>
 *     &lt;enumeration value="jgdsAufruecken"/>
 *     &lt;enumeration value="jgdsAbgang"/>
 *     &lt;enumeration value="jgdsNichtVersetzung"/>
 *     &lt;enumeration value="jgdsWiederholung"/>
 *     &lt;enumeration value="jgdsZuruecktreten"/>
 *     &lt;enumeration value="jgdsUebergang"/>
 *     &lt;enumeration value="jgdsUeberweisung"/>
 *     &lt;enumeration value="jgdsGruppenwechsel"/>
 *     &lt;enumeration value="jgdsUeberganginnerhalbSchule"/>
 *     &lt;enumeration value="jgdsUeberweisungInnerhalbSchule"/>
 *     &lt;enumeration value="jgdsFortsetzungEingangsstufe"/>
 *     &lt;enumeration value="jgdsFortsetzungSprachlernklasse"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "jahrgangsdatenstatus")
@XmlEnum
public enum Jahrgangsdatenstatus {

    @XmlEnumValue("jgdsNormal")
    JGDS_NORMAL("jgdsNormal"),
    @XmlEnumValue("jgdsVersetzung")
    JGDS_VERSETZUNG("jgdsVersetzung"),
    @XmlEnumValue("jgdsUeberspringen")
    JGDS_UEBERSPRINGEN("jgdsUeberspringen"),
    @XmlEnumValue("jgdsAufruecken")
    JGDS_AUFRUECKEN("jgdsAufruecken"),
    @XmlEnumValue("jgdsAbgang")
    JGDS_ABGANG("jgdsAbgang"),
    @XmlEnumValue("jgdsNichtVersetzung")
    JGDS_NICHT_VERSETZUNG("jgdsNichtVersetzung"),
    @XmlEnumValue("jgdsWiederholung")
    JGDS_WIEDERHOLUNG("jgdsWiederholung"),
    @XmlEnumValue("jgdsZuruecktreten")
    JGDS_ZURUECKTRETEN("jgdsZuruecktreten"),
    @XmlEnumValue("jgdsUebergang")
    JGDS_UEBERGANG("jgdsUebergang"),
    @XmlEnumValue("jgdsUeberweisung")
    JGDS_UEBERWEISUNG("jgdsUeberweisung"),
    @XmlEnumValue("jgdsGruppenwechsel")
    JGDS_GRUPPENWECHSEL("jgdsGruppenwechsel"),
    @XmlEnumValue("jgdsUeberganginnerhalbSchule")
    JGDS_UEBERGANGINNERHALB_SCHULE("jgdsUeberganginnerhalbSchule"),
    @XmlEnumValue("jgdsUeberweisungInnerhalbSchule")
    JGDS_UEBERWEISUNG_INNERHALB_SCHULE("jgdsUeberweisungInnerhalbSchule"),
    @XmlEnumValue("jgdsFortsetzungEingangsstufe")
    JGDS_FORTSETZUNG_EINGANGSSTUFE("jgdsFortsetzungEingangsstufe"),
    @XmlEnumValue("jgdsFortsetzungSprachlernklasse")
    JGDS_FORTSETZUNG_SPRACHLERNKLASSE("jgdsFortsetzungSprachlernklasse");
    private final String value;

    Jahrgangsdatenstatus(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Jahrgangsdatenstatus fromValue(String v) {
        for (Jahrgangsdatenstatus c: Jahrgangsdatenstatus.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
