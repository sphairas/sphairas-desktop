/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.sibank.ui2.impl;

import org.thespheres.betula.xmlimport.uiutil.TermModel;
import java.io.IOException;
import java.util.stream.Stream;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.sibank.SiBankImportData;
import org.thespheres.betula.sibank.SiBankImportTarget;
import org.thespheres.betula.sibank.SiBankPlus;
import org.thespheres.betula.xmlimport.ImportTargetFactory;
import org.thespheres.betula.xmlimport.uiutil.ImportProviderComboBoxModel;
import org.thespheres.betula.xmlimport.utilities.VCardStudentsUtil;

@NbBundle.Messages({"SibankImportConfigVisualPanel2.step.name=Datenquellen"})
class SibankImportConfigVisualPanel2 extends JPanel {

    private final ImportProviderComboBoxModel<SiBankImportTarget> providerModel = new ImportProviderComboBoxModel<>();
//    private final StringValue providerStringValue = v -> v != null ? ((SiBankImportTarget.Factory) v).getProviderInfo().getDisplayName() : "";
    private final SiBankTermModel termsModel;
    private final ChangeSupport cSupport = new ChangeSupport(this);
    private Exception exception;

    @SuppressWarnings("LeakingThisInConstructor")
    public SibankImportConfigVisualPanel2() {
//        Lookup.getDefault().lookupAll(SiBankImportTarget.class).stream()
//                .filter(sbit -> sbit.getProduct().equals(SiBankPlus.getProduct()))
//                .forEach(providerModel::addElement);
        Lookup.getDefault().lookupAll(ImportTargetFactory.class).stream()
                .filter(sbit -> sbit.getProduct().equals(SiBankPlus.getProduct()))
                .flatMap(sbit -> (Stream<ImportTargetFactory<SiBankImportTarget>.ProviderRef>) sbit.available(SiBankImportTarget.class).stream())
                .forEach(providerModel::addElement);
        this.termsModel = new SiBankTermModel(providerModel, this);
        initComponents();
        this.providerComboBox.setRenderer(new DefaultListRenderer(providerModel));
        this.termsComboBox.setRenderer(new DefaultListRenderer(termsModel));
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(SibankImportConfigVisualPanel2.class, "SibankImportConfigVisualPanel2.step.name");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        projectProviderLabel = new javax.swing.JLabel();
        providerComboBox = new org.jdesktop.swingx.JXComboBox();
        termLabel = new javax.swing.JLabel();
        termsComboBox = new org.jdesktop.swingx.JXComboBox();
        dryRunBox = new javax.swing.JCheckBox();

        org.openide.awt.Mnemonics.setLocalizedText(projectProviderLabel, org.openide.util.NbBundle.getMessage(SibankImportConfigVisualPanel2.class, "SibankImportConfigVisualPanel2.providerLabel.text")); // NOI18N

        providerComboBox.setModel(providerModel);
        providerComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                providerComboBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(termLabel, org.openide.util.NbBundle.getMessage(SibankImportConfigVisualPanel2.class, "SibankImportConfigVisualPanel2.termLabel.text")); // NOI18N

        termsComboBox.setModel(termsModel);

        org.openide.awt.Mnemonics.setLocalizedText(dryRunBox, org.openide.util.NbBundle.getMessage(SibankImportConfigVisualPanel2.class, "SibankImportConfigVisualPanel2.dryRunBox.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(termLabel)
                            .addComponent(projectProviderLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(termsComboBox, javax.swing.GroupLayout.DEFAULT_SIZE, 406, Short.MAX_VALUE)
                            .addComponent(providerComboBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(dryRunBox)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectProviderLabel)
                    .addComponent(providerComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(termLabel)
                    .addComponent(termsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(dryRunBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void providerComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_providerComboBoxActionPerformed
        //We have to update import term immediately, not in store() we wiz.property is set.
//        final SiBankImportTarget.Factory f = (SiBankImportTarget.Factory) providerComboBox.getSelectedItem();
        SiBankImportTarget p = providerModel.findTarget();
        termsModel.initializeModel(p, null);
    }//GEN-LAST:event_providerComboBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox dryRunBox;
    private javax.swing.JLabel projectProviderLabel;
    private org.jdesktop.swingx.JXComboBox providerComboBox;
    private javax.swing.JLabel termLabel;
    private org.jdesktop.swingx.JXComboBox termsComboBox;
    // End of variables declaration//GEN-END:variables

    void store(SiBankImportData d) {
        final SiBankImportTarget p = providerModel.findTarget();
        d.putProperty(AbstractFileImportAction.IMPORT_TARGET, p);
        final Term t = (Term) termsComboBox.getSelectedItem();
        d.putProperty(AbstractFileImportAction.TERM, t);
        d.removePropertyChangeListener(termsModel);
        d.putProperty(AbstractFileImportAction.PROP_DRY_RUN, dryRunBox.isSelected());
    }

    void read(SiBankImportData settings) {
        final SiBankImportTarget p = (SiBankImportTarget) settings.getProperty(AbstractFileImportAction.IMPORT_TARGET);
        final String purl = (String) settings.getProperty(AbstractFileImportAction.SAVED_IMPORT_TARGET_PROVIDER);
        if (p != null) {
            providerModel.setSelectedTarget(p);
        } else if (purl != null) {
            providerModel.setSelectedTarget(SiBankPlus.getProduct(), purl);
        }
        //
        termsModel.init(settings);
        settings.addPropertyChangeListener(termsModel);
        //
        final Boolean dr = (Boolean) settings.getProperty(AbstractFileImportAction.PROP_DRY_RUN);
        if (dr != null) {
            dryRunBox.setSelected(dr);
        }
    }

    private void initBetulaStudentsLoad(SiBankImportTarget p) {
        try {
            VCardStudentsUtil.findFromConfiguration(p);
            exception = null;
        } catch (IOException ex) {
            exception = ex;
        }
        cSupport.fireChange();
    }

    public static class SiBankTermModel extends TermModel<SiBankImportTarget, SiBankImportData> {

        private final SibankImportConfigVisualPanel2 panel;

        public SiBankTermModel(ImportProviderComboBoxModel<SiBankImportTarget> providerModel, SibankImportConfigVisualPanel2 outer) {
            super(providerModel);
            this.panel = outer;
        }

        @Override
        public void initializeModel(SiBankImportTarget config, Term set) {
            super.initializeModel(config, set);
            panel.initBetulaStudentsLoad(currentConfig);
        }

    }

    static class SibankImportConfigPanel2 implements WizardDescriptor.Panel<SiBankImportData> {

        private SibankImportConfigVisualPanel2 component;
        private SiBankImportData wizard;

        @Override
        public SibankImportConfigVisualPanel2 getComponent() {
            if (component == null) {
                component = new SibankImportConfigVisualPanel2();
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
            boolean ret = getComponent().exception == null;
            String msg = null;
            if (!ret) {
                msg = getComponent().exception.getLocalizedMessage();
            }
            if (wizard != null) {
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, msg);
            }
            return ret;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            getComponent().cSupport.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            getComponent().cSupport.removeChangeListener(l);
        }

        @Override
        public void readSettings(SiBankImportData wiz) {
            getComponent().read(wiz);
            this.wizard = wiz;
        }

        @Override
        public void storeSettings(SiBankImportData wiz) {
            getComponent().store(wiz);
            this.wizard = null;
        }

    }
}
