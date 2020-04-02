/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.database.action;

import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Stream;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.thespheres.betula.TermId;
import org.thespheres.betula.admin.database.DbAdminServiceProvider;
import org.thespheres.betula.database.DBAdminTask;
import org.thespheres.betula.document.model.DocumentsModel;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.scheme.spi.SchemeProvider;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.scheme.spi.TermNotFoundException;
import org.thespheres.betula.services.scheme.spi.TermSchedule;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
class DeleteTaskVisualPanel extends javax.swing.JPanel {

    private final DefaultComboBoxModel<DbAdminServiceProvider> providerModel = new DefaultComboBoxModel<>();
    private final StringValue providerConverter = o -> o instanceof DbAdminServiceProvider ? ((DbAdminServiceProvider) o).getInfo().getDisplayName() : "---";
    private final DefaultComboBoxModel<Term> termModel = new DefaultComboBoxModel<>();
    private final StringValue termConverter = o -> o instanceof Term ? ((Term) o).getDisplayName() : "---";
    private final DefaultComboBoxModel<String> targetModel = new DefaultComboBoxModel<>();
    private final StringValue targetConverter = o -> o instanceof String ? (String) o : "---";
    private TermSchedule currentTermSchedule = null;

    DeleteTaskVisualPanel() {
        initComponents();
        this.providerComboBox.setModel(providerModel);
        this.providerComboBox.setRenderer(new DefaultListRenderer(providerConverter));
        this.termComboBox.setModel(termModel);
        this.termComboBox.setRenderer(new DefaultListRenderer(termConverter));
        this.targetTypeComboBox.setModel(targetModel);
        this.targetTypeComboBox.setRenderer(new DefaultListRenderer(targetConverter));
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
        termLabel = new javax.swing.JLabel();
        termComboBox = new org.jdesktop.swingx.JXComboBox();
        targetTypeLabel = new javax.swing.JLabel();
        targetTypeComboBox = new org.jdesktop.swingx.JXComboBox();
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

        org.openide.awt.Mnemonics.setLocalizedText(providerLabel, org.openide.util.NbBundle.getMessage(DeleteTaskVisualPanel.class, "DeleteTaskVisualPanel.providerLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(termLabel, org.openide.util.NbBundle.getMessage(DeleteTaskVisualPanel.class, "DeleteTaskVisualPanel.termLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(targetTypeLabel, org.openide.util.NbBundle.getMessage(DeleteTaskVisualPanel.class, "DeleteTaskVisualPanel.targetTypeLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(maxDocLabel, org.openide.util.NbBundle.getMessage(DeleteTaskVisualPanel.class, "DeleteTaskVisualPanel.maxDocLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(maxEntriesLabel, org.openide.util.NbBundle.getMessage(DeleteTaskVisualPanel.class, "DeleteTaskVisualPanel.maxEntriesLabel.text")); // NOI18N

        maxDocTextField.setColumns(8);
        maxDocTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        maxDocTextField.setText(org.openide.util.NbBundle.getMessage(DeleteTaskVisualPanel.class, "DeleteTaskVisualPanel.maxDocTextField.text")); // NOI18N

        maxEntriesTextField.setColumns(8);
        maxEntriesTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        maxEntriesTextField.setText(org.openide.util.NbBundle.getMessage(DeleteTaskVisualPanel.class, "DeleteTaskVisualPanel.maxEntriesTextField.text_1")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(separator, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(termLabel)
                            .addComponent(providerLabel)
                            .addComponent(targetTypeLabel))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(providerComboBox, javax.swing.GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE)
                            .addComponent(termComboBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(targetTypeComboBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(termLabel)
                    .addComponent(termComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(targetTypeLabel)
                    .addComponent(targetTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
        LocalProperties props = null;
        if (provider != null) {
            props = LocalProperties.find(provider.getInfo().getURL());
            currentTermSchedule = Optional.of(props)
                    .map(lp -> lp.getProperty("termSchedule.providerURL"))
                    .map(tsprop -> Lookup.getDefault().lookupAll(SchemeProvider.class).stream()
                    .filter(p -> p.getInfo().getURL().equals(tsprop))
                    .collect(CollectionUtil.singleOrNull()))
                    .map(p -> p.getScheme(TermSchedule.DEFAULT_SCHEME, TermSchedule.class))
                    .orElse(null);
        } else {
            currentTermSchedule = null;
        }
        this.termModel.removeAllElements();
        this.termModel.addElement(null);
        if (currentTermSchedule != null) {
            final Term ct = currentTermSchedule.getCurrentTerm();
            for (int i = -10; i < 3; i++) {
                if (i == 0) {
                    this.termModel.addElement(ct);
                } else {
                    final TermId otid = new TermId(ct.getScheduledItemId().getAuthority(), ct.getScheduledItemId().getId() + i);
                    try {
                        final Term oterm = currentTermSchedule.resolve(otid);
                        this.termModel.addElement(oterm);
                    } catch (TermNotFoundException | IllegalAuthorityException ex) {
                        PlatformUtil.getCodeNameBaseLogger(DeleteTaskVisualPanel.class).log(Level.WARNING, ex.getLocalizedMessage(), ex);
                    }
                }
            }
            this.termComboBox.setSelectedItem(ct);
        }
        this.targetModel.removeAllElements();
        this.targetModel.addElement(null);
        if (provider != null && props != null) {
            Optional.of(props.getProperty(DocumentsModel.PROP_DOCUMENT_SUFFIXES))
                    .map(p -> Arrays.stream(p.split(",")))
                    .orElse(Stream.empty())
                    .map(StringUtils::capitalize)
                    .forEach(this.targetModel::addElement);
        }
    }//GEN-LAST:event_providerSelected


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel maxDocLabel;
    private javax.swing.JFormattedTextField maxDocTextField;
    private javax.swing.JLabel maxEntriesLabel;
    private javax.swing.JFormattedTextField maxEntriesTextField;
    private org.jdesktop.swingx.JXComboBox providerComboBox;
    private javax.swing.JLabel providerLabel;
    private javax.swing.JSeparator separator;
    private org.jdesktop.swingx.JXComboBox targetTypeComboBox;
    private javax.swing.JLabel targetTypeLabel;
    private org.jdesktop.swingx.JXComboBox termComboBox;
    private javax.swing.JLabel termLabel;
    // End of variables declaration//GEN-END:variables

    static class DeleteTaskPanel implements WizardDescriptor.Panel<WizardDescriptor> {

        /**
         * The visual component that displays this panel. If you need to access
         * the component from this class, just use getComponent().
         */
        private DeleteTaskVisualPanel component;

        // Get the visual component for the panel. In this template, the component
        // is kept separate. This can be more efficient: if the wizard is created
        // but never displayed, or not all panels are displayed, it is better to
        // create only those which really need to be visible.
        @Override
        public DeleteTaskVisualPanel getComponent() {
            if (component == null) {
                component = new DeleteTaskVisualPanel();
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
            final DBAdminTask task = (DBAdminTask) wiz.getProperty(DeleteTask.PROP_TASK);
            getComponent().maxDocTextField.setValue(task.getArg("max-documents", Integer.class, 1000).longValue());
            getComponent().maxEntriesTextField.setValue(task.getArg("max-entries", Integer.class, 1000).longValue());
            getComponent().providerModel.removeAllElements();
            getComponent().providerModel.addElement(null);
            Lookup.getDefault().lookupAll(DbAdminServiceProvider.class).stream()
                    .forEach(getComponent().providerModel::addElement);
        }

        @Override
        public void storeSettings(WizardDescriptor wiz) {
            wiz.putProperty(DeleteTask.PROP_PROVIDER, getComponent().providerModel.getSelectedItem());
            final DBAdminTask task = (DBAdminTask) wiz.getProperty(DeleteTask.PROP_TASK);
            final Term term = (Term) getComponent().termModel.getSelectedItem();
            final String targetType = (String) getComponent().targetModel.getSelectedItem();
            final Long md = (Long) getComponent().maxDocTextField.getValue();
            final Long me = (Long) getComponent().maxEntriesTextField.getValue();
            if (term != null) {
                task.setArg("term", term.getScheduledItemId());
            }
            if (md != null && md > 0) {
                task.setArg("max-documents", md.intValue());
            }
            if (me != null && me > 0) {
                task.setArg("max-entries", me.intValue());
            }
            task.setArg("target-type", targetType);
        }
    }
}
