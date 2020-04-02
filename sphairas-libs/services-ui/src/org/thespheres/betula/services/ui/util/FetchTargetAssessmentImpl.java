/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.util;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.IdentityTargetAssessment.Listener;
import org.thespheres.betula.document.Document.SigneeInfo;
import org.thespheres.betula.document.Document.Validity;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.document.util.TargetAssessmentEntry;
import org.thespheres.betula.services.ui.util.Targets.TargetAssessmentEntryResult;

/**
 *
 * @author boris.heithecker
 */
class FetchTargetAssessmentImpl implements TargetAssessmentEntryResult {

    private final TargetAssessmentEntry<TermId> entry;

    FetchTargetAssessmentImpl(TargetAssessmentEntry<TermId> entry) {
        this.entry = entry;
    }

    @Override
    public String getPreferredConvention() {
        return entry.getPreferredConvention();
    }

    @Override
    public String getTargetType() {
        return entry.getTargetType();
    }

    @Override
    public Map<String, Signee> getSignees() {
        return entry.getValue().getSigneeInfos()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getSignee()));
    }

    @Override
    public boolean isFragment() {
        return entry.getValue().isFragment();
    }

    @Override
    public Validity getDocumentValidity() {
        return entry.getValue().getDocumentValidity();
    }

    @Override
    public SigneeInfo getCreationInfo() {
        return entry.getValue().getCreationInfo();
    }

    @Override
    public Marker[] markers() {
        return entry.getValue().markers();
    }

    @Override
    public Grade select(StudentId student, TermId gradeId) {
        return entry.select(student, gradeId);
    }

    @Override
    public Timestamp timestamp(StudentId student, TermId gradeId) {
        return entry.timestamp(student, gradeId);
    }

    @Override
    public Set<TermId> identities() {
        return entry.identities();
    }

    @Override
    public Set<StudentId> students() {
        return entry.students();
    }

    @Override
    public void submit(StudentId student, TermId gradeId, Grade grade, Timestamp timestamp) {
        entry.submit(student, gradeId, grade, timestamp);
    }

    @Override
    public void addListener(Listener<Grade, TermId> listener) {
        entry.addListener(listener);
    }

    @Override
    public void removeListener(Listener<Grade, TermId> listener) {
        entry.removeListener(listener);
    }

    @Override
    public TargetAssessmentEntry<TermId> getTargetAssessmentEntry() {
        return entry;
    }

}
