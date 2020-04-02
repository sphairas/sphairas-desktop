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
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.services.util.Signees;

public class SetStaticChannelSigneesWizardPanel implements WizardDescriptor.Panel<WizardDescriptor> {

    private SetStaticChannelSigneesVisualPanel component;

    @Override
    public JComponent getComponent() {
        if (component == null) {
            component = new SetStaticChannelSigneesVisualPanel();
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
        return true;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    @Override
    public void readSettings(WizardDescriptor wiz) {
        SetStaticChannelSigneesVisualPanel cmp = (SetStaticChannelSigneesVisualPanel) getComponent();
        Signees signees = (Signees) wiz.getProperty(CreateStaticChannel.PROP_SIGNEES);
        cmp.setSignees(signees);
        Signee[] list = (Signee[]) wiz.getProperty(CreateStaticChannel.PROP_SELECTED_SIGNEES);
        if (list != null) {
            cmp.setSelectedSignees(list);
        }
        Boolean includeAll = (Boolean) wiz.getProperty(CreateStaticChannel.PROP_INCLUDEALL);
        if (includeAll != null && includeAll) {
            cmp.setIncludeAll(true);
        }
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        SetStaticChannelSigneesVisualPanel cmp = (SetStaticChannelSigneesVisualPanel) getComponent();
        Signee[] list = cmp.getSelectedSignees();
        if (list != null) {
            wiz.putProperty(CreateStaticChannel.PROP_SELECTED_SIGNEES, list);
        }
        boolean includeAll = cmp.isIncludeAll();
        wiz.putProperty(CreateStaticChannel.PROP_INCLUDEALL, includeAll);
    }

}
