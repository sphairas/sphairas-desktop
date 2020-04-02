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
 * <p>Java-Klasse für belegung complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="belegung">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="fach" type="{http://www.nils.nibis.de/DaNiS}kennzahltext" minOccurs="0"/>
 *         &lt;element name="kurs" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="niveau" type="{http://www.nils.nibis.de/DaNiS}kursniveau"/>
 *         &lt;element name="von" type="{http://www.nils.nibis.de/DaNiS}halbjahr"/>
 *         &lt;element name="bis" type="{http://www.nils.nibis.de/DaNiS}halbjahr"/>
 *         &lt;element name="belegungsart" type="{http://www.nils.nibis.de/DaNiS}belegungsart"/>
 *         &lt;element name="bewertung1" type="{http://www.nils.nibis.de/DaNiS}bewertung"/>
 *         &lt;element name="bewertung2" type="{http://www.nils.nibis.de/DaNiS}bewertung"/>
 *         &lt;element name="fremdsprachentyp" type="{http://www.nils.nibis.de/DaNiS}fremdsprachentyp" minOccurs="0"/>
 *       &lt;/all>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "belegung", propOrder = {

})
public class Belegung {

    protected Kennzahltext fach;
    protected String kurs;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected Kursniveau niveau;
    @XmlSchemaType(name = "nonNegativeInteger")
    protected int von;
    @XmlSchemaType(name = "nonNegativeInteger")
    protected int bis;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected Belegungsart belegungsart;
    @XmlElement(required = true)
    protected Bewertung bewertung1;
    @XmlElement(required = true)
    protected Bewertung bewertung2;
    @XmlSchemaType(name = "string")
    protected Fremdsprachentyp fremdsprachentyp;

    /**
     * Ruft den Wert der fach-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Kennzahltext }
     *     
     */
    public Kennzahltext getFach() {
        return fach;
    }

    /**
     * Legt den Wert der fach-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Kennzahltext }
     *     
     */
    public void setFach(Kennzahltext value) {
        this.fach = value;
    }

    /**
     * Ruft den Wert der kurs-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKurs() {
        return kurs;
    }

    /**
     * Legt den Wert der kurs-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKurs(String value) {
        this.kurs = value;
    }

    /**
     * Ruft den Wert der niveau-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Kursniveau }
     *     
     */
    public Kursniveau getNiveau() {
        return niveau;
    }

    /**
     * Legt den Wert der niveau-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Kursniveau }
     *     
     */
    public void setNiveau(Kursniveau value) {
        this.niveau = value;
    }

    /**
     * Ruft den Wert der von-Eigenschaft ab.
     * 
     */
    public int getVon() {
        return von;
    }

    /**
     * Legt den Wert der von-Eigenschaft fest.
     * 
     */
    public void setVon(int value) {
        this.von = value;
    }

    /**
     * Ruft den Wert der bis-Eigenschaft ab.
     * 
     */
    public int getBis() {
        return bis;
    }

    /**
     * Legt den Wert der bis-Eigenschaft fest.
     * 
     */
    public void setBis(int value) {
        this.bis = value;
    }

    /**
     * Ruft den Wert der belegungsart-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Belegungsart }
     *     
     */
    public Belegungsart getBelegungsart() {
        return belegungsart;
    }

    /**
     * Legt den Wert der belegungsart-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Belegungsart }
     *     
     */
    public void setBelegungsart(Belegungsart value) {
        this.belegungsart = value;
    }

    /**
     * Ruft den Wert der bewertung1-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Bewertung }
     *     
     */
    public Bewertung getBewertung1() {
        return bewertung1;
    }

    /**
     * Legt den Wert der bewertung1-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Bewertung }
     *     
     */
    public void setBewertung1(Bewertung value) {
        this.bewertung1 = value;
    }

    /**
     * Ruft den Wert der bewertung2-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Bewertung }
     *     
     */
    public Bewertung getBewertung2() {
        return bewertung2;
    }

    /**
     * Legt den Wert der bewertung2-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Bewertung }
     *     
     */
    public void setBewertung2(Bewertung value) {
        this.bewertung2 = value;
    }

    /**
     * Ruft den Wert der fremdsprachentyp-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Fremdsprachentyp }
     *     
     */
    public Fremdsprachentyp getFremdsprachentyp() {
        return fremdsprachentyp;
    }

    /**
     * Legt den Wert der fremdsprachentyp-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Fremdsprachentyp }
     *     
     */
    public void setFremdsprachentyp(Fremdsprachentyp value) {
        this.fremdsprachentyp = value;
    }

}
