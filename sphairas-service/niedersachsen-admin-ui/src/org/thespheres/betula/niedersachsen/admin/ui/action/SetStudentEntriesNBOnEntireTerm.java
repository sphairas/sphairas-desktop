/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;
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
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.niedersachsen.admin.ui.Constants;

/**
 *
 * @author boris.heithecker
 */
@ActionID(category = "Betula", id = "org.thespheres.betula.niedersachsen.admin.ui.action.SetStudentEntriesNBOnEntireTerm")
@ActionRegistration(displayName = "#SetStudentEntriesNBOnEntireTerm.displayName", asynchronous = true)
@ActionReferences({
    @ActionReference(path = "Loaders/application/betula-unit-context/Actions", position = 30500, separatorBefore = 30000), //    @ActionReference(path = "Editors/application/xml-dtd/Popup", position = 4000)
})
@NbBundle.Messages({"SetStudentEntriesNBOnEntireTerm.displayName=Schüler/in in allen Listen „?“ mit „n.b.“ überschreiben",
    "SetStudentEntriesNBOnEntireTerm.undoName={0} in allen Listen „?“ mit „n.b.“ überschreiben",
    "SetStudentEntriesNBOnEntireTerm.redoName={0} in allen Listen „?“ mit „n.b.“ überschreiben"})
//TODO: include term display name in action name, undo redo. no container service! -> only remove entries in target, no version update...
public class SetStudentEntriesNBOnEntireTerm implements ActionListener {

    private final RemoteUnitsModel rModel;
    private final TermId termId;
    private final RemoteStudent student;
    private final PrimaryUnitOpenSupport support;

    public SetStudentEntriesNBOnEntireTerm(TargetAssessmentSelectionProvider sp) throws IOException {
        rModel = sp.getLookup().lookup(PrimaryUnitOpenSupport.class).getRemoteUnitsModel();
        this.termId = sp.getTerm();
        student = sp.getLookup().lookup(RemoteStudent.class);
        support = sp.getLookup().lookup(PrimaryUnitOpenSupport.class);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (termId != null && rModel != null && student != null && support != null) {
            final CompoundEdit ce = new Edit(student.getFullName());
            rModel.getTargets().stream().forEach(rtad -> {
                Grade old = rtad.select(student.getStudentId(), termId);
                if (old != null && old.equals(Constants.PENDING)) {
                    rtad.submitUndoable(student.getStudentId(), termId, Constants.NB, ce);
                }
            });
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
        public boolean replaceEdit(UndoableEdit anEdit) {
            return super.replaceEdit(anEdit); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getRedoPresentationName() {
            return NbBundle.getMessage(Edit.class, "SetStudentEntriesNBOnEntireTerm.redoName", studentName);
        }

        @Override
        public String getUndoPresentationName() {
            return NbBundle.getMessage(Edit.class, "SetStudentEntriesNBOnEntireTerm.undoName", studentName);
        }

    }
}
