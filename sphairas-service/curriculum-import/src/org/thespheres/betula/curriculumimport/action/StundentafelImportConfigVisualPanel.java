/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculumimport.action;

import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.xmlimport.ImportTargetFactory;
import org.thespheres.betula.xmlimport.model.Product;
import org.thespheres.betula.xmlimport.uiutil.AbstractImportWizardSettings;
import org.thespheres.betula.xmlimport.uiutil.ImportProviderComboBoxModel;
import org.thespheres.betula.xmlimport.uiutil.TermModel;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;

@NbBundle.Messages({"XmlDataImportConfigVisualPanel.step.name=Datenquellen"})
public class StundentafelImportConfigVisualPanel extends JPanel {
    
    private final ImportProviderComboBoxModel<ConfigurableImportTarget> providerModel = new ImportProviderComboBoxModel<>();
    private final TermModel<ConfigurableImportTarget, AbstractImportWizardSettings<ConfigurableImportTarget>> termsModel;
    
    @SuppressWarnings("LeakingThisInConstructor")
    public StundentafelImportConfigVisualPanel() {
        Lookup.getDefault().lookupAll(ImportTargetFactory.class).stream()
                .filter(ConfigurableImportTarget.Factory.class::isInstance)
                .map(ConfigurableImportTarget.Factory.class::cast)
                .filter(sbit -> sbit.getProduct() == null || sbit.getProduct().equals(Product.NO))
                .flatMap(sbit -> sbit.available(ConfigurableImportTarget.class).stream())
                .forEach(providerModel::addElement);
        termsModel = new TermModel<>(providerModel);
        initComponents();
        this.providerComboBox.setRenderer(new DefaultListRenderer(providerModel));
        this.termsComboBox.setRenderer(new DefaultListRenderer(termsModel));
    }
    
