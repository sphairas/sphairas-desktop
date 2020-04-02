/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.swingx.treetable;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;
import org.jdesktop.swingx.treetable.TreeTableModelProvider;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.thespheres.betula.ui.util.PluggableTableColumn;

/**
 *
 * @author boris.heithecker
 * @param <Model>
 * @param <Item>
 */
public abstract class NbPluggableSwingXTreeTableModel<Model, Item> extends NbSwingXTreeTableModel {

    public static final String PROP_COLUMN_ID = "column-id";
    public static final String PROP_COLUMNS_INDEX = "columns-index";
    public static final Object PROP_COLUMNS_START = "columns-start";
    private final String id;
    protected final List< PluggableTableColumn<Model, Item>> columns;
    private Model model;
    private Lookup context;
    private boolean initialized;

    protected NbPluggableSwingXTreeTableModel(final String id, final Node root, final Set<? extends PluggableTableColumn<Model, Item>> s) {
        super(root);
        this.id = id;
        columns = s.stream()
                .sorted(Comparator.comparingInt(PluggableTableColumn::getPosition))
                .collect(Collectors.toList());
    }

    public String id() {
        return id;
    }

    public synchronized void initialize(final Model model, final Lookup context) {
        this.model = model;
        this.context = context;
        columns.stream()
                .forEach(jtc -> jtc.initialize(model, context));
        initialized = true;
        modelSupport.fireNewRoot();
    }

    public Lookup getContext() {
        return context != null ? context : Lookup.EMPTY;
    }

    public Model getItemsModel() {
        return model;
    }

    @Override
    public PluggableSwingXTreeTableColumnFactory<Model, Item> createColumnFactory() {
        return new PluggableSwingXTreeTableColumnFactory();
    }

    @Override
    public int getColumnCount() {
        if (initialized) {
            return 1 + columns.stream()
                    .collect(Collectors.summingInt(this::getColumnsSize));
        }
        return 0;
    }

    private int getColumnsSize(PluggableTableColumn<Model, Item> c) {
        return c instanceof PluggableTableColumn.IndexedColumn ? ((PluggableTableColumn.IndexedColumn<Item, Model>) c).getColumnsSize() : 1;
    }

    protected NbPluggableSwingXTreeTableModel.ColumnIndex getColumnsAt(final int ci) {
        final int tableColumn = ci;// - 1;
        int current = 0;
        int before = 0;
        for (PluggableTableColumn<Model, Item> c : columns) {
            int start = current;
            current += c instanceof PluggableTableColumn.IndexedColumn ? ((PluggableTableColumn.IndexedColumn<Item, Model>) c).getColumnsSize() : 1;
            if (current > tableColumn) {
                return new NbPluggableSwingXTreeTableModel.ColumnIndex(c, tableColumn - before, start);
            }
            before = current;
        }
        return null; //SwingX problem?
//        throw new IndexOutOfBoundsException("Column index is " + tableColumn);
    }

    @Override
    public Object getValueAt(final Object node, final int column) {
        if (column == 0) {
            return null;
        }
//        assert EventQueue.isDispatchThread();//Called from TableBuilder outside awt
        final Item r = getItemAt(node);
        final NbPluggableSwingXTreeTableModel.ColumnIndex ci = getColumnsAt(column - 1);
        if (ci == null || r == null) {
            return null; //see getColumnsAt(int tableColumn) , SwingX Problem?
        }
        return ci.column instanceof PluggableTableColumn.IndexedColumn ? ((PluggableTableColumn.IndexedColumn<Model, Item>) ci.column).getColumnValue(r, ci.indexWithinColGroup) : ci.column.getColumnValue(r);
    }

    @Override
    public void setValueAt(final Object val, final Object node, final int column) {
        if (column == 0) {
            return;
        }
        final Item r = getItemAt(node);
        final NbPluggableSwingXTreeTableModel.ColumnIndex ci = getColumnsAt(column - 1);
        if (ci.column instanceof PluggableTableColumn.IndexedColumn ? ((PluggableTableColumn.IndexedColumn<Model, Item>) ci.column).setColumnValue(r, ci.indexWithinColGroup, val) : ci.column.setColumnValue(r, val)) {
//         modelSupport.fireChildrenChanged(parentPath, indices, children);
            //fireTableRowsUpdated(row, row);
        }
    }

