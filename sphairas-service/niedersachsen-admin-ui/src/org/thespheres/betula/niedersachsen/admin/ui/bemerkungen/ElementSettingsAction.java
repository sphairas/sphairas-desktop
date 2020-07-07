/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.bemerkungen;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.thespheres.betula.niedersachsen.admin.ui.bemerkungen.EditBemerkungenSetRootChildren.ElementNode;
import org.thespheres.betula.niedersachsen.zeugnis.TermReportNoteSetTemplate.Element;

@ActionID(category = "Edit",
        id = "org.thespheres.betula.niedersachsen.admin.ui.bemerkungen.ElementSettingsAction")
@ActionRegistration(displayName = "#CTL_ElementSettingsAction")
@Messages("CTL_ElementSettingsAction=Einstellungen")
public final class ElementSettingsAction implements ActionListener {

    private final Element context;

    public ElementSettingsAction(final Element context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        final ElementSettingsPanel panel = new ElementSettingsPanel(context);
        final DialogDescriptor desc = new DialogDescriptor(panel, NbBundle.getMessage(ElementSettingsAction.class, "CTL_ElementSettingsAction"));
        DialogDisplayer.getDefault().notify(desc);
        if ((int) desc.getValue() == JOptionPane.OK_OPTION) {
            final String name = panel.getNameValue();
            if (name != null) {
                this.context.setElementDisplayName(name);
            }
            this.context.setMultiple(panel.isMultipleSelection());
            if (panel.isRequired() != null) {
                this.context.setNillable(!panel.isRequired());
            }
            final EditBemerkungenEnv env = Utilities.actionsGlobalContext().lookup(EditBemerkungenEnv.class);
            if (env != null) {
                env.setModified("set");
            }
            final ElementNode node = Utilities.actionsGlobalContext().lookup(ElementNode.class);
            if (node != null) {
                node.updateIcon();
                final EditBemerkungenSetRootChildren ch = (EditBemerkungenSetRootChildren) node.getParentNode().getChildren();
                ch.update();
            }

        }
    }
}
