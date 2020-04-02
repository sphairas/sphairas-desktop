/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tableimport.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeListener;
import org.jdesktop.swingx.JXTable;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.thespheres.betula.tableimport.action.XmlCsvImportSettings;
import org.thespheres.betula.tableimport.impl.PrimaryUnitsXmlCsvItem;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"SiBankUpdateStudentsVisualPanel.step.name=Schülerinnen und Schüler"})
public class PrimaryUnitUpdateStudentsVisualPanel extends JPanel {

    private final PrimaryUnitUpdateStudentsTableModel model = new PrimaryUnitUpdateStudentsTableModel();
    private final JScrollPane scrollPanel;
    private final JXTable table;
    private final JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private XmlCsvImportSettings<PrimaryUnitsXmlCsvItem> wizard;

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    public PrimaryUnitUpdateStudentsVisualPanel() {
        super();
        scrollPanel = new JScrollPane();
        table = new JXTable();
        setLayout(new BorderLayout());
        table.setHorizontalScrollEnabled(true);
        scrollPanel.setViewportView(table);
//        Arrays.stream(toolbars().getToolbars()).forEach(toolbarPanel::add);
        add(toolbarPanel, BorderLayout.NORTH);
        add(scrollPanel, BorderLayout.CENTER);
    }

    void initialize(XmlCsvImportSettings<PrimaryUnitsXmlCsvItem> wiz) {
        if (wizard == null) {
            wizard = wiz;
            table.setColumnFactory(model.createColumnFactory(wizard));
            table.setModel(model);
            table.requestFocus();
        }
        model.initialize(wizard);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(PrimaryUnitUpdateStudentsVisualPanel.class, "SiBankUpdateStudentsVisualPanel.step.name");
    }

    public static class PrimaryUnitUpdateStudentsPanel implements WizardDescriptor.Panel<XmlCsvImportSettings<PrimaryUnitsXmlCsvItem>> {

        /**
         * The visual component that displays this panel. If you need to access
         * the component from this class, just use getComponent().
         */
        private PrimaryUnitUpdateStudentsVisualPanel component;

        // Get the visual component for the panel. In this template, the component
        // is kept separate. This can be more efficient: if the wizard is created
        // but never displayed, or not all panels are displayed, it is better to
        // create only those which really need to be visible.
        @Override
        public PrimaryUnitUpdateStudentsVisualPanel getComponent() {
            if (component == null) {
                component = new PrimaryUnitUpdateStudentsVisualPanel();
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
