/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.clients.termreport;

import java.time.LocalDateTime;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.openide.util.Lookup;
import org.thespheres.betula.TermId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.util.TargetAssessmentEntry;
import org.thespheres.betula.termreport.XmlAssessmentProviderData;
import org.thespheres.betula.ui.util.JAXBUtil;
import org.thespheres.betula.util.LocalDateTimeAdapter;

/**
 *
 * @author boris.heithecker
 */
@JAXBUtil.JAXBRegistration(target = "XmlTermReport")
@XmlRootElement(name = "target-assessment-remote-reference", namespace = "http://www.thespheres.org/xsd/betula/term-report-client.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlRemoteTargetAssessmentReference extends XmlAssessmentProviderData<XmlRemoteTargetAssessmentProvider> {

    @XmlAttribute(name = "node-index")
    private int nodeIndex;
    @XmlAttribute(name = "provider-id")
    private String id;
    @XmlElement(name = "document")
    private DocumentId document;
    @XmlElement(name = "term")
    private TermId term;
    @XmlElement(name = "target", namespace = "http://www.thespheres.org/xsd/betula/container.xsd")
    TargetAssessmentEntry<TermId> target;
    @XmlElement(name = "target-time")
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    LocalDateTime targetAccessTime;

    public XmlRemoteTargetAssessmentReference() {
    }

    public XmlRemoteTargetAssessmentReference(String id, DocumentId d, TermId t) {
        this.id = id;
        this.document = d;
        this.term = t;
    }

    public String getId() {
        return id;
    }

    @Override
    public int getPosition() {
        return nodeIndex;
    }

    @Override
    public void setPosition(int index) {
        this.nodeIndex = index;
    }

    public DocumentId getDocument() {
        return document;
    }

    public TermId getTerm() {
        return term;
    }

    @Override
    public XmlRemoteTargetAssessmentProvider createAssessmentProvider(Lookup context) {
//        if (relativeUri == null) {
//            XmlFileTargetAssessmentProvider.setBrokenLink(this);
//        }
        return new XmlRemoteTargetAssessmentProvider(this, context);
    }
}
