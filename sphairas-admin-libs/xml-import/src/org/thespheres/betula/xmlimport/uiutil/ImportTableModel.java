/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.uiutil;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;
import org.thespheres.betula.xmlimport.ImportItem;
import org.thespheres.betula.xmlimport.ImportTarget;

/**
 *
 * @author boris.heithecker
 * @param <I>
 * @param <W>
 */
public class ImportTableModel<I extends ImportItem, W extends ImportWizardSettings> extends AbstractTableModel implements PropertyChangeListener, VetoableChangeListener {

    public static final String PROP_COLUMN_ID = "column-id";
//    public static final String[] defaultColumnIds = {"node", "subject", "unit", "unitDisplayName", "documentBase", "sourceSignee", "signee", "deleteDate"};
    protected final List<I> selected = new ArrayList<>();
    public final String[] columns;
    private final Map<String, Optional<ImportTableColumn>> colmap;
    protected final Set<String> updatingProperties = new HashSet<>();

    protected ImportTableModel(Set<ImportTableColumn> col) {
        columns = col.stream()
                .sorted()
                .map(ImportTableColumn::columnId)
                .toArray(String[]::new);
        colmap = col.stream()
                .collect(Collectors.toMap(ImportTableColumn::columnId, itc -> Optional.ofNullable(itc)));
//        this.product = souceProd;
    }

    public static Set<ImportTableColumn> createDefault(String product) {
        return DefaultColumns.createDefaultSet(product);
    }

    @Override
    public int getRowCount() {
        return selected.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    public I getItemAt(int row) {
        return selected.get(row);
    }

    @Override
    public Object getValueAt(int rowIndex, int col) {
        I kurs = selected.get(rowIndex);
        String column = columns[col];
        return getColumnValue(column, kurs);
    }

    protected void ensureInitialized(String column, final I kurs) {
        colmap.getOrDefault(column, Optional.empty())
                .ifPresent(itc -> itc.initializeIfNot(kurs));
    }

    protected Object getColumnValue(String column, final I kurs) throws IndexOutOfBoundsException {
        ensureInitialized(column, kurs);
        return colmap.getOrDefault(column, Optional.empty())
                .map(itc -> itc.getColumnValue(kurs))
                .orElse(null);
    }

    @Override
    public void setValueAt(Object val, int row, int col) {
        I kurs = selected.get(row);
        String column = columns[col];
        setColumnValue(column, kurs, val, row);
    }

    protected void setColumnValue(String column, final I kurs, final Object val, final int row) {
        ensureInitialized(column, kurs);
        if (colmap.getOrDefault(column, Optional.empty())
                .map(itc -> itc.setColumnValue(kurs, val))
                .orElse(false)) {
            EventQueue.invokeLater(() -> fireTableRowsUpdated(row, row));
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int ci) {
        String column = columns[ci];
        final I kurs = selected.get(rowIndex);
        ensureInitialized(column, kurs);
        return colmap.getOrDefault(column, Optional.empty())
                .map(itc -> itc.isCellEditable(kurs))
                .orElse(false);
    }

    public void initialize(W descriptor) {
    }

    protected String getColumnDisplayName(String col) {
        return null;
    }

    public ColFactory createColumnFactory(W wiz) {
        final ColFactory ret = new ColFactory();
        final ImportTarget configuration = wiz.getImportTargetProperty(); //.getProperty(AbstractFileImportAction.IMPORT_TARGET, ImportTarget.class);
        ret.initialize(configuration, wiz);
        return ret;
    }

    public void registerTableUpdatingProperty(String property) {
        synchronized (updatingProperties) {
            updatingProperties.add(property);
        }
    }

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        propertyChange(evt);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        boolean c;
        synchronized (updatingProperties) {
            c = updatingProperties.contains(evt.getPropertyName());
        }
        if (c) {
            final Object o = evt.getSource();
            EventQueue.invokeLater(() -> fireRowsUpdated(o));
        }
    }

    private void fireRowsUpdated(final Object o) {
        for (int i = 0; i < selected.size(); i++) {
            if (selected.get(i).equals(o)) {
                fireTableRowsUpdated(i, i);
            }
        }
    }

    public class ColFactory<W extends ImportWizardSettings> extends ColumnFactory {

        protected boolean initialized = false;

        public ColFactory() {
        }

        public void initialize(ImportTarget configuration, W wizard) {
            if (!initialized) {
                colmap.forEach((String key, Optional<ImportTableColumn> itc) -> itc.ifPresent(i -> i.initialize(configuration, wizard)));
                initialized = true;
            }
        }

        protected ImportTableModel model(JXTable t) {
            return model(t.getModel());
        }

        protected ImportTableModel model(TableModel t) {
            return (ImportTableModel) t;
        }

        @Override
        public void configureColumnWidths(JXTable table, TableColumnExt col) {
            super.configureColumnWidths(table, col);
            int index = col.getModelIndex();
            String column = model(table).columns[index];
            colmap.getOrDefault(column, Optional.empty())
                    .ifPresent(itc -> col.setPreferredWidth(itc.getPreferredWidth()));
        }

        @Override
        public void configureTableColumn(final TableModel model, final TableColumnExt col) {
            ImportTableModel<I, ?> m;
            try {
                m = (ImportTableModel<I, ?>) model;
            } catch (ClassCastException e) {
                throw new IllegalStateException(e);
            }
            super.configureTableColumn(model, col);
            int index = col.getModelIndex();
            String column = model(model).columns[index];
            colmap.getOrDefault(column, Optional.empty())
                    .ifPresent(itc -> {
                        col.putClientProperty(PROP_COLUMN_ID, column);
                        col.setHeaderValue(itc.getDisplayName());
                        col.setToolTipText(itc.getDisplayName());
                        itc.configureTableColumn(m, col);
                    });
        }

    }

}
