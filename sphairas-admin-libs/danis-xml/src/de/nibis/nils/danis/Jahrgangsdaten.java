//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.03.24 um 08:21:06 PM CET 
//


package de.nibis.nils.danis;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java-Klasse für jahrgangsdaten complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="jahrgangsdaten">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="schulname" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="schulgliederungsbezeichnung" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="schulform" type="{http://www.nils.nibis.de/DaNiS}kennzahltext" minOccurs="0"/>
 *         &lt;element name="gruppenbezeichnung" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="jahrgangsstufe" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" minOccurs="0"/>
 *         &lt;element name="klassensprecher" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="stadtschuelerrat" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="schuelersprecher" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="kreisschuelerrat" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="jgdklassensprecher" type="{http://www.nils.nibis.de/DaNiS}jgdklassensprecher" minOccurs="0"/>
 *         &lt;element name="jgdstadtschuelerrat" type="{http://www.nils.nibis.de/DaNiS}jgdstadtschuelerrat" minOccurs="0"/>
 *         &lt;element name="jgdschuelersprecher" type="{http://www.nils.nibis.de/DaNiS}jgdschuelersprecher" minOccurs="0"/>
 *         &lt;element name="jgdkreisschuelerrat" type="{http://www.nils.nibis.de/DaNiS}jgdkreisschuelerrat" minOccurs="0"/>
 *         &lt;element name="lehrmittelausleiheart" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="lehrmittelbezahlt" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="lehrmittelgezahlterbetrag" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="lehrmittelausleiheDatum" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
 *         &lt;element name="foerderung" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="foerderungab" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
 *         &lt;element name="foerderbedarfdeutschab" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
 *         &lt;element name="profil" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="profil2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="status" type="{http://www.nils.nibis.de/DaNiS}jahrgangsdatenstatus" minOccurs="0"/>
 *         &lt;element name="enddatum" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
 *         &lt;element name="belegungen" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="belegung" type="{http://www.nils.nibis.de/DaNiS}belegung" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="sonderpaedbedarfe" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="sonderpaedbedarf" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "jahrgangsdaten", propOrder = {

})
public class Jahrgangsdaten {

    protected String schulname;
    protected String schulgliederungsbezeichnung;
    protected Kennzahltext schulform;
    protected String gruppenbezeichnung;
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger jahrgangsstufe;
    protected Boolean klassensprecher;
    protected Boolean stadtschuelerrat;
    protected Boolean schuelersprecher;
    protected Boolean kreisschuelerrat;
    @XmlSchemaType(name = "string")
    protected Jgdklassensprecher jgdklassensprecher;
    @XmlSchemaType(name = "string")
    protected Jgdstadtschuelerrat jgdstadtschuelerrat;
    @XmlSchemaType(name = "string")
    protected Jgdschuelersprecher jgdschuelersprecher;
    @XmlSchemaType(name = "string")
    protected Jgdkreisschuelerrat jgdkreisschuelerrat;
    protected String lehrmittelausleiheart;
    protected Boolean lehrmittelbezahlt;
    protected BigDecimal lehrmittelgezahlterbetrag;
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar lehrmittelausleiheDatum;
    protected String foerderung;
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar foerderungab;
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar foerderbedarfdeutschab;
    protected String profil;
    protected String profil2;
    @XmlSchemaType(name = "string")
    protected Jahrgangsdatenstatus status;
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar enddatum;
    protected Jahrgangsdaten.Belegungen belegungen;
    protected Jahrgangsdaten.Sonderpaedbedarfe sonderpaedbedarfe;

    /**
     * Ruft den Wert der schulname-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSchulname() {
        return schulname;
    }

    /**
     * Legt den Wert der schulname-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSchulname(String value) {
        this.schulname = value;
    }

    /**
     * Ruft den Wert der schulgliederungsbezeichnung-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSchulgliederungsbezeichnung() {
        return schulgliederungsbezeichnung;
    }

    /**
     * Legt den Wert der schulgliederungsbezeichnung-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSchulgliederungsbezeichnung(String value) {
        this.schulgliederungsbezeichnung = value;
    }

    /**
     * Ruft den Wert der schulform-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Kennzahltext }
     *     
     */
    public Kennzahltext getSchulform() {
        return schulform;
    }

    /**
     * Legt den Wert der schulform-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Kennzahltext }
     *     
     */
    public void setSchulform(Kennzahltext value) {
        this.schulform = value;
    }

