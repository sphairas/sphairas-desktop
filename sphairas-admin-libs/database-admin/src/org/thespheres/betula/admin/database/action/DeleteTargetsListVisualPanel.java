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
import org.thespheres.betula.admin.database.action.DeleteTargetsListVisualPanel.DeleteTargetsListDescriptor;
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
@NbBundle.Messages(value = {"DeleteTargetsListTableModel.step.name=Auswahl"})
class DeleteTargetsListVisualPanel extends JPanel implements CreateDocumentsComponent<DeleteTargetsImportTargetsItem, DeleteTargetsListDescriptor> {

    private final JScrollPane scrollPanel;
    private final JXTable table;
    private final DeleteTargetsListTableModel model = new DeleteTargetsListTableModel();
    private DeleteTargetsListDescriptor wizard;

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    DeleteTargetsListVisualPanel() {
        super();
        scrollPanel = new JScrollPane();
        table = new JXTable();
        setLayout(new BorderLayout());
        table.setHorizontalScrollEnabled(true);
        scrollPanel.setViewportView(table);
        add(scrollPanel, BorderLayout.CENTER);
    }

    void initialize(DeleteTargetsListDescriptor wiz) {
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
    public DeleteTargetsListDescriptor getSettings() {
        return wizard;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(DeleteTargetsListVisualPanel.class, "DeleteTargetsListTableModel.step.name");
    }

    private static Set<ImportTableColumn> createColumns() {
        String product = NbBundle.getMessage(DeleteTargetsListTableModel.class, "DeleteTargetsListTableModel.product");
        HashSet<ImportTableColumn> ret = new HashSet<>();
        ret.add(new DefaultColumns.NodeColumn(product));
        ret.add(new TargetInfoColumn());
        ret.add(new DefaultColumns.UnitDisplayColumn(product));
        return ret;
    }

    static class DeleteTargetsListDescriptor implements ImportWizardSettings.TargetItemSettings<ImportTarget, DeleteTargetsImportTargetsItem> {

        private final ChangeSet<DeleteTargetsImportTargetsItem> items;

        DeleteTargetsListDescriptor(Set<DeleteTargetsImportTargetsItem> items) {
            super();
            this.items = new ChangeSet<>(items);
        }

        @Override
        public ImportTarget getImportTargetProperty() {
            return null;
        }

        @Override
        public ChangeSet<DeleteTargetsImportTargetsItem> getSelectedNodesProperty() {
            return items;
        }
    }

    @NbBundle.Messages({"DeleteTargetsListTableModel.product=Auswahl",
        "DeleteTargetsListTableModel.columnName.targetInfo=Information"})
    public class DeleteTargetsListTableModel extends ImportTableModel<DeleteTargetsImportTargetsItem, DeleteTargetsListDescriptor> {

        public DeleteTargetsListTableModel() {
            super(createColumns());
        }

        @Override
        public void initialize(final DeleteTargetsListDescriptor descriptor) {
            final ChangeSet<DeleteTargetsImportTargetsItem> s = descriptor.getSelectedNodesProperty();
            selected.clear();
            s.stream()
                    .forEach(selected::add);
            fireTableDataChanged();
        }

    }

    static class TargetInfoColumn extends ImportTableColumn<DeleteTargetsImportTargetsItem, ImportTarget, DeleteTargetsListDescriptor, DeleteTargetsListTableModel> {

        TargetInfoColumn() {
            super("targetInfo", 300, false, 400);
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(DeleteTargetsListTableModel.class, "DeleteTargetsListTableModel.columnName.targetInfo");
        }

        @Override
        public Object getColumnValue(DeleteTargetsImportTargetsItem il) {
            return il.getInfo();
        }
    }

    public static class DeleteTargetsListPanel implements WizardDescriptor.Panel<DeleteTargetsListDescriptor> {

        /**
         * The visual component that displays this panel. If you need to access
         * the component from this class, just use getComponent().
         */
        private DeleteTargetsListVisualPanel component;

        // Get the visual component for the panel. In this template, the component
        // is kept separate. This can be more efficient: if the wizard is created
        // but never displayed, or not all panels are displayed, it is better to
        // create only those which really need to be visible.
        @Override
        public DeleteTargetsListVisualPanel getComponent() {
            if (component == null) {
                component = new DeleteTargetsListVisualPanel();
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
        public void readSettings(DeleteTargetsListDescriptor wiz) {
            getComponent().initialize(wiz);
        }

        @Override
        public void storeSettings(DeleteTargetsListDescriptor wiz) {
            // use wiz.putProperty to remember current panel state
        }
    }

}
