/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.services.LocalFileProperties;
import org.thespheres.betula.termreport.TermReportActions;
import org.thespheres.betula.termreport.model.XmlTargetAssessmentProvider;

@ActionID(category = "Betula",
        id = "org.thespheres.betula.termreport.ui.AddTargetAssessmentAction")
@ActionRegistration(displayName = "#AddTargetAssessmentAction.displayName")
@ActionReferences({
    @ActionReference(path = "Loaders/text/term-report-file+xml/Actions", position = 1250, separatorBefore = 1200, separatorAfter = 5000),
    @ActionReference(path = "Loaders/text/term-report-file+xml/Toolbar", position = 5000)})
@Messages("AddTargetAssessmentAction.displayName=Bewertung hinzufügen")
public final class AddTargetAssessmentAction implements ActionListener {

    private final TermReportActions context;

    public AddTargetAssessmentAction(TermReportActions context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        final LocalFileProperties p = context.getProperties();
        String ac = null;
        if (p != null) {
            ac = p.getProperty("preferredConvention");
        }
        XmlTargetAssessmentProvider prov = XmlTargetAssessmentProvider.create(context);
        if (ac != null) {
            prov.setPreferredConvention(ac);
        }
        context.addAssessmentProvider(prov);
    }
}
