/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.acer.remote.ui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.thespheres.acer.remote.ui.RemoteChannel;

@ActionID(category = "Betula",
        id = "org.thespheres.acer.remote.ui.action.RemoveChannelAction")
@ActionRegistration(
        displayName = "#CTL_RemoveChannelAction")
@ActionReference(path = "Menu/messages", position = 4000)
@Messages("CTL_RemoveChannelAction=Adressatenliste löschen")
public final class RemoveChannelAction implements ActionListener {

    private final RemoteChannel context;

    public RemoveChannelAction(RemoteChannel context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        context.remove();
    }
}