    /**
     * Ruft den Wert der gruppenbezeichnung-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGruppenbezeichnung() {
        return gruppenbezeichnung;
    }

    /**
     * Legt den Wert der gruppenbezeichnung-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGruppenbezeichnung(String value) {
        this.gruppenbezeichnung = value;
    }

    /**
     * Ruft den Wert der jahrgangsstufe-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getJahrgangsstufe() {
        return jahrgangsstufe;
    }

    /**
     * Legt den Wert der jahrgangsstufe-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setJahrgangsstufe(BigInteger value) {
        this.jahrgangsstufe = value;
    }

    /**
     * Ruft den Wert der klassensprecher-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isKlassensprecher() {
        return klassensprecher;
    }

    /**
     * Legt den Wert der klassensprecher-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setKlassensprecher(Boolean value) {
        this.klassensprecher = value;
    }

    /**
     * Ruft den Wert der stadtschuelerrat-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isStadtschuelerrat() {
        return stadtschuelerrat;
    }

    /**
     * Legt den Wert der stadtschuelerrat-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setStadtschuelerrat(Boolean value) {
        this.stadtschuelerrat = value;
    }

    /**
     * Ruft den Wert der schuelersprecher-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSchuelersprecher() {
        return schuelersprecher;
    }

    /**
     * Legt den Wert der schuelersprecher-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSchuelersprecher(Boolean value) {
        this.schuelersprecher = value;
    }

    /**
     * Ruft den Wert der kreisschuelerrat-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isKreisschuelerrat() {
        return kreisschuelerrat;
    }

    /**
     * Legt den Wert der kreisschuelerrat-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setKreisschuelerrat(Boolean value) {
        this.kreisschuelerrat = value;
    }

    /**
     * Ruft den Wert der jgdklassensprecher-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Jgdklassensprecher }
     *     
     */
    public Jgdklassensprecher getJgdklassensprecher() {
        return jgdklassensprecher;
    }

    /**
     * Legt den Wert der jgdklassensprecher-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Jgdklassensprecher }
     *     
     */
    public void setJgdklassensprecher(Jgdklassensprecher value) {
        this.jgdklassensprecher = value;
    }

    /**
     * Ruft den Wert der jgdstadtschuelerrat-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Jgdstadtschuelerrat }
     *     
     */
    public Jgdstadtschuelerrat getJgdstadtschuelerrat() {
        return jgdstadtschuelerrat;
    }

    /**
     * Legt den Wert der jgdstadtschuelerrat-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Jgdstadtschuelerrat }
     *     
     */
    public void setJgdstadtschuelerrat(Jgdstadtschuelerrat value) {
        this.jgdstadtschuelerrat = value;
    }

    /**
     * Ruft den Wert der jgdschuelersprecher-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Jgdschuelersprecher }
     *     
     */
    public Jgdschuelersprecher getJgdschuelersprecher() {
        return jgdschuelersprecher;
    }

    /**
     * Legt den Wert der jgdschuelersprecher-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Jgdschuelersprecher }
     *     
     */
    public void setJgdschuelersprecher(Jgdschuelersprecher value) {
        this.jgdschuelersprecher = value;
    }

    /**
     * Ruft den Wert der jgdkreisschuelerrat-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Jgdkreisschuelerrat }
     *     
     */
    public Jgdkreisschuelerrat getJgdkreisschuelerrat() {
        return jgdkreisschuelerrat;
    }

    /**
     * Legt den Wert der jgdkreisschuelerrat-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Jgdkreisschuelerrat }
     *     
     */
    public void setJgdkreisschuelerrat(Jgdkreisschuelerrat value) {
        this.jgdkreisschuelerrat = value;
    }

