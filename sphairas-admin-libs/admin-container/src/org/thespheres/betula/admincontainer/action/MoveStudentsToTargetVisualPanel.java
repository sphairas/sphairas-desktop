/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admincontainer.action;

import org.thespheres.betula.xmlimport.uiutil.CreateDocumentsComponent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ChangeListener;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.FontHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.ui.util.ButtonEditor;
import org.thespheres.betula.util.ChangeSet;
import org.thespheres.betula.xmlimport.uiutil.DefaultColumns;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn;
import org.thespheres.betula.xmlimport.uiutil.ImportTableModel;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages(value = {"MoveStudentsToTargetVisualPanel.step.name=Verschieben"})
class MoveStudentsToTargetVisualPanel extends JPanel implements CreateDocumentsComponent<MoveStudentsToTargetImportTargetsItem, MoveStudentsToTargetEdit> {

    private final JScrollPane scrollPanel;
    private final JXTable table;
    private final MoveStudentsToTargetTableModel model = new MoveStudentsToTargetTableModel();
    private MoveStudentsToTargetEdit wizard;

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    MoveStudentsToTargetVisualPanel() {
        super();
        scrollPanel = new JScrollPane();
        table = new JXTable();
        setLayout(new BorderLayout());
        table.setHorizontalScrollEnabled(true);
        scrollPanel.setViewportView(table);
        add(scrollPanel, BorderLayout.CENTER);
    }

