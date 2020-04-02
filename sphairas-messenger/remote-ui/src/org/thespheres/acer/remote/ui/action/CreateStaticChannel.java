/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.acer.remote.ui.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.thespheres.acer.remote.ui.RemoteMessagesModel;
import org.thespheres.acer.remote.ui.remoteunits.CreateStudentsChannelAction;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.admin.units.util.Util;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.services.util.Signees;

@NbBundle.Messages({
    "CreateStaticChannel.step=Schritt {0}",
    "CreateStaticChannel.title=Neue statische Adressatenliste"
})
final class CreateStaticChannel implements WizardDescriptor.Iterator<WizardDescriptor> {

    final static String PROP_INCLUDEALL = "includeAll";
    final static String PROP_SIGNEES = "signees";
    final static String PROP_CHANNEL = "channel-name";
    final static String PROP_CHANNEL_DISPLAYNAME = "channel-displayName";
    final static String PROP_SELECTED_SIGNEES = "selecte-signees";
    final static String PROP_STUDENTSCHANNEL = "students-channel";
    private int index;

    private List<WizardDescriptor.Panel<WizardDescriptor>> panels;

    private List<WizardDescriptor.Panel<WizardDescriptor>> getPanels() {
        if (panels == null) {
            panels = new ArrayList<>();
            panels.add(new CreateStaticChannelWizardPanel());
            panels.add(new SetStaticChannelSigneesWizardPanel());
            String[] steps = new String[panels.size()];
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
        }
        return panels;
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return getPanels().get(index);
    }

    @Override
    public String name() {
        return index + 1 + ". from " + getPanels().size();
    }

    @Override
    public boolean hasNext() {
        return index < getPanels().size() - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    // If something changes dynamically (besides moving between panels), e.g.
    // the number of panels changes in response to user input, then use
    // ChangeSupport to implement add/removeChangeListener and call fireChange
    // when needed
    @ActionID(category = "Betula",
            id = "org.thespheres.acer.remote.ui.action.CreateStaticChannel")
    @ActionRegistration(
            displayName = "#CTL_CreateStaticChannelAction")
    @ActionReference(path = "Menu/messages", position = 3000)
    @NbBundle.Messages("CTL_CreateStaticChannelAction=Neuer Adressatenliste")
    public static class Action implements ActionListener {

        private final RemoteMessagesModel context;

        public Action(RemoteMessagesModel context) {
            this.context = context;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            WizardDescriptor wiz = new WizardDescriptor(new CreateStaticChannel());
            Optional<Signees> signees = Signees.get(context.getProviderInfo().getURL());
            if (!signees.isPresent()) {
                return;
            }
            wiz.putProperty(CreateStaticChannel.PROP_SIGNEES, signees.get());
            wiz.setTitleFormat(new MessageFormat(NbBundle.getMessage(CreateStaticChannel.class, "CreateStaticChannel.step")));
            wiz.setTitle(NbBundle.getMessage(CreateStaticChannel.class, "CreateStaticChannel.title"));
            if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
                final String name = (String) wiz.getProperty(CreateStaticChannel.PROP_CHANNEL);
                final String displayName = (String) wiz.getProperty(CreateStaticChannel.PROP_CHANNEL_DISPLAYNAME);
                Boolean sc = (Boolean) wiz.getProperty(CreateStaticChannel.PROP_STUDENTSCHANNEL);
                Boolean includeAll = (Boolean) wiz.getProperty(CreateStaticChannel.PROP_INCLUDEALL);
                if (sc != null && sc) {
                    Util.RP(context.getProviderInfo().getURL()).post(() -> {
                        CreateStudentsChannelAction.updateStudentsChannel(context.getProviderInfo(), new StudentId[0], name, name);
                    });
                } else {
                    Signee[] list = null;
                    if (!includeAll) {
                        list = (Signee[]) wiz.getProperty(CreateStaticChannel.PROP_SELECTED_SIGNEES);
                    }
                    context.createStaticChannel(name, displayName, list);
                }
            }
        }

    }
}
