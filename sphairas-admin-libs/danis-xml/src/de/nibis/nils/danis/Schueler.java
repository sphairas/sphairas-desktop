//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.03.24 um 08:21:06 PM CET 
//


package de.nibis.nils.danis;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java-Klasse für schueler complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="schueler">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="person" type="{http://www.nils.nibis.de/DaNiS}person"/>
 *         &lt;element name="identnummer" type="{http://www.nils.nibis.de/DaNiS}identnummer"/>
 *         &lt;element name="foerderbedarfab" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
 *         &lt;element name="aufnahmedatum" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
 *         &lt;element name="abgangsdatum" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
 *         &lt;element name="vonschule" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="vorschuleempf" type="{http://www.nils.nibis.de/DaNiS}empfehlung" minOccurs="0"/>
 *         &lt;element name="nachschule" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="abschluss" type="{http://www.nils.nibis.de/DaNiS}kennzahltext" minOccurs="0"/>
 *         &lt;element name="berufswunsch" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ersteinschulJahr" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" minOccurs="0"/>
 *         &lt;element name="krankenkasse" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="geburtsland" type="{http://www.nils.nibis.de/DaNiS}kennzahltext" minOccurs="0"/>
 *         &lt;element name="geburtslandiso" type="{http://www.nils.nibis.de/DaNiS}isoland" minOccurs="0"/>
 *         &lt;element name="zuzugsjahr" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" minOccurs="0"/>
 *         &lt;element name="verkehrssprache" type="{http://www.nils.nibis.de/DaNiS}kennzahltext" minOccurs="0"/>
 *         &lt;element name="trendempfehlung" type="{http://www.nils.nibis.de/DaNiS}empfehlung" minOccurs="0"/>
 *         &lt;element name="empfehlung" type="{http://www.nils.nibis.de/DaNiS}empfehlung" minOccurs="0"/>
 *         &lt;element name="elternentscheidung" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="elternwunsch" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="elternentscheidungneu" type="{http://www.nils.nibis.de/DaNiS}nachbarschule" minOccurs="0"/>
 *         &lt;element name="elternwunschneu" type="{http://www.nils.nibis.de/DaNiS}nachbarschule" minOccurs="0"/>
 *         &lt;element name="konfession" type="{http://www.nils.nibis.de/DaNiS}kennzahltext" minOccurs="0"/>
 *         &lt;element name="geburtsort" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="versbemerkung" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="staatsangehoerigkeit" type="{http://www.nils.nibis.de/DaNiS}kennzahltext" minOccurs="0"/>
 *         &lt;element name="staatsangehoerigkeitiso" type="{http://www.nils.nibis.de/DaNiS}isoland" minOccurs="0"/>
 *         &lt;element name="verantwortliche" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="verantwortlicher" type="{http://www.nils.nibis.de/DaNiS}verantwortlicher" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="befristetevorgaenge" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="befristeterVorgang" type="{http://www.nils.nibis.de/DaNiS}befristeterVorgang" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="herkunft" type="{http://www.nils.nibis.de/DaNiS}kennzahltext" minOccurs="0"/>
 *         &lt;element name="nachschulform" type="{http://www.nils.nibis.de/DaNiS}schulform" minOccurs="0"/>
 *         &lt;element name="jahrgangsdatenliste" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="jahrgangsdaten" type="{http://www.nils.nibis.de/DaNiS}jahrgangsdaten" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "schueler", propOrder = {

})
public class Schueler {

    @XmlElement(required = true)
    protected Person person;
    @XmlElement(required = true)
    protected String identnummer;
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar foerderbedarfab;
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar aufnahmedatum;
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar abgangsdatum;
    protected String vonschule;
    @XmlSchemaType(name = "string")
    protected Empfehlung vorschuleempf;
    protected String nachschule;
    protected Kennzahltext abschluss;
    protected String berufswunsch;
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger ersteinschulJahr;
    protected String krankenkasse;
    protected Kennzahltext geburtsland;
    protected Isoland geburtslandiso;
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger zuzugsjahr;
    protected Kennzahltext verkehrssprache;
    @XmlSchemaType(name = "string")
    protected Empfehlung trendempfehlung;
    @XmlSchemaType(name = "string")
    protected Empfehlung empfehlung;
    protected String elternentscheidung;
    protected String elternwunsch;
    protected Nachbarschule elternentscheidungneu;
    protected Nachbarschule elternwunschneu;
    protected Kennzahltext konfession;
    protected String geburtsort;
    protected String versbemerkung;
    protected Kennzahltext staatsangehoerigkeit;
    protected Isoland staatsangehoerigkeitiso;
    protected Schueler.Verantwortliche verantwortliche;
    protected Schueler.Befristetevorgaenge befristetevorgaenge;
    protected Kennzahltext herkunft;
    protected Schulform nachschulform;
    protected Schueler.Jahrgangsdatenliste jahrgangsdatenliste;

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
     * Ruft den Wert der identnummer-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdentnummer() {
        return identnummer;
    }

