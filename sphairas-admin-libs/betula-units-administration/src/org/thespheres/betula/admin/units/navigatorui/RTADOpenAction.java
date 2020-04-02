/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.navigatorui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.admin.units.ui.TargetsSelectionOpenAction;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.util.CollectionUtil;

@ActionID(category = "Betula",
        id = "org.thespheres.betula.admin.units.navigatorui.RTADOpenAction")
@ActionRegistration(displayName = "org.thespheres.betula.admin.units.ui.Bundle#CTL_TargetsSelectionOpenAction")
@ActionReference(path = "Loaders/application/betula-remote-target-assessment-document/Actions", position = 700, separatorAfter = 750)
public final class RTADOpenAction implements ActionListener {

    private final List<RemoteTargetAssessmentDocument> context;

    public RTADOpenAction(List<RemoteTargetAssessmentDocument> context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        final String provider = context.stream()
                .map(RemoteTargetAssessmentDocument::getProvider)
                .collect(CollectionUtil.singleOrNull());
        if (provider != null) {
            final Set<DocumentId> l = context.stream()
                    .map(RemoteTargetAssessmentDocument::getDocumentId)
                    .collect(Collectors.toSet());
            TargetsSelectionOpenAction.actionPerformed(l, provider);
        }
    }
}