    /**
     * Ruft den Wert der lehrmittelausleiheart-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLehrmittelausleiheart() {
        return lehrmittelausleiheart;
    }

    /**
     * Legt den Wert der lehrmittelausleiheart-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLehrmittelausleiheart(String value) {
        this.lehrmittelausleiheart = value;
    }

    /**
     * Ruft den Wert der lehrmittelbezahlt-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isLehrmittelbezahlt() {
        return lehrmittelbezahlt;
    }

    /**
     * Legt den Wert der lehrmittelbezahlt-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setLehrmittelbezahlt(Boolean value) {
        this.lehrmittelbezahlt = value;
    }

    /**
     * Ruft den Wert der lehrmittelgezahlterbetrag-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getLehrmittelgezahlterbetrag() {
        return lehrmittelgezahlterbetrag;
    }

    /**
     * Legt den Wert der lehrmittelgezahlterbetrag-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setLehrmittelgezahlterbetrag(BigDecimal value) {
        this.lehrmittelgezahlterbetrag = value;
    }

    /**
     * Ruft den Wert der lehrmittelausleiheDatum-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getLehrmittelausleiheDatum() {
        return lehrmittelausleiheDatum;
    }

    /**
     * Legt den Wert der lehrmittelausleiheDatum-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setLehrmittelausleiheDatum(XMLGregorianCalendar value) {
        this.lehrmittelausleiheDatum = value;
    }

    /**
     * Ruft den Wert der foerderung-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFoerderung() {
        return foerderung;
    }

    /**
     * Legt den Wert der foerderung-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFoerderung(String value) {
        this.foerderung = value;
    }

    /**
     * Ruft den Wert der foerderungab-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getFoerderungab() {
        return foerderungab;
    }

    /**
     * Legt den Wert der foerderungab-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setFoerderungab(XMLGregorianCalendar value) {
        this.foerderungab = value;
    }

    /**
     * Ruft den Wert der foerderbedarfdeutschab-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getFoerderbedarfdeutschab() {
        return foerderbedarfdeutschab;
    }

    /**
     * Legt den Wert der foerderbedarfdeutschab-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setFoerderbedarfdeutschab(XMLGregorianCalendar value) {
        this.foerderbedarfdeutschab = value;
    }

    /**
     * Ruft den Wert der profil-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProfil() {
        return profil;
    }

    /**
     * Legt den Wert der profil-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProfil(String value) {
        this.profil = value;
    }

    /**
     * Ruft den Wert der profil2-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProfil2() {
        return profil2;
    }

    /**
     * Legt den Wert der profil2-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProfil2(String value) {
        this.profil2 = value;
    }

    /**
     * Ruft den Wert der status-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Jahrgangsdatenstatus }
     *     
     */
    public Jahrgangsdatenstatus getStatus() {
        return status;
    }

    /**
     * Legt den Wert der status-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Jahrgangsdatenstatus }
     *     
     */
    public void setStatus(Jahrgangsdatenstatus value) {
        this.status = value;
    }

    /**
     * Ruft den Wert der enddatum-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getEnddatum() {
        return enddatum;
    }

    /**
     * Legt den Wert der enddatum-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setEnddatum(XMLGregorianCalendar value) {
        this.enddatum = value;
    }

    /**
     * Ruft den Wert der belegungen-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Jahrgangsdaten.Belegungen }
     *     
     */
    public Jahrgangsdaten.Belegungen getBelegungen() {
        return belegungen;
    }

    /**
     * Legt den Wert der belegungen-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Jahrgangsdaten.Belegungen }
     *     
     */
    public void setBelegungen(Jahrgangsdaten.Belegungen value) {
        this.belegungen = value;
    }

    /**
     * Ruft den Wert der sonderpaedbedarfe-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Jahrgangsdaten.Sonderpaedbedarfe }
     *     
     */
    public Jahrgangsdaten.Sonderpaedbedarfe getSonderpaedbedarfe() {
        return sonderpaedbedarfe;
    }

    /**
     * Legt den Wert der sonderpaedbedarfe-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Jahrgangsdaten.Sonderpaedbedarfe }
     *     
     */
    public void setSonderpaedbedarfe(Jahrgangsdaten.Sonderpaedbedarfe value) {
        this.sonderpaedbedarfe = value;
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
     *         &lt;element name="belegung" type="{http://www.nils.nibis.de/DaNiS}belegung" maxOccurs="unbounded" minOccurs="0"/>
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
        "belegung"
    })
    public static class Belegungen {

        protected List<Belegung> belegung;

        /**
         * Gets the value of the belegung property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the belegung property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getBelegung().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Belegung }
         * 
         * 
         */
        public List<Belegung> getBelegung() {
            if (belegung == null) {
                belegung = new ArrayList<Belegung>();
            }
            return this.belegung;
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
     *         &lt;element name="sonderpaedbedarf" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
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
        "sonderpaedbedarf"
    })
    public static class Sonderpaedbedarfe {

        protected List<String> sonderpaedbedarf;

        /**
         * Gets the value of the sonderpaedbedarf property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the sonderpaedbedarf property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getSonderpaedbedarf().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getSonderpaedbedarf() {
            if (sonderpaedbedarf == null) {
                sonderpaedbedarf = new ArrayList<String>();
            }
            return this.sonderpaedbedarf;
        }

    }

}
