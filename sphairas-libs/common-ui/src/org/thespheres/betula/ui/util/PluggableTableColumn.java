/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jdesktop.swingx.table.TableColumnExt;
import org.netbeans.spi.editor.mimelookup.MimeLocation;
import org.openide.util.Lookup;
import org.thespheres.betula.ui.swingx.treetable.NbPluggableSwingXTreeTableModel;
import org.thespheres.betula.util.Ordered;

/**
 *
 * @author boris.heithecker
 * @param <Model>
 * @param <Item>
 */
public abstract class PluggableTableColumn<Model, Item> implements Ordered {

    protected final int position;
    protected final int width;
    protected boolean editable;
    protected final String id;
    protected boolean initialized;
    protected Lookup context;
    protected Model model;

    protected PluggableTableColumn(final String id, final int position, final boolean editable, final int width) {
        this.position = position;
        this.width = width;
        this.editable = editable;
        this.id = id;
    }

    public String columnId() {
        return id;
    }

    public abstract String getDisplayName();

    protected Lookup getContext() {
        return context;
    }

    public void initialize(Model model, Lookup context) {
        this.model = model;
        this.context = context != null ? context : Lookup.EMPTY;
        initialized = true;
    }

    protected Model getModel() {
        if (initialized) {
            return model;
        }
        throw new IllegalStateException(getClass().getCanonicalName() + " not initialized.");
    }

    public int getPreferredWidth() {
        return width;
    }

    @Override
    public int getPosition() {
        return position;
    }

    public abstract Object getColumnValue(Item il);

    public boolean isCellEditable(Item il) {
        return editable;
    }

    public boolean setColumnValue(Item il, Object value) {
        return false;
    }

    public void configureColumnWidth(TableColumnExt col) {
    }

    public void configureTableColumn(AbstractPluggableTableModel<Model, Item, ?, ?> model, TableColumnExt col) {
    }

    public void configureTableColumn(NbPluggableSwingXTreeTableModel<Model, Item> model, TableColumnExt col) {
    }

    @Override
    public int compareTo(Ordered o) {
        return Integer.compare(getPosition(), o.getPosition());
    }

    public static abstract class IndexedColumn<Model, Item> extends PluggableTableColumn<Model, Item> {

        protected IndexedColumn(String id, int position, boolean editable, int width) {
            super(id, position, editable, width);
        }

        @Override
        public final String getDisplayName() {
            throw new UnsupportedOperationException("Must not be called.");
        }

        public abstract String getDisplayName(int index);

        @Override
        public Object getColumnValue(Item il) {
            return getColumnValue(il, 0);
        }

        @Override
        public boolean setColumnValue(Item il, Object value) {
            return setColumnValue(il, 0, value);
        }

        public abstract int getColumnsSize();

        public abstract Object getColumnValue(Item il, int index);

        public boolean isCellEditable(Item il, int index) {
            return super.isCellEditable(il);
        }

        public boolean setColumnValue(Item il, int index, Object value) {
            return false;
        }

    }

    @MimeLocation(subfolderName = "TableColumn")
    public static abstract class Factory<C extends PluggableTableColumn> {

        public abstract C createInstance();

        @Retention(RetentionPolicy.SOURCE)
        @Target({ElementType.TYPE})
        public static @interface Registration {

            public String component();
        }
    }
}
