/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculumimport.action;

import org.thespheres.betula.curriculumimport.StundentafelImportTargetsItem;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.event.ChangeListener;
import org.jdesktop.swingx.JXTable;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.thespheres.betula.xmlimport.uiutil.CreateDocumentsComponent;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages(value = {"StundentafelImportDocumentsVisualPanel.step.name=Dokumente anlegen"})
class StundentafelImportDocumentsVisualPanel extends JPanel implements CreateDocumentsComponent<StundentafelImportTargetsItem, StundentafelImportSettings> {

    private final JScrollPane scrollPanel;
    private final JXTable table;
    private StundentafelImportDocumentsTableModel model;
    private final JToolBar toolbar;
    private StundentafelImportSettings wizard;

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    private StundentafelImportDocumentsVisualPanel() {
        super();
        scrollPanel = new JScrollPane();
        table = new JXTable();
        toolbar = new JToolBar();
        toolbar.setLayout(new FlowLayout(FlowLayout.LEFT));
        toolbar.setFloatable(false);
        setLayout(new BorderLayout());
        table.setHorizontalScrollEnabled(true);
        scrollPanel.setViewportView(table);
        add(toolbar, BorderLayout.NORTH);
        add(scrollPanel, BorderLayout.CENTER);
    }

    void initialize(StundentafelImportSettings wiz) {
        if (wizard == null) {
            wizard = wiz;
            model = new StundentafelImportDocumentsTableModel();
            table.setColumnFactory(model.createColumnFactory(wiz));
            table.setModel(model);
            table.requestFocus();
        }
        model.initialize(wizard);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(StundentafelImportDocumentsVisualPanel.class, "StundentafelImportDocumentsVisualPanel.step.name");
    }

    @Override
    public JTable getTable() {
        return table;
    }

    @Override
    public StundentafelImportSettings getSettings() {
        return wizard;
    }

    public static class DocumentsPanel implements WizardDescriptor.Panel<StundentafelImportSettings> {

        /**
         * The visual component that displays this panel. If you need to access
         * the component from this class, just use getComponent().
         */
        private StundentafelImportDocumentsVisualPanel component;

        // Get the visual component for the panel. In this template, the component
        // is kept separate. This can be more efficient: if the wizard is created
        // but never displayed, or not all panels are displayed, it is better to
        // create only those which really need to be visible.
        @Override
        public StundentafelImportDocumentsVisualPanel getComponent() {
            if (component == null) {
                component = new StundentafelImportDocumentsVisualPanel();
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
        public void readSettings(StundentafelImportSettings wiz) {
            getComponent().initialize(wiz);
        }

        @Override
        public void storeSettings(StundentafelImportSettings wiz) {
            // use wiz.putProperty to remember current panel state
        }
    }

}
