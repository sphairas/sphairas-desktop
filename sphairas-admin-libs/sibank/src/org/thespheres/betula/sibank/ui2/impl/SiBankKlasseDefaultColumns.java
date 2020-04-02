/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.sibank.ui2.impl;

import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.HashSet;
import java.util.Set;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.thespheres.betula.sibank.SiBankImportTarget;
import org.thespheres.betula.sibank.SiBankImportStudentItem;
import org.thespheres.betula.sibank.SiBankImportData;
import org.thespheres.betula.sibank.impl.DelayedKlasseStudentSet.ImportStudentItemExt;
import org.thespheres.betula.sibank.ui2.SiBankUpdateStudentsTableModel;
import org.thespheres.betula.xmlimport.uiutil.DefaultColumns;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn;
import org.thespheres.ical.util.VCardHolder;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"SiBankKlasseDefaultColumns.product=SiBank Plus",
    "SiBankKlasseDefaultColumns.columnName.klasse=Klasse",
    "SiBankKlasseDefaultColumns.columnName.status=Status",
    "SiBankKlasseDefaultColumns.columnName.vCard=Adressdaten (Datenbank)",
    "SiBankKlasseDefaultColumns.columnName.primary.units=Klasse(n)",
    "SiBankKlasseDefaultColumns.columnName.student-id=ID",
    "SiBankKlasseDefaultColumns.columnName.selected="})
public abstract class SiBankKlasseDefaultColumns extends ImportTableColumn<SiBankImportStudentItem, SiBankImportTarget, SiBankImportData<SiBankImportStudentItem>, SiBankUpdateStudentsTableModel> {

    private final String product;

    SiBankKlasseDefaultColumns(String id, int position, boolean editable, int width, String product) {
        super(id, position, editable, width);
        this.product = product;
    }

    public static Set<ImportTableColumn> create(String product) {
        HashSet<ImportTableColumn> ret = new HashSet<>();
        ret.add(new DefaultColumns.NodeColumn(product));
        ret.add(new DefaultColumns.DeleteDateColumn());
        ret.add(new SelectedColumn());
        ret.add(new KlasseColumn(product));
        ret.add(new StatusColumn(product));
        ret.add(new VCardColumn(product));
        ret.add(new IdColumn(product));
//        ret.add(new PrimaryUnitsColumn(product));
        return ret;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(SiBankKlasseDefaultColumns.class, "SiBankKlasseDefaultColumns.columnName." + columnId(), product);
    }

    public final static class SelectedColumn extends DefaultColumns.DefaultCheckBoxColumn<SiBankImportStudentItem, SiBankImportTarget, SiBankImportData<SiBankImportStudentItem>, SiBankUpdateStudentsTableModel> {

        private VetoableChangeListener listener;

        SelectedColumn() {
            super("selected", 50);
        }

        @Override
        public void configureTableColumn(SiBankUpdateStudentsTableModel model, TableColumnExt col) {
            super.configureTableColumn(model, col);
            model.registerTableUpdatingProperty(SiBankImportStudentItem.PROP_SELECTED);
            listener = WeakListeners.vetoableChange(model, null);
        }

        @Override
        protected void initialize(SiBankImportStudentItem il) {
            il.addVetoableChangeListener(listener);
        }

        @Override
        public Boolean getColumnValue(SiBankImportStudentItem il) {
            return il.isSelected();
        }

        @Override
        public boolean setColumnValue(SiBankImportStudentItem il, Object value) {
            try {
                il.setSelected((boolean) value);
            } catch (PropertyVetoException ex) {
            }
            return false;
        }

        @Override
        public String getDisplayName() {
            return "";
        }

    }

    static class KlasseColumn extends SiBankKlasseDefaultColumns {

        KlasseColumn(String product) {
            super("klasse", 120, false, 60, product);
        }

        @Override
        public String getColumnValue(SiBankImportStudentItem il) {
            final String klasse = il.getSiBankKlasseItem().getKlasse();
            final String puString = il.getPrimaryUnitsAsString();
            if (puString == null || puString.equals(klasse)) {
                return klasse;
            }
            return klasse + " (" + puString + ")";
        }

        @Override
        public void configureTableColumn(SiBankUpdateStudentsTableModel model, TableColumnExt col) {
            col.addHighlighter(new ColorHighlighter(Color.LIGHT_GRAY, null));
        }

    }

    static class StatusColumn extends SiBankKlasseDefaultColumns {

        StatusColumn(String product) {
            super("status", 170, false, 60, product);
        }

        @Override
        public Object getColumnValue(SiBankImportStudentItem il) {
            return il.getSourceStatus();
        }

        @Override
        public void configureTableColumn(SiBankUpdateStudentsTableModel model, TableColumnExt col) {
            col.addHighlighter(new ColorHighlighter(Color.LIGHT_GRAY, null));
        }
    }

    static class VCardColumn extends SiBankKlasseDefaultColumns {

        VCardColumn(String product) {
            super("vCard", 500, false, 150, product);
        }

        @Override
        public Object getColumnValue(SiBankImportStudentItem il) {
            final ImportStudentItemExt iex = (ImportStudentItemExt) il;
            final VCardHolder vCard;
            if ((vCard = iex.getVCardOverride()) != null) {
                return vCard;
            }
            return null;
        }

    }

    static class PrimaryUnitsColumn extends SiBankKlasseDefaultColumns implements HighlightPredicate {

        PrimaryUnitsColumn(String product) {
            super("primary.units", 600, false, 60, product);
        }

        @Override
        public Object getColumnValue(SiBankImportStudentItem il) {
            return il.getPrimaryUnitsAsString();
        }

        @Override
        public void configureTableColumn(SiBankUpdateStudentsTableModel model, TableColumnExt col) {
            col.addHighlighter(new ColorHighlighter(this, null, Color.RED));
        }

        @Override
        public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
//            adapter.getComponent()
            return false;
        }

    }

    static class IdColumn extends SiBankKlasseDefaultColumns {

        IdColumn(String product) {
            super("student-id", 750, false, 100, product);
        }

        @Override
        public Object getColumnValue(SiBankImportStudentItem il) {
            return il.getStudentId().getId();
        }
    }
}
