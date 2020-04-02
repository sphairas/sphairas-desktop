/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport.xml;

import org.thespheres.betula.termreport.XmlAssessmentProviderData;
import java.util.Set;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.openide.util.Lookup;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.TargetAssessment;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.termreport.model.XmlTargetAssessmentProvider;
import org.thespheres.betula.ui.util.JAXBUtil;
import org.thespheres.betula.util.XmlTargetAssessment;

/**
 *
 * @author boris.heithecker
 */
@JAXBUtil.JAXBRegistration(target = "XmlTermReport")
@XmlRootElement(name = "term-report-target-assessment", namespace = "http://www.thespheres.org/xsd/betula/term-report.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlTermReportTargetAssessment extends XmlAssessmentProviderData<XmlTargetAssessmentProvider> implements TargetAssessment<Grade, TargetAssessment.Listener> {

    @XmlElement(name = "display-name")
    private String displayName;
    @XmlAttribute(name = "node-index")
    private int nodeIndex;
    @XmlAttribute(name = "provider-id")
    private String providerId;
    @Deprecated
    @XmlElement(name = "xml-target-assessment")
    private XmlTargetAssessment ta;
    @XmlElement(name = "target-assessment", namespace = "http://www.thespheres.org/xsd/betula/target-assessment.xsd")
    private XmlTargetAssessment ta2;

    public XmlTermReportTargetAssessment() {
    }

    public XmlTermReportTargetAssessment(String providerId) {
        this.providerId = providerId;
        this.ta2 = new XmlTargetAssessment();
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

    @Override
    public void submit(StudentId student, Grade grade, Timestamp timestamp) {
        ta2.submit(student, grade, timestamp);
    }

    @Override
    public Grade select(StudentId student) {
        return ta2.select(student);
    }

    @Override
    public Timestamp timestamp(StudentId student) {
        return ta2.timestamp(student);
    }

    @Override
    public Set<StudentId> students() {
        return ta2.students();
    }

    @Override
    public String getPreferredConvention() {
        return ta2.getPreferredConvention();
    }

    public void setPreferredConvention(String convention) {
        ta2.setPreferredConvention(convention);
    }

    @Override
    public void addListener(Listener listener) {
        ta2.addListener(listener);
    }

    @Override
    public void removeListener(Listener listener) {
        ta2.removeListener(listener);
    }

    @Override
    public XmlTargetAssessmentProvider createAssessmentProvider(Lookup context) {
        return new XmlTargetAssessmentProvider(this, context);
    }

    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        if (ta != null) {
            ta2 = ta;
            ta = null;
        }
    }
}
