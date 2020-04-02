/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.kgs.ui;

import org.jdesktop.swingx.JXTextField;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.ui.ConfigurationPanelComponent;
import org.thespheres.betula.ui.ConfigurationPanelComponentProvider;
import org.thespheres.betula.ui.ConfigurationPanelContentTypeRegistration;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
@Messages({"StudentSGLConfigPanel.name=Schulzweig"})
public class StudentSGLConfigPanel extends ConfigurationPanelComponent implements LookupListener {

    private Lookup.Result<RemoteStudent> result;
    private final JXTextField configPanel;

    @SuppressWarnings({"LeakingThisInConstructor"})
    StudentSGLConfigPanel(JXTextField component) {
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
        result = context.lookupResult(RemoteStudent.class);
        result.addLookupListener(this);
        updateText(true);
    }

    private void updateText(final boolean clear) {
        final RemoteStudent current = result.allInstances().stream()
                .collect(CollectionUtil.singleOrNull());
        if (current != null) {
            String text = null;
            final Marker sgl = current.getClientProperty("sgl", Marker.class);
            if (sgl != null) {
                text = sgl.getLongLabel();
            }
            configPanel.setText(text);
        } else if (clear) {
            configPanel.setText(null);
        }
    }

    @Override
    public synchronized void resultChanged(LookupEvent ev) {
        updateText(false);
    }

    @ConfigurationPanelContentTypeRegistration(contentType = "RemoteStudent", position = 5000)
    public static class Registration implements ConfigurationPanelComponentProvider {

        @Override
        public ConfigurationPanelComponent createConfigurationPanelComponent() {
            final JXTextField panel = new JXTextField();
            panel.setEditable(false);
            final String n = NbBundle.getMessage(StudentSGLConfigPanel.class, "StudentSGLConfigPanel.name");
            panel.setName(n);
            return new StudentSGLConfigPanel(panel);
        }
    }

}
