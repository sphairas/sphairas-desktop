/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.admin.units.TargetAssessmentSelectionProvider;

/**
 *
 * @author boris.heithecker
 */
@ActionID(category = "Betula", id = "org.thespheres.betula.admin.units.actions.RemoveTargetEntryAction")
@ActionRegistration(displayName = "#RemoveTargetEntryAction.displayName", asynchronous = true)
@ActionReferences({
    @ActionReference(path = "Loaders/application/betula-unit-context/Actions", position = 6500), //    @ActionReference(path = "Editors/application/xml-dtd/Popup", position = 4000)
})
@NbBundle.Messages({"RemoveTargetEntryAction.displayName=Eintrag löschen"})
public class RemoveTargetEntryAction implements ActionListener {

    private final RemoteTargetAssessmentDocument rtad;
    private final TermId termId;
    private final StudentId studentId;

    public RemoveTargetEntryAction(TargetAssessmentSelectionProvider sp) {
        this.rtad = sp.getLookup().lookup(RemoteTargetAssessmentDocument.class);
        this.termId = sp.getTerm();
        RemoteStudent rs = sp.getLookup().lookup(RemoteStudent.class);
        this.studentId = rs != null ? rs.getStudentId() : null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (termId != null && studentId != null && rtad != null) {
            rtad.submitUndoable(studentId, termId, null, null);
        }
    }
}
