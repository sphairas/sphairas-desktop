/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;

@ActionID(category = "Betula",
        id = "org.thespheres.betula.admin.units.actions.RefreshAction")
@ActionRegistration(displayName = "#CTL_RefreshAction")
@ActionReferences({
    @ActionReference(path = "Loaders/application/betula-unit-context/Actions", position = 230000, separatorBefore = 200000)})
@Messages("CTL_RefreshAction=Neu laden")
public final class RefreshAction implements ActionListener {

    private final List<RemoteTargetAssessmentDocument> context;

    public RefreshAction(final List<RemoteTargetAssessmentDocument> context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        context.forEach(RemoteTargetAssessmentDocument::refresh);
    }
}
