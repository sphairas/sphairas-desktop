/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.undo.CompoundEdit;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.thespheres.betula.TermId;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.admin.units.RemoteUnitsModel;
import org.thespheres.betula.admin.units.TargetAssessmentSelectionProvider;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;

/**
 *
 * @author boris.heithecker
 */
@ActionID(category = "Betula", id = "org.thespheres.betula.admin.units.actions.RemoveStudentEntriesOnEntireTerm")
@ActionRegistration(displayName = "#RemoveStudentEntriesOnEntireTerm.displayName", asynchronous = true)
@ActionReferences({
    @ActionReference(path = "Loaders/application/betula-unit-context/Actions", position = 30500, separatorBefore = 30000), //    @ActionReference(path = "Editors/application/xml-dtd/Popup", position = 4000)
})
@NbBundle.Messages({"RemoveStudentEntriesOnEntireTerm.displayName=Schüler/in in allen Listen löschen",
    "RemoveStudentEntriesOnEntireTerm.undoName={0} in allen Listen löschen rückgängig machen",
    "RemoveStudentEntriesOnEntireTerm.redoName={0} in allen Listen löschen wiederherstellen"})
//TODO include term display name in action name, undo redo. no container service! -> only remove entries in target, no version update...
public class RemoveStudentEntriesOnEntireTerm implements ActionListener {

    private final RemoteUnitsModel rModel;
    private final TermId termId;
    private final RemoteStudent student;
    private final PrimaryUnitOpenSupport support;

    public RemoveStudentEntriesOnEntireTerm(TargetAssessmentSelectionProvider sp) throws IOException {
        rModel = sp.getLookup().lookup(PrimaryUnitOpenSupport.class).getRemoteUnitsModel();
        this.termId = sp.getTerm();
        student = sp.getLookup().lookup(RemoteStudent.class);
        support = sp.getLookup().lookup(PrimaryUnitOpenSupport.class);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //compound edit
        if (termId != null && rModel != null && student != null && support != null) {
            final CompoundEdit ce = new Edit(student.getFullName());
            rModel.getTargets().stream().forEach(rtad -> rtad.submitUndoable(student.getStudentId(), termId, null, ce));
            ce.end();
            support.getUndoSupport().postEdit(ce);
        }
    }

    private static class Edit extends CompoundEdit {

        private final String studentName;

        private Edit(String studentName) {
            this.studentName = studentName;
        }

        @Override
        public String getRedoPresentationName() {
            return NbBundle.getMessage(Edit.class, "RemoveStudentEntriesOnEntireTerm.redoName", studentName);
        }

        @Override
        public String getUndoPresentationName() {
            return NbBundle.getMessage(Edit.class, "RemoveStudentEntriesOnEntireTerm.undoName", studentName);
        }

    }
}
