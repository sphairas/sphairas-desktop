/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.acer.remote.ui.editor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.Presenter;
import org.thespheres.betula.util.CollectionUtil;

@ActionID(
        category = "Betula",
        id = "org.thespheres.acer.remote.ui.editor.SetConfidentialAction"
)
@ActionRegistration(
        displayName = "#SetConfidentialAction.displayName",
        //        iconBase = "org/thespheres/acer/remote/ui/resources/mail-send.png",
        lazy = false
)
@ActionReferences({
    @ActionReference(path = "Menu/Edit", position = 200000),
//    @ActionReference(path = "Editors/text/acer-edit-message/Popup", position = 700),
    @ActionReference(path = "Editors/text/acer-edit-message/Toolbars/Default", position = 700)})
@Messages("SetConfidentialAction.displayName=Vertraulich")
public final class SetConfidentialAction extends AbstractAction implements ContextAwareAction, LookupListener, Presenter.Toolbar {

    private Lookup.Result<MessageEditorSupport> context;
    private final JCheckBox check;

    public SetConfidentialAction() {
        check = new JCheckBox();
        check.setText(NbBundle.getMessage(SetConfidentialAction.class, "SetConfidentialAction.displayName"));
    }

    @SuppressWarnings("LeakingThisInConstructor")
    private SetConfidentialAction(Lookup.Result<MessageEditorSupport> context, JCheckBox check) {
        this.context = context;
        this.check = check;
        this.check.addActionListener(this);
        this.context.addLookupListener(this);
        resultChanged(null);
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        Lookup.Result<MessageEditorSupport> res = actionContext.lookupResult(MessageEditorSupport.class);
        return new SetConfidentialAction(res, check);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        MessageEditorSupport mes = findMessageEditorSupport();
        if (mes != null) {
            JCheckBox cb = (JCheckBox) ev.getSource();
            mes.getMessage().setConfidential(cb.isSelected());
        }
    }

    @Override
    public final void resultChanged(LookupEvent ev) {
        boolean confidential = false;
        MessageEditorSupport mes = findMessageEditorSupport();
        if (mes != null) {
            confidential = mes.getMessage().isConfidential();
            setEnabled(true);
        } else {
            setEnabled(false);
        }
        if (check != null) {
            check.setSelected(confidential);
        }
    }

    private MessageEditorSupport findMessageEditorSupport() {
        if (context != null) {
            return context.allInstances().stream()
                    .map(MessageEditorSupport.class::cast)
                    .collect(CollectionUtil.singleOrNull());
        }
        return null;
    }

    @Override
    public Component getToolbarPresenter() {
        return check;
    }

}
