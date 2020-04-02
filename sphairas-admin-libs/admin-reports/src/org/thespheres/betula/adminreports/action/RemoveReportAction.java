/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminreports.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.thespheres.betula.adminreports.impl.RemoteEditableReport;

/**
 *
 * @author boris.heithecker
 */
@ActionID(category = "Betula",
        id = "org.thespheres.betula.adminreports.action.RemoveReportAction")
@ActionRegistration(displayName = "#RemoveReportAction.displayName")
@ActionReferences({
    @ActionReference(path = "Editors/text/betula-remote-reports/Popup", position = 1200)})
@NbBundle.Messages("RemoveReportAction.displayName=Bericht löschen")
public class RemoveReportAction implements ActionListener {

    private final RemoteEditableReport er;

    public RemoveReportAction(RemoteEditableReport report) {
        this.er = report;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        er.removeReport();
    }

}
