/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport.module;

import java.util.Set;
import java.util.stream.Collectors;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GradeTargetAssessment;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.termreport.model.XmlTargetAssessmentProvider;

/**
 *
 * @author boris.heithecker
 */
public class TargetAssessmentDelegate implements GradeTargetAssessment {

    private final XmlTargetAssessmentProvider original;

    public TargetAssessmentDelegate(final XmlTargetAssessmentProvider dataobj) {
        this.original = dataobj;
    }

    public XmlTargetAssessmentProvider getOriginal() {
        return original;
    }

    @Override
    public void submit(StudentId student, Grade grade, Timestamp timestamp) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Grade select(StudentId student) {
        return original.select(student);
    }

    @Override
    public Timestamp timestamp(StudentId student) {
        return original.timestamp(student);
    }

    @Override
    public Set<StudentId> students() {
        return original.students().stream().collect(Collectors.toSet());
    }

    @Override
    public String getPreferredConvention() {
        return original.getPreferredConvention();
    }

    @Override
    public void addListener(Listener listener) {
        original.addListener(listener);
    }

    @Override
    public void removeListener(Listener listener) {
        original.removeListener(listener);
    }

}
