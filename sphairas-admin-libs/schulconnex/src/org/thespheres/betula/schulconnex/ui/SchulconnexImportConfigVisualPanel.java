/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.schulconnex.ui;

import java.util.stream.Stream;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.openide.WizardDescriptor;
import org.openide.util.*;
import org.thespheres.betula.schulconnex.Schulconnex;
import org.thespheres.betula.schulconnex.SchulconnexImportConfiguration;
import org.thespheres.betula.schulconnex.SchulconnexImportData;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.xmlimport.ImportTargetFactory;
import org.thespheres.betula.xmlimport.uiutil.AbstractImportAction;
import org.thespheres.betula.xmlimport.uiutil.ImportProviderComboBoxModel;
import org.thespheres.betula.xmlimport.uiutil.TermModel;

/**
 * First wizard panel: lets the user select the sphairas server/provider that
 * acts as the import target.
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({
    "SchulconnexImportConfigVisualPanel.step.name=Mandant",
    "SchulconnexImportConfigVisualPanel.providerLabel.text=Server:",
    "SchulconnexImportConfigVisualPanel.termLabel.text=Halbjahr:",
    "SchulconnexImportConfigVisualPanel.dryRunBox.text=Probelauf"
})
class SchulconnexImportConfigVisualPanel extends JPanel {

    private final ImportProviderComboBoxModel<SchulconnexImportConfiguration> providerModel
            = new ImportProviderComboBoxModel<>();
    private final TermModel<SchulconnexImportConfiguration, SchulconnexImportData<?>> termsModel;

    @SuppressWarnings("LeakingThisInConstructor")
    public SchulconnexImportConfigVisualPanel() {
        Lookup.getDefault().lookupAll(ImportTargetFactory.class).stream()
                .filter(f -> f.getProduct().equals(Schulconnex.getProduct()))
                .flatMap(f -> (Stream<ImportTargetFactory<SchulconnexImportConfiguration>.ProviderRef>)
                        f.available(SchulconnexImportConfiguration.class).stream())
                .forEach(providerModel::addElement);
        termsModel = new TermModel<>(providerModel);
        initComponents();
        providerComboBox.setRenderer(new DefaultListRenderer(providerModel));
        termsComboBox.setRenderer(new DefaultListRenderer(termsModel));
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(SchulconnexImportConfigVisualPanel.class, "SchulconnexImportConfigVisualPanel.step.name");
    }

    private void initComponents() {
        providerLabel = new javax.swing.JLabel();
        providerComboBox = new org.jdesktop.swingx.JXComboBox();
        termLabel = new javax.swing.JLabel();
        termsComboBox = new org.jdesktop.swingx.JXComboBox();
        dryRunBox = new javax.swing.JCheckBox();

        org.openide.awt.Mnemonics.setLocalizedText(providerLabel,
            NbBundle.getMessage(SchulconnexImportConfigVisualPanel.class, "SchulconnexImportConfigVisualPanel.providerLabel.text"));

        providerComboBox.setModel(providerModel);
        providerComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                providerSelected(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(termLabel,
            NbBundle.getMessage(SchulconnexImportConfigVisualPanel.class, "SchulconnexImportConfigVisualPanel.termLabel.text"));

        termsComboBox.setModel(termsModel);

        org.openide.awt.Mnemonics.setLocalizedText(dryRunBox,
            NbBundle.getMessage(SchulconnexImportConfigVisualPanel.class, "SchulconnexImportConfigVisualPanel.dryRunBox.text"));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(providerLabel)
                            .addComponent(termLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(providerComboBox, javax.swing.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE)
                            .addComponent(termsComboBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(dryRunBox)
                        .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap()));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(providerLabel)
                                .addComponent(providerComboBox, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(termLabel)
                            .addComponent(termsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(dryRunBox)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
    }

                private void providerSelected(java.awt.event.ActionEvent evt) {
                final SchulconnexImportConfiguration p = providerModel.findTarget();
                termsModel.initializeModel(p, null);
                }

    // Variables declaration
    private javax.swing.JCheckBox dryRunBox;
    private org.jdesktop.swingx.JXComboBox providerComboBox;
    private javax.swing.JLabel providerLabel;
    private javax.swing.JLabel termLabel;
    private org.jdesktop.swingx.JXComboBox termsComboBox;
    // End of variables declaration

    void store(SchulconnexImportData d) {
        final SchulconnexImportConfiguration p = providerModel.findTarget();
        d.putProperty(AbstractImportAction.IMPORT_TARGET, p);
        final Term t = (Term) termsComboBox.getSelectedItem();
        d.putProperty(AbstractImportAction.TERM, t);
        d.removePropertyChangeListener(termsModel);
        d.putProperty(AbstractImportAction.PROP_DRY_RUN, dryRunBox.isSelected());
    }

    void read(SchulconnexImportData settings) {
        final SchulconnexImportConfiguration p =
                (SchulconnexImportConfiguration) settings.getProperty(AbstractImportAction.IMPORT_TARGET);
        final String purl = (String) settings.getProperty(AbstractImportAction.SAVED_IMPORT_TARGET_PROVIDER);
        if (p != null) {
            providerModel.setSelectedTarget(p);
        } else if (purl != null) {
            providerModel.setSelectedTarget(Schulconnex.getProduct(), purl);
        }
        termsModel.init(settings);
        settings.addPropertyChangeListener(termsModel);
        final Boolean dr = (Boolean) settings.getProperty(AbstractImportAction.PROP_DRY_RUN);
        if (dr != null) {
            dryRunBox.setSelected(dr);
        }
    }

    static class SchulconnexImportConfigPanel implements WizardDescriptor.Panel<SchulconnexImportData<?>> {

        private SchulconnexImportConfigVisualPanel component;

        @Override
        public SchulconnexImportConfigVisualPanel getComponent() {
            if (component == null) {
                component = new SchulconnexImportConfigVisualPanel();
            }
            return component;
        }

        @Override
        public HelpCtx getHelp() {
            return HelpCtx.DEFAULT_HELP;
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
        public void readSettings(SchulconnexImportData wiz) {
            getComponent().read(wiz);
        }

        @Override
        public void storeSettings(SchulconnexImportData wiz) {
            getComponent().store(wiz);
        }
    }
}
