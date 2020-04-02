package org.thespheres.betula.services.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import org.thespheres.betula.document.DocumentId;

/**
 * <p>Java-Klasse für fetch complex type.
 *
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser
 * Klasse enthalten ist.
 *
 * <pre>
 * &lt;complexType name="fetch">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ticket" type="{http://web.service.betula.thespheres.org/}xmlDocumentId" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fetch", propOrder = {
    "ticket"
})
public class Fetch {

    protected DocumentId ticket;

    /**
     * Ruft den Wert der ticket-Eigenschaft ab.
     *
     * @return possible object is {@link XmlDocumentId }
     *
     */
    public DocumentId getTicket() {
        return ticket;
    }

    /**
     * Legt den Wert der ticket-Eigenschaft fest.
     *
     * @param value allowed object is {@link XmlDocumentId }
     *
     */
    public void setTicket(DocumentId value) {
        this.ticket = value;
    }
}
