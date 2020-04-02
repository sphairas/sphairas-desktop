/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.admin.units.TargetAssessmentSelectionProvider;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.services.scheme.Terms;
import org.thespheres.betula.services.scheme.spi.Term;

/**
 *
 * @author boris.heithecker
 */
@ActionID(category = "Betula", id = "org.thespheres.betula.admin.units.actions.CopyTargetEntryFromPreviousTerm")
@ActionRegistration(displayName = "#CTL_CopyTargetEntryFromPreviousTerm", asynchronous = true)
@ActionReferences({
    @ActionReference(path = "Loaders/application/betula-unit-context/Actions", position = 14500), //    @ActionReference(path = "Editors/application/xml-dtd/Popup", position = 4000)
})
@NbBundle.Messages({
    "CTL_CopyTargetEntryFromPreviousTerm=Werte aus dem vorhergehenden Halbjahr übertragen"
})
public class CopyTargetEntryFromPreviousTerm implements ActionListener {

    private final RemoteTargetAssessmentDocument rtad;
    private final TermId termId;
    private final TermId termIdBefore;

    public CopyTargetEntryFromPreviousTerm(TargetAssessmentSelectionProvider sp) {
        this.rtad = sp.getLookup().lookup(RemoteTargetAssessmentDocument.class);
        this.termId = sp.getTerm();
        if (termId != null) {
            Term before = Terms.forTermId(new TermId(termId.getAuthority(), termId.getId() - 1));
            termIdBefore = before.getScheduledItemId();
        } else {
            termIdBefore = null;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (termId != null && termIdBefore != null && rtad != null) {
            final Map<StudentId, Grade> transfer = new HashMap<>();
            rtad.students().stream().forEach((s) -> {
                Grade g = rtad.select(s, termIdBefore);
                if (g != null) {
                    transfer.put(s, g);
                }
            });
            transfer.forEach((s, g) -> {
                if (g != null) {
                    rtad.submit(s, termId, g, null);
                }
            });
        }
    }
}
