/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.impl;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.services.implementation.ui.impl.NewProviderVisualPanel.NewProviderPanel;

@ActionID(category = "Betula",
        id = "org.thespheres.betula.services.implementation.ui.impl.NewProviderAction")
@ActionRegistration(displayName = "#NewProviderAction.title",
        iconBase = "org/thespheres/betula/services/implementation/ui/resources/server.png")
@ActionReference(path = "Menu/File", position = 550)
@Messages({"NewProviderAction.title=Neuer Mandant",
    "NewProviderAction.message.success=Der neue Mandant {0} wurde erfolgreich angelegt."})
public final class NewProviderAction extends BaseNewProviderAction implements ActionListener {

    static final String PROP_HOST = "host";
    static final String PROP_ALIAS = "alias";

    public NewProviderAction() {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        panels.add(new NewProviderPanel());
        final String[] steps = new String[panels.size()];
        for (int i = 0; i < panels.size(); i++) {
            Component c = panels.get(i).getComponent();
            // Default step name to component name of panel.
            steps[i] = c.getName();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
            }
        }
        final WizardDescriptor wiz = new WizardDescriptor(panels.stream().toArray(WizardDescriptor.Panel[]::new));
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle(NbBundle.getMessage(NewProviderAction.class, "NewProviderAction.title"));
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            final String host = (String) wiz.getProperty(NewProviderAction.PROP_HOST);
            if (host == null) {
                return;
            }
            final String aliasProp = (String) wiz.getProperty(NewProviderAction.PROP_ALIAS);
            final String alias = aliasProp != null ? aliasProp : host;
            try {
                newProvider(host, alias);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return;
            }
            final String msg = NbBundle.getMessage(NewProviderAction.class, "NewProviderAction.message.success", host);
            StatusDisplayer.getDefault().setStatusText(msg, StatusDisplayer.IMPORTANCE_ANNOTATION);
        }
    }

}
