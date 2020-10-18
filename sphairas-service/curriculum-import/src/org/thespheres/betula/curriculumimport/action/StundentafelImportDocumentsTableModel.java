/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculumimport.action;

import org.thespheres.betula.curriculumimport.StundentafelImportTargetsItem;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;
import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.util.ChangeSet;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import org.thespheres.betula.xmlimport.uiutil.ImportTableModel;
import org.thespheres.betula.xmlimport.uiutil.DefaultColumns;
import org.thespheres.betula.xmlimport.uiutil.DefaultColumns.SigneeColumn;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn;
import org.thespheres.betula.xmlimport.uiutil.UnitColumn;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"StundentafelImportDocumentsTableModel.product=Stundentafel-Import"})
class StundentafelImportDocumentsTableModel extends ImportTableModel<StundentafelImportTargetsItem, StundentafelImportSettings> {

    StundentafelImportDocumentsTableModel() {
        super(createColumns());
    }

    private static Set<ImportTableColumn> createColumns() {
        final String product = NbBundle.getMessage(StundentafelImportDocumentsTableModel.class, "StundentafelImportDocumentsTableModel.product");
        final Set<ImportTableColumn> s = ImportTableModel.createDefault(product);
        Lookups.forPath("DefaultCreateDocumentsVisualPanel/Columns").lookupAll(ImportTableColumn.Factory.class).stream()
                .map(ImportTableColumn.Factory::createInstance)
                .forEach(s::add);
        s.add(new UnitColumn(product, false));
        s.add(new StundentafelSigneeColumn(product));
        s.add(new SelectedColumn());
        s.add(new FixedSubjectColumn(product));
        s.add(new TargetIdColumn(product));
        Lookups.forPath("StundentafelImportConfigVisualPanel/Columns").lookupAll(ImportTableColumn.Factory.class).stream()
                .map(ImportTableColumn.Factory::createInstance)
                .forEach(s::add);
        return s;
    }

    @Override
    public void initialize(final StundentafelImportSettings descriptor) {
//        final ConfigurableImportTarget config = (ConfigurableImportTarget) descriptor.getProperty(AbstractFileImportAction.IMPORT_TARGET);
        final ChangeSet<StundentafelImportTargetsItem> s = (ChangeSet<StundentafelImportTargetsItem>) descriptor.getProperty(AbstractFileImportAction.SELECTED_NODES);
        selected.clear();
        s.stream()
                //                .peek(il -> ((StundentafelImportTargetsItemImpl) il).initialize(config, descriptor))
                .peek(il -> descriptor.addPropertyChangeListener(createPCL(il, descriptor)))
                .forEach(selected::add);
        fireTableDataChanged();
    }

    private PropertyChangeListener createPCL(final StundentafelImportTargetsItem il, StundentafelImportSettings descriptor) {
        return (PropertyChangeEvent evt) -> {
            if (AbstractFileImportAction.IMPORT_TARGET.equals(evt.getPropertyName())) {
                final ConfigurableImportTarget cfg = (ConfigurableImportTarget) evt.getNewValue();
                ((StundentafelImportTargetsItemImpl) il).initialize(cfg, descriptor);
            }
        };
    }

    final static class SelectedColumn extends DefaultColumns.DefaultCheckBoxColumn<StundentafelImportTargetsItem, ConfigurableImportTarget, StundentafelImportSettings, StundentafelImportDocumentsTableModel> {

        SelectedColumn() {
            super("selected", 50);
        }

        @Override
        public Boolean getColumnValue(StundentafelImportTargetsItem il) {
            return il.isSelected();
        }

        @Override
        public boolean setColumnValue(StundentafelImportTargetsItem il, Object value) {
            il.setSelected((boolean) value);
            return false;
        }

        @Override
        public String getDisplayName() {
            return "";
        }

    }

    final static class FixedSubjectColumn extends DefaultColumns<StundentafelImportTargetsItem, ConfigurableImportTarget, StundentafelImportSettings, StundentafelImportDocumentsTableModel> {

        FixedSubjectColumn(String prod) {
            super("subject", 200, false, 125, prod);
        }

        @Override
        public Object getColumnValue(StundentafelImportTargetsItem il) {
            return il.getSubjectMarker();
        }

        @Override
        public void configureTableColumn(StundentafelImportDocumentsTableModel model, TableColumnExt col) {
            super.configureTableColumn(model, col);
            col.setCellRenderer(new DefaultTableRenderer(v -> v instanceof Marker ? ((Marker) v).getLongLabel() : ""));
        }

    }

    @NbBundle.Messages({"StundentafelImportDocumentsTableModel.TargetIdColumn.displayName=Kennung"})
    final static class TargetIdColumn extends DefaultColumns<StundentafelImportTargetsItem, ConfigurableImportTarget, StundentafelImportSettings, StundentafelImportDocumentsTableModel> {

        private TargetIdColumn(String product) {
            super("targetId", 250, true, 80, product);
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(TargetIdColumn.class, "StundentafelImportDocumentsTableModel.TargetIdColumn.displayName", product);
        }

        @Override
        public Object getColumnValue(StundentafelImportTargetsItem il) {
            return il.getCustomTargetId();
        }

        @Override
        public boolean setColumnValue(StundentafelImportTargetsItem il, Object value) {
            il.setCustomTargetId((String) value);
            return true;
        }

        @Override
        public void configureTableColumn(StundentafelImportDocumentsTableModel model, TableColumnExt col) {
            JTextField tfield = new JTextField();
            tfield.setBorder(new LineBorder(Color.black, 2));
            col.setCellEditor(new DefaultCellEditor(tfield));
        }

    }

    final static class StundentafelSigneeColumn extends SigneeColumn<StundentafelImportTargetsItem, ConfigurableImportTarget, StundentafelImportSettings, StundentafelImportDocumentsTableModel> {

        private StundentafelSigneeColumn(String product) {
            super(product);
        }

        @Override
        public boolean isCellEditable(StundentafelImportTargetsItem il) {
            if (!((StundentafelImportTargetsItemImpl) il).isTaught()) {
                return false;
            }
            return super.isCellEditable(il);
        }

    }
}
