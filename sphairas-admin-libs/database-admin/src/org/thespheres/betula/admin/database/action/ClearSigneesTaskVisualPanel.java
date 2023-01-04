/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.database.action;

import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ChangeListener;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.admin.database.DbAdminServiceProvider;
import org.thespheres.betula.database.DBAdminTask;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.scheme.spi.Term;

/**
 *
 * @author boris.heithecker
 */
@Messages({"ClearSigneesTaskVisualPanel.step.name=Parameter"})
class ClearSigneesTaskVisualPanel extends javax.swing.JPanel {

    private final DefaultComboBoxModel<ProviderInfo> providerModel = new DefaultComboBoxModel<>();
    private final StringValue providerConverter = o -> o instanceof ProviderInfo ? ((ProviderInfo) o).getDisplayName() : "---";
    private final AdminTaskTermModel termsModel = new AdminTaskTermModel();
    private final ChangeSupport cSupport = new ChangeSupport(this);

    ClearSigneesTaskVisualPanel() {
        initComponents();
        DbAdminServiceProvider.findAllProviders().stream()
                .forEach(providerModel::addElement);
        this.providerComboBox.setModel(providerModel);
        this.providerComboBox.setRenderer(new DefaultListRenderer(providerConverter));
        this.termComboBox.setModel(termsModel);
        this.termComboBox.setRenderer(new DefaultListRenderer(termsModel));
        this.providerSelected(null);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ClearSigneesTaskVisualPanel.class, "ClearSigneesTaskVisualPanel.step.name");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        providerComboBox = new org.jdesktop.swingx.JXComboBox();
        providerLabel = new javax.swing.JLabel();
        termComboBox = new org.jdesktop.swingx.JXComboBox();
        termLabel = new javax.swing.JLabel();
        emptyTargetsOnlyCheckBox = new javax.swing.JCheckBox();
        dryRunCheckBox = new javax.swing.JCheckBox();

        providerComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                providerSelected(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(providerLabel, org.openide.util.NbBundle.getMessage(ClearSigneesTaskVisualPanel.class, "ClearSigneesTaskVisualPanel.providerLabel.text")); // NOI18N

        termComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                termComboBoxproviderSelected(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(termLabel, org.openide.util.NbBundle.getMessage(ClearSigneesTaskVisualPanel.class, "ClearSigneesTaskVisualPanel.termLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(emptyTargetsOnlyCheckBox, org.openide.util.NbBundle.getMessage(ClearSigneesTaskVisualPanel.class, "ClearSigneesTaskVisualPanel.emptyTargetsOnlyCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(dryRunCheckBox, org.openide.util.NbBundle.getMessage(ClearSigneesTaskVisualPanel.class, "ClearSigneesTaskVisualPanel.dryRunCheckBox.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(termLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 32, Short.MAX_VALUE)
                        .addComponent(termComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(providerLabel)
                        .addGap(30, 30, 30)
                        .addComponent(providerComboBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(emptyTargetsOnlyCheckBox)
                            .addComponent(dryRunCheckBox))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(providerLabel)
                    .addComponent(providerComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(termComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(termLabel))
                .addGap(18, 18, 18)
                .addComponent(emptyTargetsOnlyCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(dryRunCheckBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void providerSelected(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_providerSelected
        final ProviderInfo provider = (ProviderInfo) this.providerComboBox.getSelectedItem();
        termsModel.initializeModel(provider, null);
        cSupport.fireChange();
    }//GEN-LAST:event_providerSelected

    private void termComboBoxproviderSelected(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_termComboBoxproviderSelected
        // TODO add your handling code here:
    }//GEN-LAST:event_termComboBoxproviderSelected


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox dryRunCheckBox;
    private javax.swing.JCheckBox emptyTargetsOnlyCheckBox;
    private org.jdesktop.swingx.JXComboBox providerComboBox;
    private javax.swing.JLabel providerLabel;
    private org.jdesktop.swingx.JXComboBox termComboBox;
    private javax.swing.JLabel termLabel;
    // End of variables declaration//GEN-END:variables

    static class ClearSigneesTaskPanel implements WizardDescriptor.Panel<WizardDescriptor> {

        /**
         * The visual component that displays this panel. If you need to access
         * the component from this class, just use getComponent().
         */
        private ClearSigneesTaskVisualPanel component;

        // Get the visual component for the panel. In this template, the component
        // is kept separate. This can be more efficient: if the wizard is created
        // but never displayed, or not all panels are displayed, it is better to
        // create only those which really need to be visible.
        @Override
        public ClearSigneesTaskVisualPanel getComponent() {
            if (component == null) {
                component = new ClearSigneesTaskVisualPanel();
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
            return getComponent().termsModel.getSelectedItem() instanceof Term;
        }

        @Override
        public void addChangeListener(final ChangeListener l) {
            getComponent().cSupport.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(final ChangeListener l) {
            getComponent().cSupport.removeChangeListener(l);
        }

        @Override
        public void readSettings(final WizardDescriptor wiz) {
            final DBAdminTask task = (DBAdminTask) wiz.getProperty(ClearSigneesTask.PROP_TASK);
            final boolean onlyEmpty = task.getArg("if-target-empty", Boolean.class, true);
            getComponent().emptyTargetsOnlyCheckBox.setSelected(onlyEmpty);
            final boolean dryRun = task.getArg("dry-run", Boolean.class, false);
            getComponent().dryRunCheckBox.setSelected(dryRun);
        }

        @Override
        public void storeSettings(final WizardDescriptor wiz) {
            wiz.putProperty(ClearSigneesTask.PROP_PROVIDER, getComponent().providerModel.getSelectedItem());
            final DBAdminTask task = (DBAdminTask) wiz.getProperty(ClearSigneesTask.PROP_TASK);
            final Term t = (Term) getComponent().termsModel.getSelectedItem();
            task.setArg("term", t.getScheduledItemId());
            final boolean onlyEmpty = getComponent().emptyTargetsOnlyCheckBox.isSelected();
            task.setArg("if-target-empty", onlyEmpty);
            final boolean dryRun = getComponent().dryRunCheckBox.isSelected();
            task.setArg("dry-run", dryRun);
        }
    }
}