    void initialize(MoveStudentsToTargetEdit wiz) {
        if (wizard == null) {
            wizard = wiz;
//            ImportTarget config = wizard.getImportTargetProperty();
//            model = config.createCreateDocumentsTableModel2();
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
    public MoveStudentsToTargetEdit getSettings() {
        return wizard;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(MoveStudentsToTargetVisualPanel.class, "MoveStudentsToTargetVisualPanel.step.name");
    }

    private static Set<ImportTableColumn> createColumns() {
        String product = NbBundle.getMessage(MoveStudentsToTargetTableModel.class, "MoveStudentsToTargetTableModel.product");
        HashSet<ImportTableColumn> ret = new HashSet<>();
        ret.add(new CancelColumn());
        ret.add(new NodeColumn());
        ret.add(new UpdateUnitTypeColumn());
        ret.add(new DefaultColumns.UnitDisplayColumn(product));
        return ret;
    }

    @NbBundle.Messages({"MoveStudentsToTargetTableModel.product=Auswahl",
        "MoveStudentsToTargetTableModel.columnName.updateUnitType=Gruppenliste leeren?",
        "MoveStudentsToTargetTableModel.columnName.numStudentsInfo=Anzahl"})
    public class MoveStudentsToTargetTableModel extends ImportTableModel<MoveStudentsToTargetImportTargetsItem, MoveStudentsToTargetEdit> implements ChangeSet.Listener {

        private MoveStudentsToTargetEdit model;

        public MoveStudentsToTargetTableModel() {
            super(createColumns());
        }

        @Override
        public void initialize(final MoveStudentsToTargetEdit descriptor) {
            this.model = descriptor;
            final ChangeSet<MoveStudentsToTargetImportTargetsItem> s = model.getSelectedNodesProperty();
            s.addChangeListener(this);
            update();
        }

        private void update() {
            selected.clear();
            model.getSelectedNodesProperty().stream()
                    .forEach(selected::add);
            fireTableDataChanged();
        }

        @Override
        public void setChanged(ChangeSet.SetChangeEvent e) {
            EventQueue.invokeLater(this::update);
        }

    }

    static class CancelColumn extends ImportTableColumn<MoveStudentsToTargetImportTargetsItem, ConfigurableImportTarget, MoveStudentsToTargetEdit, MoveStudentsToTargetTableModel> {

        private final ButtonEditor button = new ButtonEditor();
        private MoveStudentsToTargetEdit model;

        CancelColumn() {
            super("remove", 10, true, 16);
        }

        @Override
        public String getDisplayName() {
            return "";
        }

        @Override
        public void initialize(ConfigurableImportTarget configuration, MoveStudentsToTargetEdit wizard) {
            super.initialize(configuration, wizard);
            this.model = wizard;
        }

        @Override
        public Object getColumnValue(MoveStudentsToTargetImportTargetsItem il) {
            return (ActionListener) e -> model.getSelectedNodesProperty().remove(il);
        }

//        @Override
//        public void configureColumnWidth(TableColumnExt col) {
//            ButtonEditor.configureTableColumn(col);
//        }
        @Override
        public void configureTableColumn(MoveStudentsToTargetTableModel model, TableColumnExt col) {
            super.configureTableColumn(model, col);
            col.setCellRenderer(button.createRenderer());
            col.setCellEditor(button);
            ButtonEditor.configureTableColumn(col);
        }

    }

    @Messages({"MoveStudentsToTargetVisualPanel.NodeColumn.name=Aktion",
        "MoveStudentsToTargetVisualPanel.NodeColumn.message.source=Entfernen aus {0}",
        "MoveStudentsToTargetVisualPanel.NodeColumn.message.target=Hinzufügen zu {0}"})
    public static class NodeColumn extends ImportTableColumn<MoveStudentsToTargetImportTargetsItem, ConfigurableImportTarget, MoveStudentsToTargetEdit, MoveStudentsToTargetTableModel> implements StringValue {

        public NodeColumn() {
            super("node", 100, false, 400);
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(NodeColumn.class, "MoveStudentsToTargetVisualPanel.NodeColumn.name");
        }

        @Override
        public Object getColumnValue(MoveStudentsToTargetImportTargetsItem il) {
            return il;
        }

        @Override
        public void configureTableColumn(MoveStudentsToTargetTableModel model, TableColumnExt col) {
            col.setCellRenderer(new DefaultTableRenderer(this));
        }

        @Override
        public String getString(Object value) {
            final MoveStudentsToTargetImportTargetsItem it = (MoveStudentsToTargetImportTargetsItem) value;
            final String type = it.getAction().name().toLowerCase();
            return NbBundle.getMessage(NodeColumn.class, "MoveStudentsToTargetVisualPanel.NodeColumn.message." + type, it.getSourceNodeLabel());
        }

    }

    static class UpdateUnitTypeColumn extends DefaultColumns.DefaultEnumColumn<MoveStudentsToTargetImportTargetsItem, ConfigurableImportTarget, MoveStudentsToTargetEdit, MoveStudentsToTargetTableModel> implements HighlightPredicate {

        UpdateUnitTypeColumn() {
            super(UpdateUnitType.values(), "updateUnitType", 280, 200);
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(MoveStudentsToTargetTableModel.class, "MoveStudentsToTargetTableModel.columnName.updateUnitType");
        }

        @Override
        public void configureTableColumn(MoveStudentsToTargetTableModel model, TableColumnExt col) {
            super.configureTableColumn(model, col);
            Highlighter hl = new FontHighlighter(this, box.getFont().deriveFont(Font.BOLD));
            col.addHighlighter(hl);
        }

        @Override
        public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
            MoveStudentsToTargetTableModel m = (MoveStudentsToTargetTableModel) ((JTable) adapter.getComponent()).getModel();
            int r = adapter.convertRowIndexToModel(adapter.row);
            if (r < m.getRowCount()) {
                MoveStudentsToTargetImportTargetsItem i = m.getItemAt(r);
                return !i.getUpdateType().equals(i.getProposedUpdateUnitType());
            }
            return false;
        }

        @Override
        public Object getColumnValue(MoveStudentsToTargetImportTargetsItem il) {
            return il.getUpdateType();
        }

        @Override
        public boolean setColumnValue(MoveStudentsToTargetImportTargetsItem il, Object value) {
            il.setUpdateUnitType((UpdateUnitType) value);
            return true; //update num students
        }

    }

//    @Messages({"MoveStudentsToTargetVisualPanel.MessageColumn.name=Hinweise"})
//    public static class MessageColumn extends ImportTableColumn<MoveStudentsToTargetImportTargetsItem, ConfigurableImportTarget, MoveStudentsToTargetEdit, MoveStudentsToTargetTableModel> {
//
//        public MessageColumn() {
//            super("message", 1000, false, 500);
//        }
//
//        @Override
//        public String getDisplayName() {
//            return NbBundle.getMessage(MessageColumn.class, "MoveStudentsToTargetVisualPanel.MessageColumn.name");
//        }
//
//        @Override
//        public Object getColumnValue(MoveStudentsToTargetImportTargetsItem il) {
//            return il.getMessage();
//        }
//
//    }
    public static class MoveStudentsToTargetPanel implements WizardDescriptor.Panel<MoveStudentsToTargetEdit> {

        /**
         * The visual component that displays this panel. If you need to access
         * the component from this class, just use getComponent().
         */
        private MoveStudentsToTargetVisualPanel component;

        // Get the visual component for the panel. In this template, the component
        // is kept separate. This can be more efficient: if the wizard is created
        // but never displayed, or not all panels are displayed, it is better to
        // create only those which really need to be visible.
        @Override
        public MoveStudentsToTargetVisualPanel getComponent() {
            if (component == null) {
                component = new MoveStudentsToTargetVisualPanel();
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
        public void readSettings(MoveStudentsToTargetEdit wiz) {
            getComponent().initialize(wiz);
        }

        @Override
        public void storeSettings(MoveStudentsToTargetEdit wiz) {
            // use wiz.putProperty to remember current panel state
        }
    }

}
