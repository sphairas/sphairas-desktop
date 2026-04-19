/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.schulconnex.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeListener;
import org.jdesktop.swingx.JXTable;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.thespheres.betula.schulconnex.SchulconnexImportData;
import org.thespheres.betula.schulconnex.SchulconnexKursItem;

/**
 * Step 2 of the Schulconnex wizard for target item import.
 */
@NbBundle.Messages({
    "SchulconnexTargetsDocumentsVisualPanel.step.name=Kurse auswählen"
})
final class SchulconnexTargetsDocumentsVisualPanel extends JPanel {

    private final JScrollPane scrollPanel;
    private final JXTable table;
    private final JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private final SchulconnexTargetsDocumentsTableModel model = new SchulconnexTargetsDocumentsTableModel();
    private SchulconnexImportData<SchulconnexKursItem> wizard;

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    SchulconnexTargetsDocumentsVisualPanel() {
        super();
        scrollPanel = new JScrollPane();
        table = new JXTable();
        setLayout(new BorderLayout());
        table.setHorizontalScrollEnabled(true);
        scrollPanel.setViewportView(table);
        add(toolbarPanel, BorderLayout.NORTH);
        add(scrollPanel, BorderLayout.CENTER);
    }

    @SuppressWarnings("unchecked")
    void initialize(final SchulconnexImportData wiz) {
        if (wizard == null) {
            wizard = wiz;
            table.setColumnFactory(model.createColumnFactory(wiz));
            table.setModel(model);
            model.initialize(wizard);
            table.requestFocus();
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(SchulconnexTargetsDocumentsVisualPanel.class,
                "SchulconnexTargetsDocumentsVisualPanel.step.name");
    }

    void store(final SchulconnexImportData wiz) {
    }

    static final class SchulconnexTargetsDocumentsPanel implements WizardDescriptor.Panel<SchulconnexImportData<?>> {

        private SchulconnexTargetsDocumentsVisualPanel component;

        @Override
        public SchulconnexTargetsDocumentsVisualPanel getComponent() {
            if (component == null) {
                component = new SchulconnexTargetsDocumentsVisualPanel();
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
        public void addChangeListener(final ChangeListener l) {
        }

        @Override
        public void removeChangeListener(final ChangeListener l) {
        }

        @Override
        public void readSettings(final SchulconnexImportData<?> wiz) {
            getComponent().initialize(wiz);
        }

        @Override
        public void storeSettings(final SchulconnexImportData<?> wiz) {
            getComponent().store(wiz);
        }
    }
}
