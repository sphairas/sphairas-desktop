/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.model;

import java.util.Objects;
import org.openide.nodes.Node;
import org.thespheres.betula.Student;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.journal.analytics.ParticipantMean;

/**
 *
 * @author boris.heithecker
 */
public abstract class EditableParticipant extends StudentEditor implements JournalAnnotatable {

    private int index;
    protected final EditableJournal<?, ?> ecal;
    private ParticipantMean mean;

    protected EditableParticipant(Student student, EditableJournal<?, ?> ecal) {
        super(student);
        //this.rset = rset;
        this.ecal = ecal;
    }

    public EditableJournal<?, ?> getEditableJournal() {
        return ecal;
    }

    @Override
    public EditableParticipant getAnnotatableParticipant() {
        return this;
    }

    public StudentId getStudentId() {
        return getStudent().getStudentId();
    }

    public abstract Node getNodeDelegate();

    @Override
    protected void setFullName(StringBuilder sb) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getGradeCount(final Grade g) {
        return ecal.getEditableRecords().stream()
                .map(er -> er.getGradeAt(index))
                .filter(Objects::nonNull)
                .filter(g::equals)
                .count();
    }

    public ParticipantMean getWeightedGradesMean() {
        if (mean == null) {
            mean = new ParticipantMean(ecal, this);
        }
        return mean;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    protected void setIndex(int index) {
        this.index = index;
    }

    void remove() {
//        setStudent(null);
    }

}
