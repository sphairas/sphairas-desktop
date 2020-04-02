package org.thespheres.betula.termreport.model;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import org.thespheres.betula.termreport.XmlAssessmentProviderDataProvider;
import java.io.IOException;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.netbeans.api.project.FileOwnerQuery;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.thespheres.betula.Student;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.Unit;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.TargetAssessment;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.termreport.AssessmentProvider;
import org.thespheres.betula.termreport.AssessmentProviderEnvironment;
import org.thespheres.betula.termreport.TargetAssessmentProvider;
import org.thespheres.betula.termreport.TermReportActions;
import org.thespheres.betula.termreport.module.TRConfig;
import org.thespheres.betula.termreport.xml.XmlTermReportTargetAssessment;
import org.thespheres.betula.ui.ConfigurationPanelLookupHint;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"XmlTargetAssessmentProvider.defaultDisplayName=Zensur"})
public class XmlTargetAssessmentProvider extends TargetAssessmentProvider implements XmlAssessmentProviderDataProvider<TargetAssessmentProvider>, ConfigurationPanelLookupHint {

    private final XmlTermReportTargetAssessment ref;
    private final XmlAssessmentProviderEnvironment env;

    public XmlTargetAssessmentProvider(XmlTermReportTargetAssessment ref, Lookup context) {
        super(ref.getId(), AssessmentProvider.READY);
        this.ref = ref;
        this.env = new XmlAssessmentProviderEnvironment(this, context);
        setDisplayName(this.ref.getDisplayName());
    }

    void setDisplayNameImpl(String displayName) {
        super.setDisplayName(displayName);
        ref.setDisplayName(displayName);
        env.getDataObject().setModified(true);
    }

    public static XmlTargetAssessmentProvider create(TermReportActions context) {
        final String id = TermReportUtilities.findId(context.getTermReport());
        final XmlTermReportTargetAssessment ref = new XmlTermReportTargetAssessment(id);
        ref.setDisplayName(NbBundle.getMessage(XmlTargetAssessmentProvider.class, "XmlTargetAssessmentProvider.defaultDisplayName"));
        final Set<Student> set = Optional.ofNullable(context.getContext().lookup(DataObject.class))
                .map(DataObject::getPrimaryFile)
                .map(f -> FileOwnerQuery.getOwner(f))
                .map(p -> p.getLookup().lookup(Unit.class))
                .map(u -> u.getStudents())
                .orElse(Collections.EMPTY_SET);
        final XmlTargetAssessmentProvider ret = new XmlTargetAssessmentProvider(ref, context.getContext());
        set.stream()
                .map(Student::getStudentId)
                .forEach(sid -> ret.submit(sid, TRConfig.TARGET_PENDING, Timestamp.now()));
        return ret;
    }

    @Override
    public String getContentType() {
        return "XmlTargetAssessmentProvider";
    }

    @Override
    public AssessmentProviderEnvironment getEnvironment() {
        return env;
    }

    @Override
    protected Node createNodeDelegate() {
        return new XmlTargetAssessmentProviderNode(this);
    }

    @Override
    public boolean isEditable() {
        return ref.getPreferredConvention() != null;
    }

    @Override
    public void submit(StudentId student, Grade grade, Timestamp timestamp) {
        Grade old = ref.select(student);
        ref.submit(student, grade, timestamp);
        if (!Objects.equals(old, grade)) {
            env.getDataObject().setModified(true);
        }
    }

    @Override
    public Grade select(StudentId student) {
        return ref.select(student);
    }

    @Override
    public Timestamp timestamp(StudentId student) {
        return ref.timestamp(student);
    }

    @Override
    public Set<StudentId> students() {
        return ref.students();
    }

    @Override
    public String getPreferredConvention() {
        return ref.getPreferredConvention();
    }

    public void setPreferredConvention(String convention) {
        String old = getPreferredConvention();
        ref.setPreferredConvention(convention);
        pSupport.firePropertyChange(TargetAssessment.PROP_PREFERRED_CONVENTION, old, getPreferredConvention());
        if (!Objects.equals(old, getPreferredConvention())) {
            env.getDataObject().setModified(true);
        }
    }

    @Override
    public void addListener(Listener listener) {
        pSupport.addPropertyChangeListener(listener);
        ref.addListener(listener);
    }

    @Override
    public void removeListener(Listener listener) {
        pSupport.removePropertyChangeListener(listener);
        ref.removeListener(listener);
    }

    @Override
    public void remove() throws IOException {
        env.remove();
    }

    @Override
    public XmlTermReportTargetAssessment getXmlAssessmentProviderData() {
        return this.ref;
    }
}
