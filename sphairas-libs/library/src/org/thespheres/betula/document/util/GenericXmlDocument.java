package org.thespheres.betula.document.util;

import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.document.Description;
import org.thespheres.betula.document.Document;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.util.XmlZonedDateTime;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "generic-document", namespace = "http://www.thespheres.org/xsd/betula/container.xsd")
@XmlType(name = "genericXmlDocumentType",
        propOrder = {"isFragment", "docClass", "signeeInfos", "creationInfo", "validity"})
@XmlAccessorType(value = XmlAccessType.FIELD)
public class GenericXmlDocument extends XmlContent implements Document, Serializable {

    private static final long serialVersionUID = 1L;
    //declared-document-id
//    @XmlElement(name = "document")
//    private DocumentId preferredId;
    @XmlAttribute(name = "fragment", required = false)
    private boolean isFragment;
    @XmlElement(name = "creator")
    private XmlSigneeInfo creationInfo;
    @XmlElement(name = "valid")
    private XmlValidity validity;
    @XmlElement(name = "document-class")
    private XmlDocumentClass docClass;
    @XmlElement(name = "signee-infos")
    @XmlJavaTypeAdapter(value = SigneeMapAdapter.class)
    private Map<String, SigneeInfo> signeeInfos;

    /*
     * For unmarshalling only!
     */
    public GenericXmlDocument() {
        super();
    }

    public GenericXmlDocument(boolean fragment) {
        super();
//        this.preferredId = id;
        this.isFragment = fragment;
    }

//    public DocumentId getPreferredDocumentId() {
//        return preferredId;
//    }
//    public void setPreferredDocumentId(DocumentId documentId) {
//        //only afterUnmarshll in GenericXmlDocument
//        this.preferredId = documentId;
//    }
    @Override
    public boolean isFragment() {
        return isFragment;
    }

    public void setFragment(boolean fragment) {
        this.isFragment = fragment;
    }

    @Override
    public SigneeInfo getCreationInfo() {
        if (creationInfo == null) {
            creationInfo = new XmlSigneeInfo(Document.CREATOR);
        }
        return creationInfo;
    }

    public void setDocumentInfo(XmlSigneeInfo info) {
        creationInfo = info;
    }

    @Override
    public Validity getDocumentValidity() {
        if (validity == null) {
            validity = new XmlValidity();
        }
        return validity;
    }

    public void setDocumentValidity(ZonedDateTime date) {
        validity = new XmlValidity(date);
    }

    public XmlDocumentClass getDocClass() {
        return docClass;
    }

    public void setDocClass(XmlDocumentClass docClass) {
        this.docClass = docClass;
    }

    public Map<String, SigneeInfo> getSigneeInfos() {
        if (signeeInfos == null) {
            signeeInfos = new HashMap<>();
        }
        return signeeInfos;
    }

    public SigneeInfo addSigneeInfo(String entitlement, final Signee signee) {
        return getSigneeInfos().compute(entitlement, (e, si) -> new XmlSigneeInfo(e, signee));
    }

    @XmlAccessorType(value = XmlAccessType.FIELD)
    public static class XmlDocumentClass implements Serializable {

        private static final long serialVersionUID = 1L;
        @XmlAttribute
        private String revision;
        @XmlValue
        private String value;

        public XmlDocumentClass() {
            super();
        }

        public XmlDocumentClass(String value, String revision) {
            super();
            this.revision = revision;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public String getRevision() {
            return revision;
        }
    }

    @XmlAccessorType(value = XmlAccessType.FIELD)
    public static class XmlSigneeInfo implements SigneeInfo, Serializable {

        private static final long serialVersionUID = 1L;
        @XmlAttribute(name = "signee-entitlement", required = true)
        private String type;
        @XmlAttribute(name = "sign-time")
        private Timestamp time;
        @XmlElement(name = "signee")
        private Signee signee;
        @XmlElement(name = "description")
        private List<Description> description = new ArrayList<>();

        public XmlSigneeInfo() {
            super();
        }

        XmlSigneeInfo(String entitl) {
            super();
            this.type = entitl;
        }

        XmlSigneeInfo(String entitl, Signee signee) {
            super();
            this.type = entitl;
            this.signee = signee;
            this.time = Timestamp.now();
        }

        static XmlSigneeInfo create(String entitl, SigneeInfo orig) {
            XmlSigneeInfo si = new XmlSigneeInfo(entitl);
            si.signee = orig.getSignee();
            si.time = orig.getTimestamp();
            return si;
        }

        public String getEntitlement() {
            return type;
        }

        @Override
        public Timestamp getTimestamp() {
            return time;
        }

        @Override
        public Signee getSignee() {
            return signee;
        }
    }

    @XmlAccessorType(value = XmlAccessType.FIELD)
    static class XmlValidity extends XmlZonedDateTime implements Validity, Serializable {

        private static final long serialVersionUID = 1L;

        public XmlValidity() {
            super();
        }

        XmlValidity(Date d) {
            this(ZonedDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault()));
        }

        XmlValidity(ZonedDateTime zdt) {
            super(zdt);
        }

        @Override
        public boolean isValid() {
            return getZonedDateTime() == null ? true : ZonedDateTime.now().isBefore(getZonedDateTime());
        }

        @Override
        public ZonedDateTime getExpirationDate() {
            return getZonedDateTime();
        }
    }

}
