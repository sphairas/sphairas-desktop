/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.database.action;

import org.thespheres.betula.xmlimport.uiutil.CreateDocumentsComponent;
import java.awt.BorderLayout;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ChangeListener;
import org.jdesktop.swingx.JXTable;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.thespheres.betula.admin.database.action.DestroyUnitsListVisualPanel.DestroyUnitsListDescriptor;
import org.thespheres.betula.util.ChangeSet;
import org.thespheres.betula.xmlimport.ImportTarget;
import org.thespheres.betula.xmlimport.uiutil.DefaultColumns;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn;
import org.thespheres.betula.xmlimport.uiutil.ImportTableModel;
import org.thespheres.betula.xmlimport.uiutil.ImportWizardSettings;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages(value = {"DestroyUnitsListTableModel.step.name=Auswahl"})
class DestroyUnitsListVisualPanel extends JPanel implements CreateDocumentsComponent<DestroyUnitsImportItem, DestroyUnitsListDescriptor> {

    private final JScrollPane scrollPanel;
    private final JXTable table;
    private final DestroyUnitsListTableModel model = new DestroyUnitsListTableModel();
    private DestroyUnitsListDescriptor wizard;

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    DestroyUnitsListVisualPanel() {
        super();
        scrollPanel = new JScrollPane();
        table = new JXTable();
        setLayout(new BorderLayout());
        table.setHorizontalScrollEnabled(true);
        scrollPanel.setViewportView(table);
        add(scrollPanel, BorderLayout.CENTER);
    }

    void initialize(DestroyUnitsListDescriptor wiz) {
        if (wizard == null) {
            wizard = wiz;
            table.setColumnFactory(model.createColumnFactory(wiz));
            table.setModel(model);
            table.requestFocus();
        }
        model.initialize(wizard);
    }

    @Override
    public JTable getTable() {
        return table;
    }

    @Override
    public DestroyUnitsListDescriptor getSettings() {
        return wizard;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(DestroyUnitsListVisualPanel.class, "DestroyUnitsListTableModel.step.name");
    }

    private static Set<ImportTableColumn> createColumns() {
        String product = NbBundle.getMessage(DestroyUnitsListTableModel.class, "DestroyUnitsListTableModel.product");
        HashSet<ImportTableColumn> ret = new HashSet<>();
        ret.add(new DefaultColumns.NodeColumn(product));
        ret.add(new UnitInfoColumn());
        ret.add(new UnitDocIdColumn());
//        ret.add(new DefaultColumns.UnitDisplayColumn(product));
        return ret;
    }

    static class DestroyUnitsListDescriptor implements ImportWizardSettings.TargetItemSettings<ImportTarget, DestroyUnitsImportItem> {

        private final ChangeSet<DestroyUnitsImportItem> items;

        DestroyUnitsListDescriptor(Set<DestroyUnitsImportItem> items) {
            super();
            this.items = new ChangeSet<>(items);
        }

        @Override
        public ImportTarget getImportTargetProperty() {
            return null;
        }

        @Override
        public ChangeSet<DestroyUnitsImportItem> getSelectedNodesProperty() {
            return items;
        }
    }

    @NbBundle.Messages({"DestroyUnitsListTableModel.product=Auswahl",
        "DestroyUnitsListTableModel.columnName.unitInfo=Information",
        "DestroyUnitsListTableModel.columnName.unitDocId=Gruppen-Dokument-ID"})
    public class DestroyUnitsListTableModel extends ImportTableModel<DestroyUnitsImportItem, DestroyUnitsListDescriptor> {

        public DestroyUnitsListTableModel() {
            super(createColumns());
        }

        @Override
        public void initialize(final DestroyUnitsListDescriptor descriptor) {
            final ChangeSet<DestroyUnitsImportItem> s = descriptor.getSelectedNodesProperty();
            selected.clear();
            s.stream()
                    .forEach(selected::add);
            fireTableDataChanged();
        }

    }

    static class UnitInfoColumn extends ImportTableColumn<DestroyUnitsImportItem, ImportTarget, DestroyUnitsListDescriptor, DestroyUnitsListTableModel> {

        UnitInfoColumn() {
            super("unitInfo", 300, false, 220);
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(DestroyUnitsListTableModel.class, "DestroyUnitsListTableModel.columnName.unitInfo");
        }

        @Override
        public Object getColumnValue(DestroyUnitsImportItem il) {
            return il.getInfo();
        }
    }

    static class UnitDocIdColumn extends ImportTableColumn<DestroyUnitsImportItem, ImportTarget, DestroyUnitsListDescriptor, DestroyUnitsListTableModel> {

        UnitDocIdColumn() {
            super("unit-doc-id", 1000, false, 175);
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(DestroyUnitsListTableModel.class, "DestroyUnitsListTableModel.columnName.unitDocId");
        }

        @Override
        public Object getColumnValue(DestroyUnitsImportItem il) {
            return il.getTargetDocumentIdBase().getId();
        }
    }

    public static class DestroyUnitsListPanel implements WizardDescriptor.Panel<DestroyUnitsListDescriptor> {

        /**
         * The visual component that displays this panel. If you need to access
         * the component from this class, just use getComponent().
         */
        private DestroyUnitsListVisualPanel component;

        // Get the visual component for the panel. In this template, the component
        // is kept separate. This can be more efficient: if the wizard is created
        // but never displayed, or not all panels are displayed, it is better to
        // create only those which really need to be visible.
        @Override
        public DestroyUnitsListVisualPanel getComponent() {
            if (component == null) {
                component = new DestroyUnitsListVisualPanel();
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
        public void readSettings(DestroyUnitsListDescriptor wiz) {
            getComponent().initialize(wiz);
        }

        @Override
        public void storeSettings(DestroyUnitsListDescriptor wiz) {
            // use wiz.putProperty to remember current panel state
        }
    }

}
