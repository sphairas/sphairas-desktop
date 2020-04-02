/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admincontainer;

import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.thespheres.betula.admincontainer.action.SigneeAction;

/**
 *
 * @author boris.heithecker
 */
public class SigneeActions {

    @ActionID(category = "Betula", id = "org.thespheres.betula.admin.container.SetEntitledSigneeAction")
    @ActionRegistration(displayName = "#SetSigneeAction.entitled.signee.name",
            lazy = false)
    @ActionReferences(value = {
        @ActionReference(path = "TargetSigneesTopComponent/Toolbars/entitled-signee", position = 100)})
    @NbBundle.Messages(value = {"SetSigneeAction.entitled.signee.displayName=Eintragungsberechtigter",
        "SetSigneeAction.entitled.signee.name=Unterzeichner setzen"})
    public static final Action createEntitledSigneeAction() {
        return SigneeAction.create("entitled.signee");
    }

    public static Action create(String entitlement) {
        return SigneeAction.create(entitlement);
    }

}
