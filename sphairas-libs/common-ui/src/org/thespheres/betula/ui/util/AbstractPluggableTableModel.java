/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.thespheres.betula.ui.util.AbstractPluggableTableModel.PluggableColumnFactory;
import org.thespheres.betula.ui.util.PluggableTableColumn.IndexedColumn;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 * @param <Model>
 * @param <Item>
 * @param <Col>
 * @param <CF>
 */
public abstract class AbstractPluggableTableModel<Model, Item, Col extends PluggableTableColumn<Model, Item>, CF extends PluggableColumnFactory> extends AbstractTableModel {

    public static final String PROP_COLUMN_ID = "column-id";
    public static final String PROP_COLUMNS_INDEX = "columns-index";
    public static final Object PROP_COLUMNS_START = "columns-start";

    private final String id;
    protected final List<Col> columns;
    protected Lookup context;
    protected Model model;
    protected boolean initialized = false;

    protected AbstractPluggableTableModel(String id, Set<? extends Col> s) {
        this.id = id;
        columns = s.stream()
                .sorted(Comparator.comparingInt(Col::getPosition))
                .collect(Collectors.toList());
    }

    public String id() {
        return id;
    }

    public synchronized void initialize(Model model, Lookup context) {
        this.model = model;
        this.context = context;
        columns.stream()
                .forEach(jtc -> jtc.initialize(model, context));
        initialized = true;
        fireTableStructureChanged();
    }

    public Lookup getContext() {
        return context != null ? context : Lookup.EMPTY;
    }

    public Model getItemsModel() {
        return model;
    }

    protected abstract CF createColumnFactory();

    @Override
    public int getRowCount() {
        if (initialized) {
            return getItemSize();
        }
        return 0;
    }

    public List<Item> getRows() {
        if (initialized) {
            IntStream.range(0, getItemSize())
                    .mapToObj(this::getItemAt)
                    .collect(Collectors.toList());
        }
        return Collections.EMPTY_LIST;
    }

    protected abstract int getItemSize();

    @Override
    public int getColumnCount() {
        if (initialized) {
            return columns.stream()
                    .collect(Collectors.summingInt(this::getColumnsSize));
        }
        return 0;
    }

    public Col getColumn(String id) {
        return columns.stream()
                .filter(c -> c.columnId().equals(id))
                .collect(CollectionUtil.singleOrNull());
    }

    private int getColumnsSize(Col c) {
        return c instanceof IndexedColumn ? ((IndexedColumn<Item, Model>) c).getColumnsSize() : 1;
    }

    protected ColumnIndex getColumnsAt(int tableColumn) {
        int current = 0;
        int before = 0;
        for (Col c : columns) {
            int start = current;
            current += c instanceof IndexedColumn ? ((IndexedColumn<Item, Model>) c).getColumnsSize() : 1;
            if (current > tableColumn) {
                return new ColumnIndex(c, tableColumn - before, start);
            }
            before = current;
        }
        return null; //SwingX problem?
//        throw new IndexOutOfBoundsException("Column index is " + tableColumn);
    }

    public int getColumnIndex(int column) {
        final ColumnIndex ci = getColumnsAt(column);
        return ci.indexWithinColGroup;
    }

    @Override
    public Object getValueAt(int row, int column) {
//        assert EventQueue.isDispatchThread();//Called from TableBuilder outside awt
        final Item r = getItemAt(row);
        final ColumnIndex ci = getColumnsAt(column);
        if (ci == null || r == null) {
            return null; //see getColumnsAt(int tableColumn) , SwingX Problem?
        }
        return ci.column instanceof IndexedColumn ? ((IndexedColumn<Model, Item>) ci.column).getColumnValue(r, ci.indexWithinColGroup) : ci.column.getColumnValue(r);
    }

    @Override
    public void setValueAt(Object val, int row, int col) {
        Item r = getItemAt(row);
        ColumnIndex ci = getColumnsAt(col);
        if (ci.column instanceof IndexedColumn ? ((IndexedColumn<Model, Item>) ci.column).setColumnValue(r, ci.indexWithinColGroup, val) : ci.column.setColumnValue(r, val)) {
            fireTableRowsUpdated(row, row);
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        Item r = getItemAt(row);
        ColumnIndex ci = getColumnsAt(col);
        return ci.column instanceof IndexedColumn ? ((IndexedColumn<Model, Item>) ci.column).isCellEditable(r, ci.indexWithinColGroup) : ci.column.isCellEditable(r);
    }

    protected abstract Item getItemAt(int row);

    public void updateCellValue(int row, int column) {
        Mutex.EVENT.postWriteRequest(() -> fireTableCellUpdated(row, column));
    }

    protected class ColumnIndex {

        private final Col column;
        private final int indexWithinColGroup;
        private final int colGroupStart;

        private ColumnIndex(Col column, int i, int start) {
            this.column = column;
            this.indexWithinColGroup = i;
            this.colGroupStart = start;
        }

        public Col getColumn() {
            return column;
        }

        public int getIndexWithinColGroup() {
            return indexWithinColGroup;
        }

        public int getColGroupStart() {
            return colGroupStart;
        }

    }

    public class PluggableColumnFactory extends ColumnFactory {

        protected boolean initialized = false;

        public PluggableColumnFactory() {
        }

        public void initialize(final Model ecal, final Lookup context) {
            if (!initialized) {
//                columns.forEach(itc -> itc.initialize(ecal, context));
                initialized = true;
            }
        }

        @Override
        public void configureColumnWidths(JXTable table, TableColumnExt col) {
            super.configureColumnWidths(table, col);
            int index = col.getModelIndex();
//            final String column = model(table).columns[index];
            ColumnIndex ci = getColumnsAt(index);
            final int preferredWidth = ci.column.getPreferredWidth();
            col.setPreferredWidth(preferredWidth);
            final Col itc = ci.column;
            itc.configureColumnWidth(col);
        }

        @Override
        public void configureTableColumn(final TableModel model, final TableColumnExt col) {
            super.configureTableColumn(model, col);
            AbstractPluggableTableModel<Model, Item, Col, CF> aptm = (AbstractPluggableTableModel<Model, Item, Col, CF>) model;
            int index = col.getModelIndex();
//            if (initialized) {
            ColumnIndex ci = getColumnsAt(index);//TODO: indexoutofbound, if not yet initialized
            final Col itc = ci.column;
            col.putClientProperty(PROP_COLUMN_ID, itc.columnId());
            col.putClientProperty(PROP_COLUMNS_INDEX, ci.indexWithinColGroup);
            col.putClientProperty(PROP_COLUMNS_START, ci.colGroupStart);
            col.setHeaderValue(itc instanceof IndexedColumn ? ((IndexedColumn) itc).getDisplayName(ci.indexWithinColGroup) : itc.getDisplayName());
            itc.configureTableColumn(aptm, col);
//            }
        }
    }
}
