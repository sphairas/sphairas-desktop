/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.termreport.NumberAssessmentProvider;

@ActionID(
        category = "Betula",
        id = "org.thespheres.betula.termreport.action.DistributeWeightsAction"
)
@ActionRegistration(displayName = "#DistributeWeightsAction.displayName",
        iconBase = "org/thespheres/betula/termreport/resources/edit-alignment-justify-distribute.png",
        surviveFocusChange = true)
@ActionReference(path = "Loaders/text/betula-term-report-number-assessment-context/Actions")
@Messages("DistributeWeightsAction.displayName=Gewichtung gleichmäßig")
public final class DistributeWeightsAction implements ActionListener {

    private final NumberAssessmentProvider context;

    public DistributeWeightsAction(NumberAssessmentProvider context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        int length = context.getProviderReferences().size();
        final double w = 1d / (double) length;
        context.getProviderReferences().stream()
                .forEach(pr -> pr.setWeight(w));
    }
}
