/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tableimport.impl;

import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.thespheres.betula.tableimport.action.XmlCsvImportSettings;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"SigneeXmlCsvImportVisualPanel.step.name=Lehrer importieren"})
class SigneeXmlCsvImportVisualPanel extends JPanel {

    final SigneeXmlCsvImportTableModel model = new SigneeXmlCsvImportTableModel();

    SigneeXmlCsvImportVisualPanel() {
        initComponents();
        table.setModel(model);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(SigneeXmlCsvImportVisualPanel.class, "SigneeXmlCsvImportVisualPanel.step.name");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollPane = new javax.swing.JScrollPane();
        table = new org.jdesktop.swingx.JXTable();

        setLayout(new java.awt.BorderLayout());

        table.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_LAST_COLUMN);
        table.setColumnFactory(model.columnFactory);
        scrollPane.setViewportView(table);

        add(scrollPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane scrollPane;
    private org.jdesktop.swingx.JXTable table;
    // End of variables declaration//GEN-END:variables

    static class SigneeXmlCsvImportPanel implements WizardDescriptor.Panel<XmlCsvImportSettings<SigneeXmlCsvItem>> {

        /**
         * The visual component that displays this panel. If you need to access
         * the component from this class, just use getComponent().
         */
        private SigneeXmlCsvImportVisualPanel component;

        // Get the visual component for the panel. In this template, the component
        // is kept separate. This can be more efficient: if the wizard is created
        // but never displayed, or not all panels are displayed, it is better to
        // create only those which really need to be visible.
        @Override
        public SigneeXmlCsvImportVisualPanel getComponent() {
            if (component == null) {
                component = new SigneeXmlCsvImportVisualPanel();
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
            return getComponent().model.isValid();
            // If it depends on some condition (form filled out...) and
            // this condition changes (last form field filled in...) then
            // use ChangeSupport to implement add/removeChangeListener below.
            // WizardDescriptor.ERROR/WARNING/INFORMATION_MESSAGE will also be useful.
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            getComponent().model.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            getComponent().model.removeChangeListener(l);
        }

        @Override
        public void readSettings(XmlCsvImportSettings<SigneeXmlCsvItem> wiz) {
            //        getComponent().columnFactory.initialize(wiz);
            getComponent().model.initialize((SigneeXmlCsvSettings) wiz);
        }

        @Override
        public void storeSettings(XmlCsvImportSettings<SigneeXmlCsvItem> wiz) {
        }
    }
}
