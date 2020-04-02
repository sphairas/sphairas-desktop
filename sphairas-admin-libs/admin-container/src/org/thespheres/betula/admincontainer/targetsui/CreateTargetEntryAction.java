/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admincontainer.targetsui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.Level;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.admin.units.AbstractUnitOpenSupport;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.admin.units.TargetAssessmentSelectionProvider;
import org.thespheres.betula.admincontainer.util.TargetsUtil;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;

/**
 *
 * @author boris.heithecker
 */
@ActionID(category = "Betula", id = "org.thespheres.betula.admincontainer.targetsui.CreateTargetEntryAction")
@ActionRegistration(displayName = "#CTL_CreateTargetEntryAction", asynchronous = true)
@ActionReferences({
    @ActionReference(path = "Loaders/application/betula-unit-context/Actions", position = 5500, separatorBefore = 5000), //    @ActionReference(path = "Editors/application/xml-dtd/Popup", position = 4000)
})
@NbBundle.Messages({
    "CTL_CreateTargetEntryAction=Neuer Eintrag"
})
public class CreateTargetEntryAction implements ActionListener {

//    private final static Grade pending = GradeFactory.find("niedersachsen.ersatzeintrag", "pending");
    private final RemoteTargetAssessmentDocument rtad;
    private final TermId termId;
    private final StudentId studentId;
    private Grade pendingGrade;
    
    public CreateTargetEntryAction(TargetAssessmentSelectionProvider sp) {
        this.rtad = sp.getLookup().lookup(RemoteTargetAssessmentDocument.class);
        this.termId = sp.getTerm();
        final RemoteStudent rs = sp.getLookup().lookup(RemoteStudent.class);
        final AbstractUnitOpenSupport uos = sp.getLookup().lookup(AbstractUnitOpenSupport.class);
        if (uos != null) {
            try {
                final ConfigurableImportTarget it = TargetsUtil.findCommonImportTarget(uos);
                if (it != null) {
                    pendingGrade = it.getDefaultValue(null, null);
                }
            } catch (IOException ex) {
                PlatformUtil.getCodeNameBaseLogger(CreateTargetEntryAction.class).log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
        }
        this.studentId = rs != null ? rs.getStudentId() : null;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (termId != null && studentId != null && rtad != null && pendingGrade != null) {
            rtad.submit(studentId, termId, pendingGrade, null);
        }
    }
}
