package org.thespheres.betula.document.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.Identity;
import org.thespheres.betula.RecordId;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.document.util.MarkerAdapter.XmlMarkerAdapter;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "ticket", namespace = "http://www.thespheres.org/xsd/betula/container.xsd")
@XmlType(name = "genericXmlTicketType",
        propOrder = {"ticketClass", "scope"})
@XmlAccessorType(value = XmlAccessType.FIELD)
public class GenericXmlTicket extends XmlContent implements Serializable {

    private static final long serialVersionUID = 1L;
    @XmlElement(name = "ticket-class")
    private XmlTicketClass ticketClass;
    @XmlElement(name = "scope-definition")
    private List<XmlTicketScope> scope;

    /*
     * For unmarshalling only!
     */
    public GenericXmlTicket() {
        super();
    }

    GenericXmlTicket(String ticketClass, String version) {
        super();
        this.ticketClass = new XmlTicketClass(ticketClass, version);
    }

    public List<XmlTicketScope> getScope() {
        if (scope == null) {
            scope = new ArrayList<>();
        }
        return scope;
    }

    public XmlTicketClass getTicketClass() {
        return ticketClass;
    }

    @XmlAccessorType(value = XmlAccessType.FIELD)
    public static class XmlTicketClass implements Serializable {

        private static final long serialVersionUID = 1L;
        @XmlAttribute
        private String version;
        @XmlValue
        private String value;

        public XmlTicketClass() {
            super();
        }

        public XmlTicketClass(String value, String version) {
            super();
            this.version = version;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public String getVersion() {
            return version;
        }

    }

    @XmlAccessorType(value = XmlAccessType.FIELD)
    public static class XmlTicketScope<I extends Identity> implements Serializable {

        private static final long serialVersionUID = 1L;
        @XmlAttribute
        private String scope;
        @XmlAttribute
        private String action;
        @XmlElements({
            @XmlElement(name = "document", type = DocumentId.class),
            @XmlElement(name = "student", type = StudentId.class),
            @XmlElement(name = "unit", type = UnitId.class),
            @XmlElement(name = "record", type = RecordId.class),
            @XmlElement(name = "term", type = TermId.class),
            @XmlElement(name = "signee", type = Signee.class)
        })
        private I value;
        @XmlElement(name = "value")
        private String text;
        @XmlElement(name = "marker")
        @XmlJavaTypeAdapter(XmlMarkerAdapter.class)
        private Marker marker;

        public XmlTicketScope() {
            super();
        }

        public XmlTicketScope(String scope, I identity, String action) {
            this(scope, identity, null, null, action);
        }

        public XmlTicketScope(String scope, String value, String action) {
            this(scope, null, null, value, action);
        }

        public XmlTicketScope(String scope, Marker value, String action) {
            this(scope, null, value, null, action);
        }

        private XmlTicketScope(String scope, I identity, Marker marker, String value, String action) {
            super();
            this.scope = scope;
            this.value = identity;
            this.marker = marker;
            this.text = value;
            this.action = action;
        }

        public String getScope() {
            return scope;
        }

        public String getAction() {
            return action;
        }

        public I getValue() {
            return value;
        }

        public String getTextValue() {
            return text;
        }

        public Marker getMarker() {
            return marker;
        }

    }

}
