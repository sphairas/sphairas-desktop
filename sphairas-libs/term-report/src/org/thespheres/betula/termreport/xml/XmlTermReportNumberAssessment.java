/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport.xml;

import org.thespheres.betula.termreport.XmlAssessmentProviderData;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.openide.util.Lookup;
import org.thespheres.betula.termreport.model.XmlNumberAssessmentProvider;
import org.thespheres.betula.ui.util.JAXBUtil;

/**
 *
 * @author boris.heithecker
 */
@JAXBUtil.JAXBRegistration(target = "XmlTermReport")
@XmlRootElement(name = "term-report-number-assessment", namespace = "http://www.thespheres.org/xsd/betula/term-report.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlTermReportNumberAssessment extends XmlAssessmentProviderData<XmlNumberAssessmentProvider> {

    @XmlElement(name = "display-name")
    private String displayName;
    @XmlAttribute(name = "node-index")
    private int nodeIndex;
    @XmlAttribute(name = "provider-id")
    private String providerId;
    @XmlElement(name = "assessment-reference")
    private Ref[] refs;

    public XmlTermReportNumberAssessment() {
    }

    public XmlTermReportNumberAssessment(String id, Ref[] refs) {
        this.providerId = id;
        this.refs = refs;
    }

    public static XmlTermReportNumberAssessment create(XmlNumberAssessmentProvider np) {
        XmlTermReportNumberAssessment.Ref[] ref = np.getProviderReferences().stream()
                .map(pr -> new XmlTermReportNumberAssessment.Ref(pr.getReferenced().getId(), pr.getWeight()))
                .toArray(XmlTermReportNumberAssessment.Ref[]::new);
        XmlTermReportNumberAssessment ret = new XmlTermReportNumberAssessment(np.getId(), ref);
        ret.setDisplayName(np.getDisplayName());
        return ret;
    }

    public String getId() {
        return providerId;
    }

    @Override
    public int getPosition() {
        return nodeIndex;
    }

    @Override
    public void setPosition(int index) {
        this.nodeIndex = index;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String name) {
        displayName = name;
    }

    public Ref[] getReferences() {
        return refs;
    }

    @Override
    public XmlNumberAssessmentProvider createAssessmentProvider(Lookup context) {
        return new XmlNumberAssessmentProvider(this, context);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Ref {

        @XmlAttribute(name = "provider-id-reference")
        private String idRef;
        @XmlAttribute(name = "weight")
        private double weight;

        public Ref() {
        }

        public Ref(String idref, double weight) {
            this.idRef = idref;
            this.weight = weight;
        }

        public String getIdReference() {
            return idRef;
        }

        public double getWeight() {
            return weight;
        }

    }
}
