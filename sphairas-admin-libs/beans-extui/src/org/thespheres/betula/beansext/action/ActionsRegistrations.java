/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.beansext.action;

import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.thespheres.betula.admincontainer.SigneeActions;

/**
 *
 * @author boris.heithecker
 */
public class ActionsRegistrations {

    @ActionID(category = "Betula",
            id = "org.thespheres.betula.beansext.action.TrustedSigneeAction")
    @ActionRegistration(
            displayName = "#SetSigneeAction.trusted.signee.name",
            lazy = false)
    @ActionReferences({
        @ActionReference(path = "TargetSigneesTopComponent/Toolbars/trusted-signee", position = 100), //    @ActionReference(path = "Shortcuts", name = "D-A"),
    })
    @NbBundle.Messages({"SetSigneeAction.trusted.signee.displayName=Vertrauensperson",
        "SetSigneeAction.trusted.signee.name=Vertauensperson setzen"})
    public static Action createTrustedSigneeAction() {
        final Action ret = SigneeActions.create("trusted.signee");
        ret.putValue("org.thespheres.betula.admin.units.ticketui.fixed-selection", "unselected");
        return ret;
    }

}
