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
import org.thespheres.betula.admin.units.RemoteUnitsModel;
import org.thespheres.betula.admin.units.TargetAssessmentSelectionProvider;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.niedersachsen.admin.ui.Constants;

/**
 *
 * @author boris.heithecker
 */
@ActionID(category = "Betula", id = "org.thespheres.betula.niedersachsen.admin.ui.action.SetVorschlagOnEntireTargetView")
@ActionRegistration(displayName = "#SetVorschlagOnEntireTargetView.displayName", asynchronous = true)
@ActionReferences({
    @ActionReference(path = "Loaders/application/betula-unit-context/Actions", position = 33000, separatorBefore = 30000), //    @ActionReference(path = "Editors/application/xml-dtd/Popup", position = 4000)
})
@NbBundle.Messages({"SetVorschlagOnEntireTargetView.displayName=„Vorschlag folgen“ setzen",
    "SetVorschlagOnEntireTargetView.undoName==„Vorschlag“ setzen",
    "SetVorschlagOnEntireTargetView.redoName==„Vorschlag“ setzen"})
//TODO: include term display name in action name, undo redo. no container service! -> only remove entries in target, no version update...
public class SetVorschlagOnEntireTargetView implements ActionListener {
    
    private final RemoteTargetAssessmentDocument rtad;
    private final TermId termId;
    private final PrimaryUnitOpenSupport support;
    private final RemoteUnitsModel rModel;
    
    public SetVorschlagOnEntireTargetView(TargetAssessmentSelectionProvider sp) throws IOException {
        rtad = sp.getLookup().lookup(RemoteTargetAssessmentDocument.class);
        rModel = sp.getLookup().lookup(PrimaryUnitOpenSupport.class).getRemoteUnitsModel();
        termId = sp.getTerm();
        support = sp.getLookup().lookup(PrimaryUnitOpenSupport.class);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (termId != null && rModel != null && rtad != null && support != null) {
            String dn;
            try {
                dn = support.findNamingResolverResult().getResolvedName();
            } catch (IOException ex) {
                dn = support.getUnitId().getId();
            }
            String ln = rtad.getName().getDisplayName(null);
            final CompoundEdit ce = new Edit(dn, ln);
            final Set<StudentId> rtadStuds = rtad.students();
            rModel.getStudents().stream()
                    .map(RemoteStudent::getStudentId)
                    .filter(rtadStuds::contains)
                    .forEach(s -> {
                        final Grade old = rtad.select(s, termId);
                        if (old != null && old.equals(Constants.PENDING)) {
                            rtad.submitUndoable(s, termId, Constants.VORSCHLAG, ce);
                        }
                    });
            ce.end();
            support.getUndoSupport().postEdit(ce);
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
            return NbBundle.getMessage(Edit.class, "SetVorschlagOnEntireTargetView.redoName", unitName, targetName);
        }
        
        @Override
        public String getUndoPresentationName() {
            return NbBundle.getMessage(Edit.class, "SetVorschlagOnEntireTargetView.undoName", unitName, targetName);
        }
        
    }
}
