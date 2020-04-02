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
import org.thespheres.betula.termreport.TermReportActions;
import org.thespheres.betula.termreport.model.XmlNumberAssessmentProvider;

@ActionID(category = "Betula",
        id = "org.thespheres.betula.termreport.ui.AddNumberAssessmentAction")
@ActionRegistration(displayName = "#AddNumberAssessmentAction.displayName")
@ActionReferences({
    @ActionReference(path = "Loaders/text/term-report-file+xml/Actions", position = 1280, separatorBefore = 1200, separatorAfter = 5000),
    @ActionReference(path = "Loaders/text/term-report-file+xml/Toolbar", position = 6000)})
@Messages("AddNumberAssessmentAction.displayName=Schnitt hinzufügen")
public final class AddNumberAssessmentAction implements ActionListener {

    private final TermReportActions context;

    public AddNumberAssessmentAction(TermReportActions context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) { 
        XmlNumberAssessmentProvider prov = XmlNumberAssessmentProvider.create(context);
        context.addAssessmentProvider(prov);
    }
}
