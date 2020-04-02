//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.03.24 um 08:21:06 PM CET 
//


package de.nibis.nils.danis;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für verantwortlicher complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="verantwortlicher">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="person" type="{http://www.nils.nibis.de/DaNiS}person"/>
 *         &lt;element name="erziehungsberechtigt" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="umgangsberechtigt" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="art" type="{http://www.nils.nibis.de/DaNiS}verantwortlichenArt"/>
 *       &lt;/all>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "verantwortlicher", propOrder = {

})
public class Verantwortlicher {

    @XmlElement(required = true)
    protected Person person;
    protected Boolean erziehungsberechtigt;
    protected Boolean umgangsberechtigt;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected VerantwortlichenArt art;

    /**
     * Ruft den Wert der person-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Person }
     *     
     */
    public Person getPerson() {
        return person;
    }

    /**
     * Legt den Wert der person-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Person }
     *     
     */
    public void setPerson(Person value) {
        this.person = value;
    }

    /**
     * Ruft den Wert der erziehungsberechtigt-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isErziehungsberechtigt() {
        return erziehungsberechtigt;
    }

    /**
     * Legt den Wert der erziehungsberechtigt-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setErziehungsberechtigt(Boolean value) {
        this.erziehungsberechtigt = value;
    }

    /**
     * Ruft den Wert der umgangsberechtigt-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isUmgangsberechtigt() {
        return umgangsberechtigt;
    }

    /**
     * Legt den Wert der umgangsberechtigt-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUmgangsberechtigt(Boolean value) {
        this.umgangsberechtigt = value;
    }

    /**
     * Ruft den Wert der art-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link VerantwortlichenArt }
     *     
     */
    public VerantwortlichenArt getArt() {
        return art;
    }

    /**
     * Legt den Wert der art-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link VerantwortlichenArt }
     *     
     */
    public void setArt(VerantwortlichenArt value) {
        this.art = value;
    }

}
