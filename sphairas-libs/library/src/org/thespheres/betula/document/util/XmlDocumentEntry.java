/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document.util;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.DocumentEntry;
import org.thespheres.betula.document.DocumentId;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "betula-xml-document", namespace = "http://www.thespheres.org/xsd/betula/container.xsd")
@XmlType(name = "xmlDocumentEntryType", namespace = "http://www.thespheres.org/xsd/betula/container.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlDocumentEntry extends DocumentEntry<GenericXmlDocument> {

    public static final String PROP_REPORT_DATA = "report-data";

    public XmlDocumentEntry() {
    }

    @SuppressWarnings("OverridableMethodCallInConstructor")
    private XmlDocumentEntry(Action action, DocumentId id, GenericXmlDocument doc) {
        super(action, id);
        setValue(doc);
    }

    public XmlDocumentEntry(DocumentId id, Action action, boolean fragment) {
        this(action, id, new GenericXmlDocument(fragment));
    }

    @Override
    public GenericXmlDocument getValue() {
        return (GenericXmlDocument) super.getValue();
    }

    public org.w3c.dom.Element getReportDataElement() {
        return getValue().getContentElement(XmlDocumentEntry.PROP_REPORT_DATA);
    }

    public void setReportDataElement(org.w3c.dom.Element node) {
        getValue().setContent(XmlDocumentEntry.PROP_REPORT_DATA, node);
    }
}
