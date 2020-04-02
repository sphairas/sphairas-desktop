/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.acer.remote.ui.action;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

class CreateStaticChannelWizardPanel implements WizardDescriptor.Panel<WizardDescriptor> {

    private CreateStaticChannelVisualPanel component;

    @Override
    public JComponent getComponent() {
        if (component == null) {
            component = new CreateStaticChannelVisualPanel();
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx("help.key.here");
    }

    @Override
    public boolean isValid() {
        CreateStaticChannelVisualPanel cmp = (CreateStaticChannelVisualPanel) getComponent();
        return cmp.valid();
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        CreateStaticChannelVisualPanel cmp = (CreateStaticChannelVisualPanel) getComponent();
        cmp.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        CreateStaticChannelVisualPanel cmp = (CreateStaticChannelVisualPanel) getComponent();
        cmp.addChangeListener(l);
    }

    @Override
    public void readSettings(WizardDescriptor wiz) {
        CreateStaticChannelVisualPanel cmp = (CreateStaticChannelVisualPanel) getComponent();
        String name = (String) wiz.getProperty(CreateStaticChannel.PROP_CHANNEL);
        if (name != null) {
            cmp.setChannelName(name);
        }
        String dn = (String) wiz.getProperty(CreateStaticChannel.PROP_CHANNEL_DISPLAYNAME);
        if (dn != null) {
            cmp.setChannelDisplayName(dn);
        }
        Boolean sc = (Boolean) wiz.getProperty(CreateStaticChannel.PROP_STUDENTSCHANNEL);
        if (sc != null) {
            cmp.setStudentsChannel(sc);
        }
        cmp.setWizardDescriptor(wiz);
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        CreateStaticChannelVisualPanel cmp = (CreateStaticChannelVisualPanel) getComponent();
        wiz.putProperty(CreateStaticChannel.PROP_CHANNEL, cmp.getChannelName());
        wiz.putProperty(CreateStaticChannel.PROP_CHANNEL_DISPLAYNAME, cmp.getChannelDisplayName());
        wiz.putProperty(CreateStaticChannel.PROP_STUDENTSCHANNEL, cmp.isStudentsChannel());
        cmp.setWizardDescriptor(null);
    }

}
