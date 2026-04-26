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
import org.thespheres.betula.schulconnex.SchulconnexKlasseItem;

/**
 * Step 3 for Schueler imports: review/select person records for update.
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({
    "SchulconnexPrimaryUnitUpdateStudentsVisualPanel.step.name=Schüler/-innen auswählen"
})
final class SchulconnexPrimaryUnitStudentsVisualPanel extends JPanel {

    static final String PROP_SELECTED_PERSONEN_FOR_UPDATE = "schulconnex-selected-personen-update";

    private final JScrollPane scrollPanel;
    private final JXTable table;
    private final JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private final SchulconnexPrimaryUnitStudentsTableModel model = new SchulconnexPrimaryUnitStudentsTableModel();

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    SchulconnexPrimaryUnitStudentsVisualPanel() {
        super();
        scrollPanel = new JScrollPane();
        table = new JXTable();
        setLayout(new BorderLayout());
        table.setHorizontalScrollEnabled(true);
        scrollPanel.setViewportView(table);
        add(toolbarPanel, BorderLayout.NORTH);
        add(scrollPanel, BorderLayout.CENTER);
    }

    void initialize(final SchulconnexImportData<SchulconnexKlasseItem> wiz) {
        table.setColumnFactory(model.createColumnFactory(wiz));
        table.setModel(model);
        model.initialize(wiz);
        table.requestFocus();
    }

    void store(final SchulconnexImportData<SchulconnexKlasseItem> wiz) {
        wiz.putProperty(PROP_SELECTED_PERSONEN_FOR_UPDATE, model.getSelectedPersons());
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(SchulconnexPrimaryUnitStudentsVisualPanel.class,
                "SchulconnexPrimaryUnitUpdateStudentsVisualPanel.step.name");
    }

    static final class SchulconnexPrimaryUnitUpdateStudentsPanel implements WizardDescriptor.Panel<SchulconnexImportData<?>> {

        private SchulconnexPrimaryUnitStudentsVisualPanel component;

        @Override
        public SchulconnexPrimaryUnitStudentsVisualPanel getComponent() {
            if (component == null) {
                component = new SchulconnexPrimaryUnitStudentsVisualPanel();
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
        @SuppressWarnings("unchecked")
        public void readSettings(final SchulconnexImportData wiz) {
            getComponent().initialize(wiz);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void storeSettings(final SchulconnexImportData wiz) {
            getComponent().store(wiz);
        }
    }
}