    /**
     * Legt den Wert der identnummer-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdentnummer(String value) {
        this.identnummer = value;
    }

    /**
     * Ruft den Wert der foerderbedarfab-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getFoerderbedarfab() {
        return foerderbedarfab;
    }

    /**
     * Legt den Wert der foerderbedarfab-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setFoerderbedarfab(XMLGregorianCalendar value) {
        this.foerderbedarfab = value;
    }

    /**
     * Ruft den Wert der aufnahmedatum-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getAufnahmedatum() {
        return aufnahmedatum;
    }

    /**
     * Legt den Wert der aufnahmedatum-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setAufnahmedatum(XMLGregorianCalendar value) {
        this.aufnahmedatum = value;
    }

    /**
     * Ruft den Wert der abgangsdatum-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getAbgangsdatum() {
        return abgangsdatum;
    }

    /**
     * Legt den Wert der abgangsdatum-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setAbgangsdatum(XMLGregorianCalendar value) {
        this.abgangsdatum = value;
    }

    /**
     * Ruft den Wert der vonschule-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVonschule() {
        return vonschule;
    }

    /**
     * Legt den Wert der vonschule-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVonschule(String value) {
        this.vonschule = value;
    }

    /**
     * Ruft den Wert der vorschuleempf-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Empfehlung }
     *     
     */
    public Empfehlung getVorschuleempf() {
        return vorschuleempf;
    }

    /**
     * Legt den Wert der vorschuleempf-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Empfehlung }
     *     
     */
    public void setVorschuleempf(Empfehlung value) {
        this.vorschuleempf = value;
    }

    /**
     * Ruft den Wert der nachschule-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNachschule() {
        return nachschule;
    }

    /**
     * Legt den Wert der nachschule-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNachschule(String value) {
        this.nachschule = value;
    }

    /**
     * Ruft den Wert der abschluss-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Kennzahltext }
     *     
     */
    public Kennzahltext getAbschluss() {
        return abschluss;
    }

    /**
     * Legt den Wert der abschluss-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Kennzahltext }
     *     
     */
    public void setAbschluss(Kennzahltext value) {
        this.abschluss = value;
    }

    /**
     * Ruft den Wert der berufswunsch-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBerufswunsch() {
        return berufswunsch;
    }

    /**
     * Legt den Wert der berufswunsch-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBerufswunsch(String value) {
        this.berufswunsch = value;
    }

    /**
     * Ruft den Wert der ersteinschulJahr-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getErsteinschulJahr() {
        return ersteinschulJahr;
    }

    /**
     * Legt den Wert der ersteinschulJahr-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setErsteinschulJahr(BigInteger value) {
        this.ersteinschulJahr = value;
    }

    /**
     * Ruft den Wert der krankenkasse-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKrankenkasse() {
        return krankenkasse;
    }

    /**
     * Legt den Wert der krankenkasse-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKrankenkasse(String value) {
        this.krankenkasse = value;
    }

    /**
     * Ruft den Wert der geburtsland-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Kennzahltext }
     *     
     */
    public Kennzahltext getGeburtsland() {
        return geburtsland;
    }

    /**
     * Legt den Wert der geburtsland-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Kennzahltext }
     *     
     */
    public void setGeburtsland(Kennzahltext value) {
        this.geburtsland = value;
    }

    /**
     * Ruft den Wert der geburtslandiso-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Isoland }
     *     
     */
    public Isoland getGeburtslandiso() {
        return geburtslandiso;
    }

    /**
     * Legt den Wert der geburtslandiso-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Isoland }
     *     
     */
    public void setGeburtslandiso(Isoland value) {
        this.geburtslandiso = value;
    }

