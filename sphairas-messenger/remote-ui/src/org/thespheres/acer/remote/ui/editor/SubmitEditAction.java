/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.acer.remote.ui.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Betula",
        id = "org.thespheres.acer.remote.ui.editor.SubmitEditAction")
@ActionRegistration(
        displayName = "#CTL_SubmitEditAction",
        iconBase = "org/thespheres/acer/remote/ui/resources/mail-send.png")
@ActionReferences({
    @ActionReference(path = "Menu/messages", position = 200000),
    @ActionReference(path = "Editors/text/acer-edit-message/Popup", position = 200),
    @ActionReference(path = "Editors/text/acer-edit-message/Toolbars/Default", position = 200)})
@Messages("CTL_SubmitEditAction=OK")
public final class SubmitEditAction implements ActionListener {

    private final MessageEditorSupport context;

    public SubmitEditAction(MessageEditorSupport context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        try {
            context.saveAndSubmit();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

    }
}
