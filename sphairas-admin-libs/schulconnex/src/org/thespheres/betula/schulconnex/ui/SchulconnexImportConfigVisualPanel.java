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
import org.thespheres.betula.xmlimport.ImportTargetFactory;
import org.thespheres.betula.xmlimport.uiutil.AbstractImportAction;
import org.thespheres.betula.xmlimport.uiutil.ImportProviderComboBoxModel;

/**
 * First wizard panel: lets the user select the sphairas server/provider that
 * acts as the import target.
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({
    "SchulconnexImportConfigVisualPanel.step.name=Mandant",
    "SchulconnexImportConfigVisualPanel.providerLabel.text=Server:"
})
class SchulconnexImportConfigVisualPanel extends JPanel {

    private final ImportProviderComboBoxModel<SchulconnexImportConfiguration> providerModel
            = new ImportProviderComboBoxModel<>();

    @SuppressWarnings("LeakingThisInConstructor")
    public SchulconnexImportConfigVisualPanel() {
        Lookup.getDefault().lookupAll(ImportTargetFactory.class).stream()
                .filter(f -> f.getProduct().equals(Schulconnex.getProduct()))
                .flatMap(f -> (Stream<ImportTargetFactory<SchulconnexImportConfiguration>.ProviderRef>)
                        f.available(SchulconnexImportConfiguration.class).stream())
                .forEach(providerModel::addElement);
        initComponents();
        providerComboBox.setRenderer(new DefaultListRenderer(providerModel));
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(SchulconnexImportConfigVisualPanel.class, "SchulconnexImportConfigVisualPanel.step.name");
    }

    private void initComponents() {
        providerLabel = new javax.swing.JLabel();
        providerComboBox = new org.jdesktop.swingx.JXComboBox();

        org.openide.awt.Mnemonics.setLocalizedText(providerLabel,
            NbBundle.getMessage(SchulconnexImportConfigVisualPanel.class, "SchulconnexImportConfigVisualPanel.providerLabel.text"));

        providerComboBox.setModel(providerModel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(providerLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(providerComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 266,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap()));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(providerLabel)
                                .addComponent(providerComboBox, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
    }

    // Variables declaration
    private org.jdesktop.swingx.JXComboBox providerComboBox;
    private javax.swing.JLabel providerLabel;
    // End of variables declaration

    void store(SchulconnexImportData d) {
        final SchulconnexImportConfiguration p = providerModel.findTarget();
        d.putProperty(AbstractImportAction.IMPORT_TARGET, p);
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
    }

    static class SchulconnexImportConfigPanel implements WizardDescriptor.Panel<SchulconnexImportData> {

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
