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
import java.util.logging.Logger;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;
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
import org.thespheres.betula.assess.Grade;
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
    "CTL_AllePendingAction=Alle Werte des Kurses zurücksetzen",
    "AllePendingAction.undoName=Alle Werte in Klasse {0}, Liste {1} zurücksetzen",
    "AllePendingAction.redoName=Alle Werte in Klasse {0}, Liste {1} zurücksetzen"
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
            final RemoteUnitsModel rModel;
            if (termId != null && rtad != null && uos != null && (rModel = uos.getRemoteUnitsModel()) != null) {
//                final Set<StudentId> rtadStuds = rtad.students();
//                rModel.getStudents().stream()
//                        .map(RemoteStudent::getStudentId)
//                        .filter(rtadStuds::contains)
//                        .forEach(s -> rtad.submit(s, termId, Constants.PENDING, null));

                String dn;
                try {
                    dn = uos.findNamingResolverResult().getResolvedName();
                } catch (IOException ex) {
                    dn = uos.getUnitId().getId();
                }
                final String ln = rtad.getName().getDisplayName(null);
                final CompoundEdit ce = new Edit(dn, ln);

                final Set<StudentId> rtadStuds = rtad.students();
                rModel.getStudents().stream()
                        .map(RemoteStudent::getStudentId)
                        .filter(rtadStuds::contains)
                        .forEach(s -> {
                            final Grade old = rtad.select(s, termId);
                            if (old != null) {
                                rtad.submitUndoable(s, termId, Constants.PENDING, ce);
                            }
                        });
                ce.end();
                uos.getUndoSupport().postEdit(ce);
            }
        } catch (final IOException ex) {
            Logger.getLogger(AllePendingAction.class.getName()).warning(ex.getLocalizedMessage());
        }

    }

    private static class Edit extends CompoundEdit {

        private final String unitName;
        private final String targetName;

        private Edit(String un, String dn) {
            this.unitName = un;
            this.targetName = dn;
        }

        @Override
        public boolean replaceEdit(UndoableEdit anEdit) {
            return super.replaceEdit(anEdit);
        }

        @Override
        public String getRedoPresentationName() {
            return NbBundle.getMessage(AllePendingAction.class, "AllePendingAction.redoName", unitName, targetName);
        }

        @Override
        public String getUndoPresentationName() {
            return NbBundle.getMessage(AllePendingAction.class, "AllePendingAction.undoName", unitName, targetName);
        }

    }
}
