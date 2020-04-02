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
import org.thespheres.acer.remote.ui.AbstractMessage;
import org.thespheres.acer.remote.ui.RemoteChannel;

@ActionID(
        category = "Betula",
        id = "org.thespheres.acer.remote.ui.action.PublishMessageAction"
)
@ActionRegistration(
        displayName = "#CTL_PublishMessageAction",
        iconBase = "org/thespheres/acer/remote/ui/resources/mail--plus.png"
)
@ActionReference(path = "Menu/messages", position = 5500, separatorBefore = 5000)
@Messages("CTL_PublishMessageAction=Neue Mitteilung")
public final class PublishMessageAction implements ActionListener {

    private final RemoteChannel context;

    public PublishMessageAction(RemoteChannel context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        AbstractMessage draft = context.createDraftMessage();
        draft.getMessageEditorSupport().open();
    }
}