    @Override
    public String getName() {
        return NbBundle.getMessage(StundentafelImportConfigVisualPanel.class, "XmlDataImportConfigVisualPanel.step.name");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        providerLabel = new javax.swing.JLabel();
        providerComboBox = new org.jdesktop.swingx.JXComboBox();
        termLabel = new javax.swing.JLabel();
        termsComboBox = new org.jdesktop.swingx.JXComboBox();
        dryRunBox = new javax.swing.JCheckBox();
        selectUnitsButton = new javax.swing.JButton();
        unitsLabel = new javax.swing.JLabel();
        unitsTextField = new org.jdesktop.swingx.JXTextField();

        setPreferredSize(new java.awt.Dimension(2, 362));

        org.openide.awt.Mnemonics.setLocalizedText(providerLabel, org.openide.util.NbBundle.getMessage(StundentafelImportConfigVisualPanel.class, "StundentafelImportConfigVisualPanel.providerLabel.text")); // NOI18N
        providerLabel.setEnabled(false);

        providerComboBox.setModel(providerModel);
        providerComboBox.setEnabled(false);
        providerComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                providerSelected(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(termLabel, org.openide.util.NbBundle.getMessage(StundentafelImportConfigVisualPanel.class, "StundentafelImportConfigVisualPanel.termLabel.text")); // NOI18N

        termsComboBox.setModel(termsModel);

        org.openide.awt.Mnemonics.setLocalizedText(dryRunBox, org.openide.util.NbBundle.getMessage(StundentafelImportConfigVisualPanel.class, "StundentafelImportConfigVisualPanel.dryRunBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(selectUnitsButton, org.openide.util.NbBundle.getMessage(StundentafelImportConfigVisualPanel.class, "StundentafelImportConfigVisualPanel.selectUnitsButton.text")); // NOI18N
        selectUnitsButton.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(unitsLabel, org.openide.util.NbBundle.getMessage(StundentafelImportConfigVisualPanel.class, "StundentafelImportConfigVisualPanel.unitsLabel.text")); // NOI18N
        unitsLabel.setEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(dryRunBox)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(termLabel)
                            .addComponent(providerLabel)
                            .addComponent(unitsLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(unitsTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(selectUnitsButton))
                            .addComponent(providerComboBox, javax.swing.GroupLayout.DEFAULT_SIZE, 547, Short.MAX_VALUE)
                            .addComponent(termsComboBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(providerLabel)
                    .addComponent(providerComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(termLabel)
                    .addComponent(termsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(selectUnitsButton)
                    .addComponent(unitsLabel)
                    .addComponent(unitsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(dryRunBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void providerSelected(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_providerSelected
        //We have to update import term immediately, not in store() we wiz.property is set.
//        final SiBankImportTarget.Factory f = (SiBankImportTarget.Factory) providerComboBox.getSelectedItem();
        ConfigurableImportTarget p = providerModel.findTarget();
        termsModel.initializeModel(p, null);
    }//GEN-LAST:event_providerSelected

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox dryRunBox;
    private org.jdesktop.swingx.JXComboBox providerComboBox;
    private javax.swing.JLabel providerLabel;
    private javax.swing.JButton selectUnitsButton;
    private javax.swing.JLabel termLabel;
    private org.jdesktop.swingx.JXComboBox termsComboBox;
    private javax.swing.JLabel unitsLabel;
    private org.jdesktop.swingx.JXTextField unitsTextField;
    // End of variables declaration//GEN-END:variables

    void store(AbstractImportWizardSettings<ConfigurableImportTarget> d) {
        final ConfigurableImportTarget p = providerModel.findTarget();
        d.putProperty(AbstractFileImportAction.IMPORT_TARGET, p);
        final Term t = (Term) termsComboBox.getSelectedItem();
        d.putProperty(AbstractFileImportAction.TERM, t);
        d.removePropertyChangeListener(termsModel);
        d.putProperty(AbstractFileImportAction.PROP_DRY_RUN, dryRunBox.isSelected());
    }
    
    void read(AbstractImportWizardSettings<ConfigurableImportTarget> settings) {
        final ConfigurableImportTarget p = (ConfigurableImportTarget) settings.getProperty(AbstractFileImportAction.IMPORT_TARGET);
        if (p != null) {
            providerModel.setSelectedTarget(p);
        }
        termsModel.init(settings);
//        final List<UnitId> units = settings.getProperty(StundentafelImportAction.SELECTED_UNITS, List.class);
        //
        final Boolean b = settings.getProperty(StundentafelImportAction.ALLOW_SELECT_UNITS, Boolean.class);
        final boolean ue = b != null && b;
        this.unitsLabel.setEnabled(ue);
        this.unitsTextField.setEnabled(ue);
        this.selectUnitsButton.setEnabled(ue);
        settings.addPropertyChangeListener(termsModel);
        final Boolean dr = (Boolean) settings.getProperty(AbstractFileImportAction.PROP_DRY_RUN);
        if (dr != null) {
            dryRunBox.setSelected(dr);
        }
    }
    
    public static class XmlDataImportConfigPanel<D extends AbstractImportWizardSettings<ConfigurableImportTarget>> implements WizardDescriptor.Panel<D> {

        /**
         * The visual component that displays this panel. If you need to access
         * the component from this class, just use getComponent().
         */
        private StundentafelImportConfigVisualPanel component;

        // Get the visual component for the panel. In this template, the component
        // is kept separate. This can be more efficient: if the wizard is created
        // but never displayed, or not all panels are displayed, it is better to
        // create only those which really need to be visible.
        @Override
        public StundentafelImportConfigVisualPanel getComponent() {
            if (component == null) {
                component = new StundentafelImportConfigVisualPanel();
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
        public void readSettings(D wiz) {
            getComponent().read(wiz);
            // use wiz.getProperty to retrieve previous panel state
        }
        
        @Override
        public void storeSettings(D wiz) {
            getComponent().store(wiz);
        }
        
    }
}
