/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Set;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.admin.units.RemoteUnitsModel;
import org.thespheres.betula.admin.units.TargetAssessmentSelectionProvider;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.niedersachsen.admin.ui.Constants;

/**
 *
 * @author boris.heithecker
 */
@ActionID(category = "Betula", id = "org.thespheres.betula.niedersachsen.admin.ui.action.AllePendingAction")
@ActionRegistration(displayName = "#CTL_AllePendingAction", surviveFocusChange = true, asynchronous = true)
@ActionReferences({
    @ActionReference(path = "Loaders/application/betula-unit-context/Actions", position = 10500, separatorBefore = 10000), //    @ActionReference(path = "Editors/application/xml-dtd/Popup", position = 4000)
})
@NbBundle.Messages({
    "CTL_AllePendingAction=Alle Werte zurücksetzen"
})
public class AllePendingAction implements ActionListener {

    private final RemoteTargetAssessmentDocument rtad;
    private final TermId termId;
    private final PrimaryUnitOpenSupport uos;

    public AllePendingAction(TargetAssessmentSelectionProvider sp) {
        this.rtad = sp.getLookup().lookup(RemoteTargetAssessmentDocument.class);
        this.termId = sp.getTerm();
        this.uos = sp.getLookup().lookup(PrimaryUnitOpenSupport.class);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            RemoteUnitsModel rModel;
            if (termId != null && rtad != null && uos != null && (rModel = uos.getRemoteUnitsModel()) != null) {
                final Set<StudentId> rtadStuds = rtad.students();
                rModel.getStudents().stream()
                        .map(RemoteStudent::getStudentId)
                        .filter(rtadStuds::contains)
                        .forEach(s -> rtad.submit(s, termId, Constants.PENDING, null));
            }
        } catch (IOException ex) {
        }
    }
}
