/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tableimport.ui;

import java.io.IOException;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.thespheres.betula.tableimport.action.XmlCsvImportSettings;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"ConfigureDictionaryVisualPanel.step.name=Spalten"})
public class ConfigureDictionaryVisualPanel extends javax.swing.JPanel {
    
    private final ConfigureDictionaryTableModel model;
    
    public ConfigureDictionaryVisualPanel() {
        this.model = new ConfigureDictionaryTableModel();
        initComponents();
        table.setColumnFactory(model.createColumnFactory());
        table.setModel(model);
    }
    
    @Override
    public String getName() {
        return NbBundle.getMessage(XmlDataImportConfigVisualPanel.class, "ConfigureDictionaryVisualPanel.step.name");
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollPane = new javax.swing.JScrollPane();
        table = new org.jdesktop.swingx.JXTable();
        helpPanel = new org.jdesktop.swingx.JXPanel();
        groupCheckBox = new javax.swing.JCheckBox();

        setLayout(new java.awt.BorderLayout());

        table.setHorizontalScrollEnabled(true);
        scrollPane.setViewportView(table);

        add(scrollPane, java.awt.BorderLayout.CENTER);

        org.openide.awt.Mnemonics.setLocalizedText(groupCheckBox, org.openide.util.NbBundle.getMessage(ConfigureDictionaryVisualPanel.class, "ConfigureDictionaryVisualPanel.groupCheckBox.text")); // NOI18N
        groupCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                groupCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout helpPanelLayout = new javax.swing.GroupLayout(helpPanel);
        helpPanel.setLayout(helpPanelLayout);
        helpPanelLayout.setHorizontalGroup(
            helpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(helpPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(groupCheckBox)
                .addContainerGap(294, Short.MAX_VALUE))
        );
        helpPanelLayout.setVerticalGroup(
            helpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, helpPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(groupCheckBox)
                .addContainerGap())
        );

        add(helpPanel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void groupCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_groupCheckBoxActionPerformed
        model.updateGrouping(groupCheckBox.isSelected());
    }//GEN-LAST:event_groupCheckBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox groupCheckBox;
    private org.jdesktop.swingx.JXPanel helpPanel;
    private javax.swing.JScrollPane scrollPane;
    private org.jdesktop.swingx.JXTable table;
    // End of variables declaration//GEN-END:variables

    void store(final XmlCsvImportSettings<?> d) throws IOException {
        d.setGrouping(model.useGrouping());
        if (model.changed()) {
            d.reload(null);
            model.reset();
        }
    }
    
    void read(XmlCsvImportSettings<?> settings) throws IOException {
        model.initialize(settings.getXmlCsv(), settings.createDictionary());
        groupCheckBox.setSelected(settings.useGrouping());
        groupCheckBox.setEnabled(settings.allowSelectUseGrouping());
    }
    
    public static class ConfigureDictionaryPanel<D extends XmlCsvImportSettings<?>> implements WizardDescriptor.Panel<D> {

        /**
         * The visual component that displays this panel. If you need to access
         * the component from this class, just use getComponent().
         */
        private ConfigureDictionaryVisualPanel component;

        // Get the visual component for the panel. In this template, the component
        // is kept separate. This can be more efficient: if the wizard is created
        // but never displayed, or not all panels are displayed, it is better to
        // create only those which really need to be visible.
        @Override
        public ConfigureDictionaryVisualPanel getComponent() {
            if (component == null) {
                component = new ConfigureDictionaryVisualPanel();
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
            try {
                getComponent().read(wiz);
                wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
                // use wiz.getProperty to retrieve previous panel state
            } catch (IOException ex) {
                wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, ex.getLocalizedMessage());
            }
        }
        
        @Override
        public void storeSettings(D wiz) {
            try {
                getComponent().store(wiz);
                wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
            } catch (IOException ex) {
                wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, ex.getLocalizedMessage());
            }
        }
        
    }
    
}
