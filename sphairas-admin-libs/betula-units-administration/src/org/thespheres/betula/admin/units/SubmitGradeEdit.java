/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units;

import java.util.Objects;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.assess.Grade;

/**
 *
 * @author boris.heithecker
 */
@Messages({"SubmitGradeEdit.name=Eintrag {0} für {1}",
    "SubmitGradeEdit.remove.name=Eintrag löschen für {0}",
    "SubmitGradeEdit.message.willNotUndoAfterOtherEdit=Der Wert wurde zwischenzeitlich noch einmal geändert; der aktuelle Wert entspricht nicht mehr der Eingabe.",
    "SubmitGradeEdit.message.willNotUndoUnconfirmedEntry=Die Eingabe ist noch nicht bestätigt. Bitte warten, bis das Feld nicht mehr rot hinterlegt ist."})
class SubmitGradeEdit extends AbstractUndoableEdit {

    private final RemoteTargetAssessmentDocument source;
    private final StudentId student;
    private final TermId term;
    private final Grade overridden;
    private final Grade override;

    SubmitGradeEdit(RemoteTargetAssessmentDocument source, StudentId sid, TermId i, Grade old, Grade newGrade) {
        this.source = source;
        this.student = sid;
        this.term = i;
        this.overridden = old;
        this.override = newGrade;
    }

    @Override
    public String getRedoPresentationName() {
        return getName();
    }

    @Override
    public String getUndoPresentationName() {
        return getName();
    }

    private String getName() {
        RemoteStudent s = RemoteStudents.find(source.provider, student);
        if (override != null) {
            return NbBundle.getMessage(SubmitGradeEdit.class, "SubmitGradeEdit.name", override.getShortLabel(), s.getFullName());
        } else {
            return NbBundle.getMessage(SubmitGradeEdit.class, "SubmitGradeEdit.remove.name", s.getFullName());
        }
    }

//    @Override
//    public boolean canUndo() {
//        boolean ret = super.canUndo();
//        return ret && isOverrideConfirmed() && isOverrideValid();
//    }
    private boolean isOverrideConfirmed() {
        return !source.selectGradeAccess(student, term)
                .map(RemoteGradeEntry::isUnconfirmed)
                .orElse(Boolean.FALSE);
    }

    private boolean isOverrideValid() {
        Grade current = source.selectGradeAccess(student, term)
                .map(RemoteGradeEntry::getGrade)
                .orElse(null);
        return Objects.equals(current, override);
    }

    @Override
    public void undo() throws CannotUndoException {
        if (!isOverrideConfirmed()) {
            final String msg = NbBundle.getMessage(SubmitGradeEdit.class, "SubmitGradeEdit.message.willNotUndoUnconfirmedEntry");
            class UnconfirmedEditException extends CannotUndoException {

                @Override
                public String getMessage() {
                    return msg;
                }

            }
            UnconfirmedEditException ret = new UnconfirmedEditException();
            throw ret;
        }
        if (!isOverrideValid()) {
            final String msg = NbBundle.getMessage(SubmitGradeEdit.class, "SubmitGradeEdit.message.willNotUndoAfterOtherEdit");
            class AfterEditException extends CannotUndoException {

                @Override
                public String getMessage() {
                    return msg;
                }

            }
            AfterEditException ret = new AfterEditException();
            throw ret;
        }
        super.undo();
        source.submit(student, term, overridden, null);
    }

    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        source.submit(student, term, override, null);
    }

    public RemoteTargetAssessmentDocument getSource() {
        return source;
    }

    public StudentId getStudent() {
        return student;
    }

    public TermId getTerm() {
        return term;
    }

    public Grade getOverridden() {
        return overridden;
    }

    public Grade getOverride() {
        return override;
    }

}
