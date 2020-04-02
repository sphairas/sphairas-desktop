/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tableimport.ui;

import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.thespheres.betula.tableimport.action.XmlCsvImportSettings;
import org.thespheres.betula.tableimport.impl.PrimaryUnitsXmlCsvItem;

/**
 *
 * @author boris.heithecker
 */
public class XmlUnitDataDocumentsVisualPanel extends XmlDataDocumentsVisualPanel<PrimaryUnitsXmlCsvItem> {

    protected XmlPrimaryUnitDataDocumentsTableModel model;

    @Override
    protected void initialize(XmlCsvImportSettings<PrimaryUnitsXmlCsvItem> wiz) {
        if (wizard == null) {
            wizard = wiz;
            model = new XmlPrimaryUnitDataDocumentsTableModel();
            table.setColumnFactory(model.createColumnFactory(wiz));
            table.setModel(model);
            table.requestFocus();
        }
        model.initialize(wizard);
    }

    public static class XmlDataDocumentsPanel implements WizardDescriptor.Panel<XmlCsvImportSettings<PrimaryUnitsXmlCsvItem>> {

        /**
         * The visual component that displays this panel. If you need to access
         * the component from this class, just use getComponent().
         */
        private XmlUnitDataDocumentsVisualPanel component;

        // Get the visual component for the panel. In this template, the component
        // is kept separate. This can be more efficient: if the wizard is created
        // but never displayed, or not all panels are displayed, it is better to
        // create only those which really need to be visible.
        @Override
        public XmlUnitDataDocumentsVisualPanel getComponent() {
            if (component == null) {
                component = new XmlUnitDataDocumentsVisualPanel();
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
        public void readSettings(XmlCsvImportSettings<PrimaryUnitsXmlCsvItem> wiz) {
            getComponent().initialize(wiz);
        }

        @Override
        public void storeSettings(XmlCsvImportSettings<PrimaryUnitsXmlCsvItem> wiz) {
            // use wiz.putProperty to remember current panel state
        }
    }
}