    /**
     * Ruft den Wert der zuzugsjahr-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getZuzugsjahr() {
        return zuzugsjahr;
    }

    /**
     * Legt den Wert der zuzugsjahr-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setZuzugsjahr(BigInteger value) {
        this.zuzugsjahr = value;
    }

    /**
     * Ruft den Wert der verkehrssprache-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Kennzahltext }
     *     
     */
    public Kennzahltext getVerkehrssprache() {
        return verkehrssprache;
    }

    /**
     * Legt den Wert der verkehrssprache-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Kennzahltext }
     *     
     */
    public void setVerkehrssprache(Kennzahltext value) {
        this.verkehrssprache = value;
    }

    /**
     * Ruft den Wert der trendempfehlung-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Empfehlung }
     *     
     */
    public Empfehlung getTrendempfehlung() {
        return trendempfehlung;
    }

    /**
     * Legt den Wert der trendempfehlung-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Empfehlung }
     *     
     */
    public void setTrendempfehlung(Empfehlung value) {
        this.trendempfehlung = value;
    }

    /**
     * Ruft den Wert der empfehlung-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Empfehlung }
     *     
     */
    public Empfehlung getEmpfehlung() {
        return empfehlung;
    }

    /**
     * Legt den Wert der empfehlung-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Empfehlung }
     *     
     */
    public void setEmpfehlung(Empfehlung value) {
        this.empfehlung = value;
    }

    /**
     * Ruft den Wert der elternentscheidung-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getElternentscheidung() {
        return elternentscheidung;
    }

    /**
     * Legt den Wert der elternentscheidung-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setElternentscheidung(String value) {
        this.elternentscheidung = value;
    }

    /**
     * Ruft den Wert der elternwunsch-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getElternwunsch() {
        return elternwunsch;
    }

    /**
     * Legt den Wert der elternwunsch-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setElternwunsch(String value) {
        this.elternwunsch = value;
    }

    /**
     * Ruft den Wert der elternentscheidungneu-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Nachbarschule }
     *     
     */
    public Nachbarschule getElternentscheidungneu() {
        return elternentscheidungneu;
    }

    /**
     * Legt den Wert der elternentscheidungneu-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Nachbarschule }
     *     
     */
    public void setElternentscheidungneu(Nachbarschule value) {
        this.elternentscheidungneu = value;
    }

    /**
     * Ruft den Wert der elternwunschneu-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Nachbarschule }
     *     
     */
    public Nachbarschule getElternwunschneu() {
        return elternwunschneu;
    }

    /**
     * Legt den Wert der elternwunschneu-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Nachbarschule }
     *     
     */
    public void setElternwunschneu(Nachbarschule value) {
        this.elternwunschneu = value;
    }

    /**
     * Ruft den Wert der konfession-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Kennzahltext }
     *     
     */
    public Kennzahltext getKonfession() {
        return konfession;
    }

    /**
     * Legt den Wert der konfession-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Kennzahltext }
     *     
     */
    public void setKonfession(Kennzahltext value) {
        this.konfession = value;
    }

    /**
     * Ruft den Wert der geburtsort-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGeburtsort() {
        return geburtsort;
    }

    /**
     * Legt den Wert der geburtsort-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGeburtsort(String value) {
        this.geburtsort = value;
    }

    /**
     * Ruft den Wert der versbemerkung-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersbemerkung() {
        return versbemerkung;
    }

    /**
     * Legt den Wert der versbemerkung-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersbemerkung(String value) {
        this.versbemerkung = value;
    }

    /**
     * Ruft den Wert der staatsangehoerigkeit-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Kennzahltext }
     *     
     */
    public Kennzahltext getStaatsangehoerigkeit() {
        return staatsangehoerigkeit;
    }

    /**
     * Legt den Wert der staatsangehoerigkeit-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Kennzahltext }
     *     
     */
    public void setStaatsangehoerigkeit(Kennzahltext value) {
        this.staatsangehoerigkeit = value;
    }

    /**
     * Ruft den Wert der staatsangehoerigkeitiso-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Isoland }
     *     
     */
    public Isoland getStaatsangehoerigkeitiso() {
        return staatsangehoerigkeitiso;
    }

    /**
     * Legt den Wert der staatsangehoerigkeitiso-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Isoland }
     *     
     */
    public void setStaatsangehoerigkeitiso(Isoland value) {
        this.staatsangehoerigkeitiso = value;
    }

