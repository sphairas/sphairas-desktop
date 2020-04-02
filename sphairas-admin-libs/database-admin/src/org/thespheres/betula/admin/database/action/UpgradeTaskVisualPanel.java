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
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.thespheres.betula.admin.database.DbAdminServiceProvider;
import org.thespheres.betula.database.DBAdminTask;

/**
 *
 * @author boris.heithecker
 */
class UpgradeTaskVisualPanel extends javax.swing.JPanel {

    private final DefaultComboBoxModel<DbAdminServiceProvider> providerModel = new DefaultComboBoxModel<>();
    private final StringValue providerConverter = o -> o instanceof DbAdminServiceProvider ? ((DbAdminServiceProvider) o).getInfo().getDisplayName() : "---";

    UpgradeTaskVisualPanel() {
        initComponents();
        this.providerComboBox.setModel(providerModel);
        this.providerComboBox.setRenderer(new DefaultListRenderer(providerConverter));
    }

    @Override
    public String getName() {
        return super.getName(); //To change body of generated methods, choose Tools | Templates.
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        providerComboBox = new org.jdesktop.swingx.JXComboBox();
        providerLabel = new javax.swing.JLabel();
        separator = new javax.swing.JSeparator();
        maxDocLabel = new javax.swing.JLabel();
        maxEntriesLabel = new javax.swing.JLabel();
        maxDocTextField = new javax.swing.JFormattedTextField();
        maxEntriesTextField = new javax.swing.JFormattedTextField();

        providerComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                providerSelected(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(providerLabel, org.openide.util.NbBundle.getMessage(UpgradeTaskVisualPanel.class, "UpgradeTaskVisualPanel.providerLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(maxDocLabel, org.openide.util.NbBundle.getMessage(UpgradeTaskVisualPanel.class, "UpgradeTaskVisualPanel.maxDocLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(maxEntriesLabel, org.openide.util.NbBundle.getMessage(UpgradeTaskVisualPanel.class, "UpgradeTaskVisualPanel.maxEntriesLabel.text")); // NOI18N

        maxDocTextField.setColumns(8);
        maxDocTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        maxDocTextField.setText(org.openide.util.NbBundle.getMessage(UpgradeTaskVisualPanel.class, "UpgradeTaskVisualPanel.maxDocTextField.text")); // NOI18N

        maxEntriesTextField.setColumns(8);
        maxEntriesTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        maxEntriesTextField.setText(org.openide.util.NbBundle.getMessage(UpgradeTaskVisualPanel.class, "UpgradeTaskVisualPanel.maxEntriesTextField.text_1")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(separator, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(providerLabel)
                        .addGap(30, 30, 30)
                        .addComponent(providerComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(maxEntriesLabel)
                            .addComponent(maxDocLabel))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(maxDocTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(maxEntriesTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(providerLabel)
                    .addComponent(providerComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(separator, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxDocLabel)
                    .addComponent(maxDocTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxEntriesLabel)
                    .addComponent(maxEntriesTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void providerSelected(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_providerSelected
        final DbAdminServiceProvider provider = (DbAdminServiceProvider) this.providerComboBox.getSelectedItem();
    }//GEN-LAST:event_providerSelected


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel maxDocLabel;
    private javax.swing.JFormattedTextField maxDocTextField;
    private javax.swing.JLabel maxEntriesLabel;
    private javax.swing.JFormattedTextField maxEntriesTextField;
    private org.jdesktop.swingx.JXComboBox providerComboBox;
    private javax.swing.JLabel providerLabel;
    private javax.swing.JSeparator separator;
    // End of variables declaration//GEN-END:variables

    static class UpgradeTaskPanel implements WizardDescriptor.Panel<WizardDescriptor> {

        /**
         * The visual component that displays this panel. If you need to access
         * the component from this class, just use getComponent().
         */
        private UpgradeTaskVisualPanel component;

        // Get the visual component for the panel. In this template, the component
        // is kept separate. This can be more efficient: if the wizard is created
        // but never displayed, or not all panels are displayed, it is better to
        // create only those which really need to be visible.
        @Override
        public UpgradeTaskVisualPanel getComponent() {
            if (component == null) {
                component = new UpgradeTaskVisualPanel();
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
            // If it is always OK to press Next or Finish, then:
            return true;
            // If it depends on some condition (form filled out...) and
            // this condition changes (last form field filled in...) then
            // use ChangeSupport to implement add/removeChangeListener below.
            // WizardDescriptor.ERROR/WARNING/INFORMATION_MESSAGE will also be useful.
        }

        @Override
        public void addChangeListener(ChangeListener l) {
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
        }

        @Override
        public void readSettings(WizardDescriptor wiz) {
            final DBAdminTask task = (DBAdminTask) wiz.getProperty(UpgradeTask.PROP_TASK);
            getComponent().maxDocTextField.setValue(task.getArg("max-documents", Integer.class, 1000).longValue());
            getComponent().maxEntriesTextField.setValue(task.getArg("max-entries", Integer.class, 1000).longValue());
            getComponent().providerModel.removeAllElements();
            getComponent().providerModel.addElement(null);
            Lookup.getDefault().lookupAll(DbAdminServiceProvider.class).stream()
                    .forEach(getComponent().providerModel::addElement);
        }

        @Override
        public void storeSettings(WizardDescriptor wiz) {
            wiz.putProperty(UpgradeTask.PROP_PROVIDER, getComponent().providerModel.getSelectedItem());
            final DBAdminTask task = (DBAdminTask) wiz.getProperty(UpgradeTask.PROP_TASK);
            final Long md = (Long) getComponent().maxDocTextField.getValue();
            final Long me = (Long) getComponent().maxEntriesTextField.getValue();
            if (md != null && md > 0) {
                task.setArg("max-documents", md.intValue());
            }
            if (me != null && me > 0) {
                task.setArg("max-entries", me.intValue());
            }
        }
    }
}
