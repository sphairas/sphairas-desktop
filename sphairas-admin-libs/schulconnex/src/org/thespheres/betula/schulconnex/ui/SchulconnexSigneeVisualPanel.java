/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.schulconnex.ui;

import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.thespheres.betula.schulconnex.SchulconnexImportData;
import org.thespheres.betula.schulconnex.SchulconnexSigneeItem;

/**
 * Wizard step for reviewing Schulconnex signees before import.
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"SchulconnexSigneeVisualPanel.step.name=Unterzeichner auswählen"})
final class SchulconnexSigneeVisualPanel extends JPanel {

    private javax.swing.JScrollPane scrollPane;
    private org.jdesktop.swingx.JXTable table;

    final SchulconnexSigneeTableModel model = new SchulconnexSigneeTableModel();

    SchulconnexSigneeVisualPanel() {
        scrollPane = new javax.swing.JScrollPane();
        table = new org.jdesktop.swingx.JXTable();
        setLayout(new java.awt.BorderLayout());
        table.setHorizontalScrollEnabled(true);
        scrollPane.setViewportView(table);
        add(scrollPane, java.awt.BorderLayout.CENTER);
    }

    void initialize(final SchulconnexImportData<SchulconnexSigneeItem> wiz) {
        table.setColumnFactory(model.createColumnFactory(wiz));
        table.setModel(model);
        model.initialize(wiz);
        table.requestFocus();
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(SchulconnexSigneeVisualPanel.class, "SchulconnexSigneeVisualPanel.step.name");
    }

    static final class SchulconnexSigneePanel implements WizardDescriptor.Panel<SchulconnexImportData<?>> {

        private SchulconnexSigneeVisualPanel component;

        @Override
        public SchulconnexSigneeVisualPanel getComponent() {
            if (component == null) {
                component = new SchulconnexSigneeVisualPanel();
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
        public void addChangeListener(final ChangeListener listener) {
            getComponent().model.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(final ChangeListener listener) {
            getComponent().model.removeChangeListener(listener);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void readSettings(final SchulconnexImportData wiz) {
            getComponent().initialize(wiz);
        }

        @Override
        public void storeSettings(final SchulconnexImportData<?> wiz) {
            // TODO Schulconnex: consume the reviewed signee selection once the updater is implemented.
        }
    }
}
