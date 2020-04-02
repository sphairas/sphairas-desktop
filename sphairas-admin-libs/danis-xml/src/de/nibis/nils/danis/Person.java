//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.03.24 um 08:21:06 PM CET 
//


package de.nibis.nils.danis;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java-Klasse für person complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="person">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="vorname" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="rufname" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="vornamen" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="nachname" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="titel" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="adelstitel" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="akadtitel" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="namenszusatz" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="geburtsdatum" type="{http://www.w3.org/2001/XMLSchema}date"/>
 *         &lt;element name="geschlecht" type="{http://www.nils.nibis.de/DaNiS}geschlecht"/>
 *         &lt;element name="adressen" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="adresse" type="{http://www.nils.nibis.de/DaNiS}adresse" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="bild" type="{http://www.nils.nibis.de/DaNiS}bild" minOccurs="0"/>
 *         &lt;element name="kontakte" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="kontakt" type="{http://www.nils.nibis.de/DaNiS}kontakt" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/all>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "person", propOrder = {

})
public class Person {

    protected String vorname;
    protected String rufname;
    protected String vornamen;
    @XmlElement(required = true)
    protected String nachname;
    protected String titel;
    protected String adelstitel;
    protected String akadtitel;
    protected String namenszusatz;
    @XmlElement(required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar geburtsdatum;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected Geschlecht geschlecht;
    protected Person.Adressen adressen;
    protected Bild bild;
    protected Person.Kontakte kontakte;

    /**
     * Ruft den Wert der vorname-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVorname() {
        return vorname;
    }

    /**
     * Legt den Wert der vorname-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVorname(String value) {
        this.vorname = value;
    }

    /**
     * Ruft den Wert der rufname-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRufname() {
        return rufname;
    }

    /**
     * Legt den Wert der rufname-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRufname(String value) {
        this.rufname = value;
    }

    /**
     * Ruft den Wert der vornamen-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVornamen() {
        return vornamen;
    }

    /**
     * Legt den Wert der vornamen-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVornamen(String value) {
        this.vornamen = value;
    }

    /**
     * Ruft den Wert der nachname-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNachname() {
        return nachname;
    }

    /**
     * Legt den Wert der nachname-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNachname(String value) {
        this.nachname = value;
    }

    /**
     * Ruft den Wert der titel-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTitel() {
        return titel;
    }

    /**
     * Legt den Wert der titel-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTitel(String value) {
        this.titel = value;
    }

    /**
     * Ruft den Wert der adelstitel-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAdelstitel() {
        return adelstitel;
    }

    /**
     * Legt den Wert der adelstitel-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAdelstitel(String value) {
        this.adelstitel = value;
    }

    /**
     * Ruft den Wert der akadtitel-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAkadtitel() {
        return akadtitel;
    }

    /**
     * Legt den Wert der akadtitel-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAkadtitel(String value) {
        this.akadtitel = value;
    }

    /**
     * Ruft den Wert der namenszusatz-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNamenszusatz() {
        return namenszusatz;
    }

    /**
     * Legt den Wert der namenszusatz-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNamenszusatz(String value) {
        this.namenszusatz = value;
    }

    /**
     * Ruft den Wert der geburtsdatum-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getGeburtsdatum() {
        return geburtsdatum;
    }

    /**
     * Legt den Wert der geburtsdatum-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setGeburtsdatum(XMLGregorianCalendar value) {
        this.geburtsdatum = value;
    }

    /**
     * Ruft den Wert der geschlecht-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Geschlecht }
     *     
     */
    public Geschlecht getGeschlecht() {
        return geschlecht;
    }

    /**
     * Legt den Wert der geschlecht-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Geschlecht }
     *     
     */
    public void setGeschlecht(Geschlecht value) {
        this.geschlecht = value;
    }

    /**
     * Ruft den Wert der adressen-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Person.Adressen }
     *     
     */
    public Person.Adressen getAdressen() {
        return adressen;
    }

    /**
     * Legt den Wert der adressen-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Person.Adressen }
     *     
     */
    public void setAdressen(Person.Adressen value) {
        this.adressen = value;
    }

    /**
     * Ruft den Wert der bild-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Bild }
     *     
     */
    public Bild getBild() {
        return bild;
    }

    /**
     * Legt den Wert der bild-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Bild }
     *     
     */
    public void setBild(Bild value) {
        this.bild = value;
    }

    /**
     * Ruft den Wert der kontakte-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Person.Kontakte }
     *     
     */
    public Person.Kontakte getKontakte() {
        return kontakte;
    }

    /**
     * Legt den Wert der kontakte-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Person.Kontakte }
     *     
     */
    public void setKontakte(Person.Kontakte value) {
        this.kontakte = value;
    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="adresse" type="{http://www.nils.nibis.de/DaNiS}adresse" maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "adresse"
    })
    public static class Adressen {

        protected List<Adresse> adresse;

        /**
         * Gets the value of the adresse property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the adresse property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getAdresse().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Adresse }
         * 
         * 
         */
        public List<Adresse> getAdresse() {
            if (adresse == null) {
                adresse = new ArrayList<Adresse>();
            }
            return this.adresse;
        }

    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="kontakt" type="{http://www.nils.nibis.de/DaNiS}kontakt" maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "kontakt"
    })
    public static class Kontakte {

        protected List<Kontakt> kontakt;

        /**
         * Gets the value of the kontakt property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the kontakt property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getKontakt().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Kontakt }
         * 
         * 
         */
        public List<Kontakt> getKontakt() {
            if (kontakt == null) {
                kontakt = new ArrayList<Kontakt>();
            }
            return this.kontakt;
        }

    }

}
