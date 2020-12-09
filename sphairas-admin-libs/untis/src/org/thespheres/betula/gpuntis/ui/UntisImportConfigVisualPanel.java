package org.thespheres.betula.gpuntis.ui;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.thespheres.betula.gpuntis.ImportUntisUtil;
import org.thespheres.betula.gpuntis.Untis;
import org.thespheres.betula.gpuntis.UntisImportConfiguration;
import org.thespheres.betula.gpuntis.UntisImportData;
import org.thespheres.betula.gpuntis.xml.Document;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.xmlimport.ImportTargetFactory;
import org.thespheres.betula.xmlimport.uiutil.ImportProviderComboBoxModel;
import org.thespheres.betula.xmlimport.uiutil.TermModel;

@NbBundle.Messages({"UntisImportConfigVisualPanel.step.name=Datenquellen",
    "UntisImportConfigVisualPanel.untis.period={0} bis {1}"})
class UntisImportConfigVisualPanel extends JPanel {

    private final ImportProviderComboBoxModel<UntisImportConfiguration> providerModel = new ImportProviderComboBoxModel<>();
//    private final StringValue providerStringValue = v -> v != null ? ((UntisImportConfiguration) v).getProviderInfo().getDisplayName() : "";
    private final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("EE, d.M.yy HH:mm");
    private final DateTimeFormatter DF = DateTimeFormatter.ofPattern("d.M.yy");
    private final UntisTermModel termsModel;
    private UntisImportData settings;

