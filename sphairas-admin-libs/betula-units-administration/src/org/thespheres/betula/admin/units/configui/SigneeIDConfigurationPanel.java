/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.configui;

import org.jdesktop.swingx.JXTextField;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.thespheres.betula.admin.units.RemoteSignee;
import org.thespheres.betula.ui.ConfigurationPanelComponent;
import org.thespheres.betula.ui.ConfigurationPanelContentTypeRegistration;
import org.thespheres.betula.ui.ConfigurationPanelComponentProvider;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"SigneeIDConfigurationPanel.name=ID"})
public class SigneeIDConfigurationPanel extends ConfigurationPanelComponent implements LookupListener {

    private Lookup.Result<RemoteSignee> result;
    private final JXTextField configPanel;

    @SuppressWarnings({"LeakingThisInConstructor"})
    SigneeIDConfigurationPanel(JXTextField component) {
        super(component);
        configPanel = component;
    }

    @Override
    public synchronized void panelDeactivated() {
        updateText(true);
    }

    @Override
    public synchronized void panelActivated(Lookup context) {
        if (result != null) {
            result.removeLookupListener(this);
        }
        result = context.lookupResult(RemoteSignee.class);
        result.addLookupListener(this);
        updateText(true);
    }

    private void updateText(final boolean clear) {
        final RemoteSignee current = result.allInstances().stream()
                .collect(CollectionUtil.singleOrNull());
        if (current != null) {
            String text = current.getSignee().toString();
            configPanel.setText(text);
        } else if (clear) {
            configPanel.setText(null);
        }
    }

    @Override
    public synchronized void resultChanged(LookupEvent ev) {
        updateText(false);
    }

    @ConfigurationPanelContentTypeRegistration(contentType = "RemoteSignee", position = 100)
    public static class Registration implements ConfigurationPanelComponentProvider {

        @Override
        public ConfigurationPanelComponent createConfigurationPanelComponent() {
            final JXTextField panel = new JXTextField();
            panel.setEditable(false);
            final String n = NbBundle.getMessage(SigneeIDConfigurationPanel.class, "SigneeIDConfigurationPanel.name");
            panel.setName(n);
            return new SigneeIDConfigurationPanel(panel);
        }
    }

}
