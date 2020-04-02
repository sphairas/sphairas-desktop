/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.acer.remote.ui.mail;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.thespheres.acer.remote.ui.RemoteMessage;
import org.thespheres.betula.admin.units.util.Util;
import org.thespheres.betula.services.ProviderInfo;

@ActionID(
        category = "Betula",
        id = "org.thespheres.acer.remote.ui.mail.MailMessageAction"
)
@ActionRegistration(
        displayName = "#MailMessageAction.displayName"
)
@ActionReferences({
    @ActionReference(path = "Menu/messages", position = 200000)
    ,
    @ActionReference(path = "Editors/text/acer-edit-message/Popup", position = 1100, separatorBefore = 1000)
    ,
    @ActionReference(path = "Editors/text/acer-edit-message/Toolbars/Default", position = 1100, separatorBefore = 1000)})
@Messages("MailMessageAction.displayName=@ Email")
public final class MailMessageAction implements ActionListener {

    private final RemoteMessage message;
    private ProviderInfo provider;

    public MailMessageAction(RemoteMessage context) {
        this.message = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        provider = message.getRemoteMessagesModel().getProviderInfo();
        Util.RP(provider.getURL()).post(this::doSend);
    }

    private void doSend() {
//        MessageBean bean = provider.lookup(MessageBean.class);
//        long result = bean.enqueueEmail(message.getMessageId());
//        if (result == -1) {
//            //notify user...
//        }
        throw new UnsupportedOperationException("Not implemented!");
    }
}