    @Override
    public boolean isCellEditable(final Object node, final int col) {
        if (col == 0) {
            return false;
        }
        final Item r = getItemAt(node);
        final NbPluggableSwingXTreeTableModel.ColumnIndex ci = getColumnsAt(col - 1);
        return ci.column instanceof PluggableTableColumn.IndexedColumn ? ((PluggableTableColumn.IndexedColumn<Model, Item>) ci.column).isCellEditable(r, ci.indexWithinColGroup) : ci.column.isCellEditable(r);
    }

    protected abstract Item getItemAt(Object node);

    protected abstract Object getHierarchicalColumnHeader();

    protected abstract int getHierarchicalColumnWidth();

    protected class ColumnIndex {

        private final PluggableTableColumn<Model, Item> column;
        private final int indexWithinColGroup;
        private final int colGroupStart;

        private ColumnIndex(PluggableTableColumn<Model, Item> column, int i, int start) {
            this.column = column;
            this.indexWithinColGroup = i;
            this.colGroupStart = start;
        }

        public PluggableTableColumn<Model, Item> getColumn() {
            return column;
        }

        public int getIndexWithinColGroup() {
            return indexWithinColGroup;
        }

        public int getColGroupStart() {
            return colGroupStart;
        }

    }

    public class PluggableSwingXTreeTableColumnFactory<Model, Item> extends ColumnFactory {

        protected boolean initialized = false;

        public PluggableSwingXTreeTableColumnFactory() {
        }

        public void initialize(final Model ecal, final Lookup context) {
            if (!initialized) {
//                columns.forEach(itc -> itc.initialize(ecal, context));
                initialized = true;
            }
        }

        @Override
        public void configureColumnWidths(final JXTable table, final TableColumnExt col) {
            super.configureColumnWidths(table, col);
            final int index = col.getModelIndex();
            if (index != 0) {
                final NbPluggableSwingXTreeTableModel.ColumnIndex ci = getColumnsAt(index - 1);
                final int preferredWidth = ci.column.getPreferredWidth();
                col.setPreferredWidth(preferredWidth);
                final PluggableTableColumn<Model, Item> itc = ci.column;
                itc.configureColumnWidth(col);
            } else {
                final int preferredWidth = NbPluggableSwingXTreeTableModel.this.getHierarchicalColumnWidth();
                col.setPreferredWidth(preferredWidth);
            }
        }

        @Override
        public void configureTableColumn(final TableModel model, final TableColumnExt col) {
            super.configureTableColumn(model, col);
            int index = col.getModelIndex();
            if (index != 0) {
                //model is instanceof JXTreeTable.TreeTableModelAdapter;
                final TreeTableModelProvider ttmp = (TreeTableModelProvider) model;
                final NbPluggableSwingXTreeTableModel<Model, Item> aptm = (NbPluggableSwingXTreeTableModel<Model, Item>) ttmp.getTreeTableModel();
                final NbPluggableSwingXTreeTableModel.ColumnIndex ci = getColumnsAt(index - 1);//TODO: indexoutofbound, if not yet initialized
                final PluggableTableColumn<Model, Item> itc = ci.column;
                col.putClientProperty(PROP_COLUMN_ID, itc.columnId());
                col.putClientProperty(PROP_COLUMNS_INDEX, ci.indexWithinColGroup);
                col.putClientProperty(PROP_COLUMNS_START, ci.colGroupStart);
                col.setHeaderValue(itc instanceof PluggableTableColumn.IndexedColumn ? ((PluggableTableColumn.IndexedColumn) itc).getDisplayName(ci.indexWithinColGroup) : itc.getDisplayName());
                itc.configureTableColumn(aptm, col);
            } else {
                final Object headerValue = NbPluggableSwingXTreeTableModel.this.getHierarchicalColumnHeader();
                col.setHeaderValue(headerValue);
            }
        }
    }
}
