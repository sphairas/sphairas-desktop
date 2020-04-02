/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport.xml;

import org.thespheres.betula.termreport.XmlAssessmentProviderData;
import java.net.URI;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.openide.util.Lookup;
import org.thespheres.betula.termreport.model.XmlFileTargetAssessmentProvider;
import org.thespheres.betula.ui.util.JAXBUtil;

/**
 *
 * @author boris.heithecker
 */
@JAXBUtil.JAXBRegistration(target = "XmlTermReport")
@XmlRootElement(name = "target-assessment-file-reference", namespace = "http://www.thespheres.org/xsd/betula/term-report.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlFileTargetAssessmentReference extends XmlAssessmentProviderData<XmlFileTargetAssessmentProvider> {

    @XmlElement(name = "project-directory-relative-uri")
    private URI relativeUri;
    @XmlAttribute(name = "node-index")
    private int nodeIndex;
    @XmlAttribute(name = "provider-id")
    private String id;
    @XmlAttribute(name = "provider-id-referenced")
    private String idRef;

    public XmlFileTargetAssessmentReference() {
    }

    public XmlFileTargetAssessmentReference(String id, URI relativeUri, String idRef) {
        this.id = id;
        this.relativeUri = relativeUri;
        this.idRef = idRef;
    }

    public String getId() {
        if (id == null) {
            return getRelativeUri().toString();//legacy case
        }
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

    public URI getRelativeUri() {
        return relativeUri;
    }

    public void setRelativeUri(URI relativUri) {
        this.relativeUri = relativUri;
    }

    public String getReferencedId() {
        return idRef;
    }

    @Override
    public XmlFileTargetAssessmentProvider createAssessmentProvider(Lookup context) {
//        if (relativeUri == null) {
//            XmlFileTargetAssessmentProvider.setBrokenLink(this);
//        }
        return new XmlFileTargetAssessmentProvider(this, context);
    }
}
