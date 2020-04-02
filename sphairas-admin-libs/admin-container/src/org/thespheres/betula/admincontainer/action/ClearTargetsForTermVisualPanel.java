/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admincontainer.action;

import org.thespheres.betula.xmlimport.uiutil.CreateDocumentsComponent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
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
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.thespheres.betula.admincontainer.action.ClearTargetsForTermImportTargetsItem.ResetType;
import org.thespheres.betula.util.ChangeSet;
import org.thespheres.betula.xmlimport.ImportTarget;
import org.thespheres.betula.xmlimport.uiutil.DefaultColumns;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn;
import org.thespheres.betula.xmlimport.uiutil.ImportTableModel;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages(value = {"ClearTargetsForTermVisualPanel.step.name=Auswahl"})
class ClearTargetsForTermVisualPanel extends JPanel implements CreateDocumentsComponent<ClearTargetsForTermImportTargetsItem, ClearTargetsForTermEdit> {

    private final JScrollPane scrollPanel;
    private final JXTable table;
    private final ClearTargetsForTermTableModel model = new ClearTargetsForTermTableModel();
    private ClearTargetsForTermEdit wizard;

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    ClearTargetsForTermVisualPanel() {
        super();
        scrollPanel = new JScrollPane();
        table = new JXTable();
        setLayout(new BorderLayout());
        table.setHorizontalScrollEnabled(true);
        scrollPanel.setViewportView(table);
        add(scrollPanel, BorderLayout.CENTER);
    }

    void initialize(ClearTargetsForTermEdit wiz) {
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
    public ClearTargetsForTermEdit getSettings() {
        return wizard;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ClearTargetsForTermVisualPanel.class, "ClearTargetsForTermVisualPanel.step.name");
    }

    private static Set<ImportTableColumn> createColumns() {
        String product = NbBundle.getMessage(ClearTargetsForTermTableModel.class, "ClearTargetsForTermTableModel.product");
        HashSet<ImportTableColumn> ret = new HashSet<>();
        ret.add(new DefaultColumns.NodeColumn(product));
        ret.add(new ClearUnitTypeColumn());
        ret.add(new DefaultColumns.UnitDisplayColumn(product));
        ret.add(new ResetTypeColumn());
        return ret;
    }

    @NbBundle.Messages({"ClearTargetsForTermTableModel.product=Auswahl",
        "ClearTargetsForTermTableModel.columnName.clearUnitType=Gruppenliste",
        "ClearTargetsForTermTableModel.columnName.resetType=Aktion"})
    public class ClearTargetsForTermTableModel extends ImportTableModel<ClearTargetsForTermImportTargetsItem, ClearTargetsForTermEdit> {

        public ClearTargetsForTermTableModel() {
            super(createColumns());
        }

        @Override
        public void initialize(final ClearTargetsForTermEdit descriptor) {
            ChangeSet<ClearTargetsForTermImportTargetsItem> s = descriptor.getSelectedNodesProperty();
            selected.clear();
            s.stream()
                    .forEach(selected::add);
            fireTableDataChanged();
        }

    }

    static class ClearUnitTypeColumn extends DefaultColumns.DefaultEnumColumn<ClearTargetsForTermImportTargetsItem, ImportTarget, ClearTargetsForTermEdit, ClearTargetsForTermTableModel> implements HighlightPredicate {

        ClearUnitTypeColumn() {
            super(UpdateUnitType.values(), "clearUnitType", 280, 200);
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(ClearTargetsForTermTableModel.class, "ClearTargetsForTermTableModel.columnName.clearUnitType");
        }

        @Override
        public void configureTableColumn(ClearTargetsForTermTableModel model, TableColumnExt col) {
            super.configureTableColumn(model, col);
            Highlighter hl = new FontHighlighter(this, box.getFont().deriveFont(Font.BOLD));
            col.addHighlighter(hl);
        }

        @Override
        public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
            ClearTargetsForTermTableModel m = (ClearTargetsForTermTableModel) ((JTable) adapter.getComponent()).getModel();
            int r = adapter.convertRowIndexToModel(adapter.row);
            if (r < m.getRowCount()) {
                ClearTargetsForTermImportTargetsItem i = m.getItemAt(r);
                return !i.getClearType().equals(i.getProposedClearType());
            }
            return false;
        }

        @Override
        public Object getColumnValue(ClearTargetsForTermImportTargetsItem il) {
            return il.getClearType();
        }

        @Override
        public boolean setColumnValue(ClearTargetsForTermImportTargetsItem il, Object value) {
            il.setClearType((UpdateUnitType) value);
            return false;
        }

    }

    static class ResetTypeColumn extends DefaultColumns.DefaultEnumColumn<ClearTargetsForTermImportTargetsItem, ImportTarget, ClearTargetsForTermEdit, ClearTargetsForTermTableModel> {

        ResetTypeColumn() {
            super(ResetType.values(), "resetType", 120, 200);
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(ClearTargetsForTermTableModel.class, "ClearTargetsForTermTableModel.columnName.resetType");
        }

        @Override
        public Object getColumnValue(ClearTargetsForTermImportTargetsItem il) {
            return il.getResetType();
        }

        @Override
        public boolean setColumnValue(ClearTargetsForTermImportTargetsItem il, Object value) {
            il.setResetType((ResetType) value);
            return false;
        }

    }

    public static class ClearTargetsForTermPanel implements WizardDescriptor.Panel<ClearTargetsForTermEdit> {

        /**
         * The visual component that displays this panel. If you need to access
         * the component from this class, just use getComponent().
         */
        private ClearTargetsForTermVisualPanel component;

        // Get the visual component for the panel. In this template, the component
        // is kept separate. This can be more efficient: if the wizard is created
        // but never displayed, or not all panels are displayed, it is better to
        // create only those which really need to be visible.
        @Override
        public ClearTargetsForTermVisualPanel getComponent() {
            if (component == null) {
                component = new ClearTargetsForTermVisualPanel();
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
        public void readSettings(ClearTargetsForTermEdit wiz) {
            getComponent().initialize(wiz);
        }

        @Override
        public void storeSettings(ClearTargetsForTermEdit wiz) {
            // use wiz.putProperty to remember current panel state
        }
    }

}