    /**
     * Ruft den Wert der verantwortliche-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Schueler.Verantwortliche }
     *     
     */
    public Schueler.Verantwortliche getVerantwortliche() {
        return verantwortliche;
    }

    /**
     * Legt den Wert der verantwortliche-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Schueler.Verantwortliche }
     *     
     */
    public void setVerantwortliche(Schueler.Verantwortliche value) {
        this.verantwortliche = value;
    }

    /**
     * Ruft den Wert der befristetevorgaenge-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Schueler.Befristetevorgaenge }
     *     
     */
    public Schueler.Befristetevorgaenge getBefristetevorgaenge() {
        return befristetevorgaenge;
    }

    /**
     * Legt den Wert der befristetevorgaenge-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Schueler.Befristetevorgaenge }
     *     
     */
    public void setBefristetevorgaenge(Schueler.Befristetevorgaenge value) {
        this.befristetevorgaenge = value;
    }

    /**
     * Ruft den Wert der herkunft-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Kennzahltext }
     *     
     */
    public Kennzahltext getHerkunft() {
        return herkunft;
    }

    /**
     * Legt den Wert der herkunft-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Kennzahltext }
     *     
     */
    public void setHerkunft(Kennzahltext value) {
        this.herkunft = value;
    }

    /**
     * Ruft den Wert der nachschulform-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Schulform }
     *     
     */
    public Schulform getNachschulform() {
        return nachschulform;
    }

    /**
     * Legt den Wert der nachschulform-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Schulform }
     *     
     */
    public void setNachschulform(Schulform value) {
        this.nachschulform = value;
    }

    /**
     * Ruft den Wert der jahrgangsdatenliste-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Schueler.Jahrgangsdatenliste }
     *     
     */
    public Schueler.Jahrgangsdatenliste getJahrgangsdatenliste() {
        return jahrgangsdatenliste;
    }

    /**
     * Legt den Wert der jahrgangsdatenliste-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Schueler.Jahrgangsdatenliste }
     *     
     */
    public void setJahrgangsdatenliste(Schueler.Jahrgangsdatenliste value) {
        this.jahrgangsdatenliste = value;
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
     *         &lt;element name="befristeterVorgang" type="{http://www.nils.nibis.de/DaNiS}befristeterVorgang" maxOccurs="unbounded" minOccurs="0"/>
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
        "befristeterVorgang"
    })
    public static class Befristetevorgaenge {

        protected List<BefristeterVorgang> befristeterVorgang;

        /**
         * Gets the value of the befristeterVorgang property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the befristeterVorgang property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getBefristeterVorgang().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link BefristeterVorgang }
         * 
         * 
         */
        public List<BefristeterVorgang> getBefristeterVorgang() {
            if (befristeterVorgang == null) {
                befristeterVorgang = new ArrayList<BefristeterVorgang>();
            }
            return this.befristeterVorgang;
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
     *         &lt;element name="jahrgangsdaten" type="{http://www.nils.nibis.de/DaNiS}jahrgangsdaten" maxOccurs="unbounded" minOccurs="0"/>
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
        "jahrgangsdaten"
    })
    public static class Jahrgangsdatenliste {

        protected List<Jahrgangsdaten> jahrgangsdaten;

        /**
         * Gets the value of the jahrgangsdaten property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the jahrgangsdaten property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getJahrgangsdaten().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Jahrgangsdaten }
         * 
         * 
         */
        public List<Jahrgangsdaten> getJahrgangsdaten() {
            if (jahrgangsdaten == null) {
                jahrgangsdaten = new ArrayList<Jahrgangsdaten>();
            }
            return this.jahrgangsdaten;
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
     *         &lt;element name="verantwortlicher" type="{http://www.nils.nibis.de/DaNiS}verantwortlicher" maxOccurs="unbounded" minOccurs="0"/>
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
        "verantwortlicher"
    })
    public static class Verantwortliche {

        protected List<Verantwortlicher> verantwortlicher;

        /**
         * Gets the value of the verantwortlicher property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the verantwortlicher property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getVerantwortlicher().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Verantwortlicher }
         * 
         * 
         */
        public List<Verantwortlicher> getVerantwortlicher() {
            if (verantwortlicher == null) {
                verantwortlicher = new ArrayList<Verantwortlicher>();
            }
            return this.verantwortlicher;
        }

    }

}