    @SuppressWarnings("LeakingThisInConstructor")
    public UntisImportConfigVisualPanel() {
        Lookup.getDefault().lookupAll(ImportTargetFactory.class).stream()
                .filter(sbit -> sbit.getProduct().equals(Untis.getProduct()))
                .flatMap(sbit -> (Stream<ImportTargetFactory<UntisImportConfiguration>.ProviderRef>) sbit.available(UntisImportConfiguration.class).stream())
                .forEach(providerModel::addElement);
        this.termsModel = new UntisTermModel(providerModel);
        initComponents();
        this.providerComboBox.setRenderer(new DefaultListRenderer(providerModel));
        this.termsComboBox.setRenderer(new DefaultListRenderer(termsModel));
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(UntisImportConfigVisualPanel.class, "UntisImportConfigVisualPanel.step.name");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFormattedTextField1 = new javax.swing.JFormattedTextField();
        providerLabel = new javax.swing.JLabel();
        providerComboBox = new org.jdesktop.swingx.JXComboBox();
        updateCalendarDataCheckBox = new javax.swing.JCheckBox();
        schoolNumberLabel = new javax.swing.JLabel();
        schoolNumberTextField = new javax.swing.JTextField();
        schoolYearIdLabel = new javax.swing.JLabel();
        untisTermLabel = new javax.swing.JLabel();
        untisTermTextField = new org.jdesktop.swingx.JXTextField();
        schoolYearIdTextField = new javax.swing.JFormattedTextField();
        untisTermNameTextField = new org.jdesktop.swingx.JXTextField();
        untisTermNameLabel = new javax.swing.JLabel();
        termLabel = new javax.swing.JLabel();
        termsComboBox = new org.jdesktop.swingx.JXComboBox();
        versionLabel = new javax.swing.JLabel();
        versionTextField = new javax.swing.JTextField();

        jFormattedTextField1.setText(org.openide.util.NbBundle.getMessage(UntisImportConfigVisualPanel.class, "UntisImportConfigVisualPanel.jFormattedTextField1.text")); // NOI18N

        setPreferredSize(new java.awt.Dimension(2, 362));

        org.openide.awt.Mnemonics.setLocalizedText(providerLabel, org.openide.util.NbBundle.getMessage(UntisImportConfigVisualPanel.class, "UntisImportConfigVisualPanel.providerLabel.text")); // NOI18N

        providerComboBox.setModel(providerModel);
        providerComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                providerSelected(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(updateCalendarDataCheckBox, org.openide.util.NbBundle.getMessage(UntisImportConfigVisualPanel.class, "UntisImportConfigVisualPanel.updateCalendarDataCheckBox.text")); // NOI18N
        updateCalendarDataCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateCalendarDataCheckBoxActionPerformed(evt);
            }
        });

        schoolNumberLabel.setLabelFor(schoolNumberTextField);
        org.openide.awt.Mnemonics.setLocalizedText(schoolNumberLabel, org.openide.util.NbBundle.getMessage(UntisImportConfigVisualPanel.class, "UntisImportConfigVisualPanel.schoolNumberLabel.text")); // NOI18N

        schoolNumberTextField.setEditable(false);
        schoolNumberTextField.setColumns(10);
        schoolNumberTextField.setEnabled(false);

        schoolYearIdLabel.setLabelFor(schoolYearIdTextField);
        org.openide.awt.Mnemonics.setLocalizedText(schoolYearIdLabel, org.openide.util.NbBundle.getMessage(UntisImportConfigVisualPanel.class, "UntisImportConfigVisualPanel.schoolYearIdLabel.text")); // NOI18N

        untisTermLabel.setLabelFor(untisTermTextField);
        org.openide.awt.Mnemonics.setLocalizedText(untisTermLabel, org.openide.util.NbBundle.getMessage(UntisImportConfigVisualPanel.class, "UntisImportConfigVisualPanel.untisTermLabel.text")); // NOI18N

        untisTermTextField.setEditable(false);
        untisTermTextField.setColumns(20);
        untisTermTextField.setEnabled(false);

        schoolYearIdTextField.setEditable(false);
        schoolYearIdTextField.setColumns(10);
        schoolYearIdTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        schoolYearIdTextField.setEnabled(false);

        untisTermNameTextField.setEditable(false);
        untisTermNameTextField.setColumns(20);
        untisTermNameTextField.setEnabled(false);

        untisTermNameLabel.setLabelFor(untisTermNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(untisTermNameLabel, org.openide.util.NbBundle.getMessage(UntisImportConfigVisualPanel.class, "UntisImportConfigVisualPanel.untisTermNameLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(termLabel, org.openide.util.NbBundle.getMessage(UntisImportConfigVisualPanel.class, "UntisImportConfigVisualPanel.termLabel.text")); // NOI18N

        termsComboBox.setModel(termsModel);

        org.openide.awt.Mnemonics.setLocalizedText(versionLabel, org.openide.util.NbBundle.getMessage(UntisImportConfigVisualPanel.class, "UntisImportConfigVisualPanel.versionLabel.text")); // NOI18N

        versionTextField.setEditable(false);
        versionTextField.setEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(schoolNumberLabel)
                            .addComponent(schoolYearIdLabel)
                            .addComponent(untisTermLabel)
                            .addComponent(untisTermNameLabel)
                            .addComponent(versionLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(schoolNumberTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(schoolYearIdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(versionTextField, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(untisTermNameTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE)
                                .addComponent(untisTermTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(providerLabel)
                            .addComponent(termLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(termsComboBox, javax.swing.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
                            .addComponent(providerComboBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(updateCalendarDataCheckBox))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addGap(18, 18, 18)
                .addComponent(updateCalendarDataCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(schoolNumberLabel)
                    .addComponent(schoolNumberTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(schoolYearIdLabel)
                    .addComponent(schoolYearIdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(untisTermLabel)
                    .addComponent(untisTermTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(untisTermNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(untisTermNameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(versionLabel)
                    .addComponent(versionTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void providerSelected(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_providerSelected
        //We have to update import term immediately, not in store() we wiz.property is set.
//        final SiBankImportTarget.Factory f = (SiBankImportTarget.Factory) providerComboBox.getSelectedItem();
        UntisImportConfiguration p = providerModel.findTarget();
        termsModel.initializeModel(p, null);
    }//GEN-LAST:event_providerSelected

    private void updateCalendarDataCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateCalendarDataCheckBoxActionPerformed
        final boolean uploadDocument = updateCalendarDataCheckBox.isSelected();
        if (settings != null) {
            settings.setUploadUntisDocument(uploadDocument);
            updateEnabled();
        }
    }//GEN-LAST:event_updateCalendarDataCheckBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFormattedTextField jFormattedTextField1;
    private org.jdesktop.swingx.JXComboBox providerComboBox;
    private javax.swing.JLabel providerLabel;
    private javax.swing.JLabel schoolNumberLabel;
    private javax.swing.JTextField schoolNumberTextField;
    private javax.swing.JLabel schoolYearIdLabel;
    private javax.swing.JFormattedTextField schoolYearIdTextField;
    private javax.swing.JLabel termLabel;
    private org.jdesktop.swingx.JXComboBox termsComboBox;
    private javax.swing.JLabel untisTermLabel;
    private javax.swing.JLabel untisTermNameLabel;
    private org.jdesktop.swingx.JXTextField untisTermNameTextField;
    private org.jdesktop.swingx.JXTextField untisTermTextField;
    private javax.swing.JCheckBox updateCalendarDataCheckBox;
    private javax.swing.JLabel versionLabel;
    private javax.swing.JTextField versionTextField;
    // End of variables declaration//GEN-END:variables

    void store(UntisImportData d) {
//        UntisImportConfiguration p = (UntisImportConfiguration) providerComboBox.getSelectedItem();
        final UntisImportConfiguration p = providerModel.findTarget();
        d.putProperty(AbstractFileImportAction.IMPORT_TARGET, p);
        final Term t = (Term) termsComboBox.getSelectedItem();
        d.putProperty(AbstractFileImportAction.TERM, t);
        d.setUploadUntisDocument(updateCalendarDataCheckBox.isSelected());
        d.removePropertyChangeListener(termsModel);
    }

    void read(final UntisImportData settings) {
        final UntisImportConfiguration p = (UntisImportConfiguration) settings.getProperty(AbstractFileImportAction.IMPORT_TARGET);
        final String purl = (String) settings.getProperty(AbstractFileImportAction.SAVED_IMPORT_TARGET_PROVIDER);
        final Document doc = (Document) settings.getProperty(AbstractFileImportAction.DATA);
        if (p != null) {
            providerModel.setSelectedTarget(p);
        } else if (purl != null) {
            providerModel.setSelectedTarget(Untis.getProduct(), purl);
        }
        final boolean uploadDocument = settings.isUploadUntisDocument();
        if (doc != null) {
            schoolNumberTextField.setText(Integer.toString(doc.getGeneral().getSchoolNumber()));
            schoolYearIdTextField.setValue(ImportUntisUtil.computeSchoolYearId(doc.getGeneral()));
            final LocalDate tb = doc.getGeneral().getTermBeginDate();
            final LocalDate te = doc.getGeneral().getTermEndDate();
            final String period = NbBundle.getMessage(UntisImportConfigVisualPanel.class, "UntisImportConfigVisualPanel.untis.period", tb.format(DF), te.format(DF));
            untisTermTextField.setText(period);
            untisTermNameTextField.setText(doc.getGeneral().getTermName());
            final LocalDateTime ldt = LocalDateTime.of(doc.getDate(), doc.getTime());
            versionTextField.setText(ldt.format(DTF));
        }
        updateCalendarDataCheckBox.setSelected(uploadDocument);
        termsModel.init(settings);
        final UntisImportConfiguration cf = termsModel.getCurrentConfig();
        if (cf != null && doc != null && settings.getProperty(AbstractFileImportAction.TERM) == null) {
            final Term term = ImportAction.findImportTerm(doc, cf);
            if (term != null) {
                termsModel.setSelectedItem(term);
            }
        }
        updateEnabled();
        settings.addPropertyChangeListener(termsModel);
        this.settings = settings;
    }

    private void updateEnabled() {
        final boolean uploadDocument = updateCalendarDataCheckBox.isSelected();
        schoolNumberTextField.setEnabled(uploadDocument);
        untisTermTextField.setEnabled(uploadDocument);
        untisTermNameTextField.setEnabled(uploadDocument);
        schoolYearIdTextField.setEnabled(uploadDocument);
        versionTextField.setEnabled(uploadDocument);
    }

    class UntisTermModel extends TermModel<UntisImportConfiguration, UntisImportData> {

        UntisTermModel(ImportProviderComboBoxModel<UntisImportConfiguration> providerModel) {
            super(providerModel);
        }

        UntisImportConfiguration getCurrentConfig() {
            return currentConfig;
        }

    }

    static class UntisImportConfigPanel implements WizardDescriptor.Panel<UntisImportData> {

        /**
         * The visual component that displays this panel. If you need to access
         * the component from this class, just use getComponent().
         */
        private UntisImportConfigVisualPanel component;

        // Get the visual component for the panel. In this template, the component
        // is kept separate. This can be more efficient: if the wizard is created
        // but never displayed, or not all panels are displayed, it is better to
        // create only those which really need to be visible.
        @Override
        public UntisImportConfigVisualPanel getComponent() {
            if (component == null) {
                component = new UntisImportConfigVisualPanel();
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
        public void readSettings(UntisImportData wiz) {
            getComponent().read(wiz);
            // use wiz.getProperty to retrieve previous panel state
        }

        @Override
        public void storeSettings(UntisImportData wiz) {
            getComponent().store(wiz);
        }

    }
}
