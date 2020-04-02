/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.uiutil;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.WeakSet;
import org.thespheres.betula.util.Ordered;
import org.thespheres.betula.xmlimport.ImportItem;
import org.thespheres.betula.xmlimport.ImportTarget;

/**
 *
 * @author boris.heithecker
 * @param <I>
 * @param <T>
 * @param <W>
 * @param <M>
 */
public abstract class ImportTableColumn<I extends ImportItem, T extends ImportTarget, W extends ImportWizardSettings, M extends ImportTableModel<I, W>> implements Ordered {

    private final int position;
    private final int width;
    private final boolean editable;
    private final String id;
    protected final Set<I> initialized;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    protected ImportTableColumn(String id, int position, boolean editable, int width) {
        this.position = position;
        this.width = width;
        this.editable = editable;
        this.id = id;
        initialized = createInitializedSet();
    }

    protected Set<I> createInitializedSet() {
        return new WeakSet<>();
    }

    public String columnId() {
        return id;
    }

    public abstract String getDisplayName();

    public boolean isCellEditable(I il) {
        return editable;
    }

    public int getPreferredWidth() {
        return width;
    }

    @Override
    public int getPosition() {
        return position;
    }

    protected void initializeIfNot(I il) {
        synchronized (initialized) {
            if (!initialized.contains(il)) {
                initialize(il);
                initialized.add(il);
            }
        }
    }

    protected void initialize(I il) {
    }

    public abstract Object getColumnValue(I il);

    //true -->         fireTableRowsUpdated(row, row);//wegen getTargetDocumentId
    public boolean setColumnValue(I il, Object value) {
        return false;
    }

    public void initialize(T configuration, W wizard) {
    }

    public void configureTableColumn(M model, TableColumnExt col) {
    }

    @Override
    public int compareTo(Ordered o) {
        return Integer.compare(getPosition(), o.getPosition());
    }

    public static abstract class Factory {

        public abstract ImportTableColumn createInstance();

        @Retention(RetentionPolicy.SOURCE)
        @Target({ElementType.TYPE})
        public static @interface Registration {

            public String component();
        }

        @Retention(RetentionPolicy.SOURCE)
        @Target({ElementType.TYPE})
        public static @interface Registrations {

            public Registration[] value();
        }
    }
}
