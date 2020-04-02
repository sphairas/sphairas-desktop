/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tableimport.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.beans.VetoableChangeListener;
import java.util.HashSet;
import java.util.Set;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.NbBundle;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.tableimport.action.XmlCsvImportSettings;
import org.thespheres.betula.tableimport.impl.PrimaryUnitImportStudentItem;
import org.thespheres.betula.tableimport.impl.PrimaryUnitsXmlCsvItem;
import org.thespheres.betula.tableimport.util.VCardUtil;
import org.thespheres.betula.ui.swingx.FontFaceHighlighter;
import org.thespheres.betula.xmlimport.uiutil.DefaultColumns;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;
import org.thespheres.ical.VCard;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"PrimaryUnitDefaultColumns.product=No",
    "PrimaryUnitDefaultColumns.columnName.klasse=Klasse",
    "PrimaryUnitDefaultColumns.columnName.status=Status",
    "PrimaryUnitDefaultColumns.columnName.vCard=Adressdaten (Datenbank)",
    "PrimaryUnitDefaultColumns.columnName.primary.units=Klasse(n)",
    "PrimaryUnitDefaultColumns.columnName.student-id=ID",
    "PrimaryUnitDefaultColumns.columnName.selected="})
public abstract class PrimaryUnitDefaultColumns extends ImportTableColumn<PrimaryUnitImportStudentItem, ConfigurableImportTarget, XmlCsvImportSettings<PrimaryUnitsXmlCsvItem>, PrimaryUnitUpdateStudentsTableModel> {

    private final String product;

    PrimaryUnitDefaultColumns(String id, int position, boolean editable, int width, String product) {
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
        return NbBundle.getMessage(PrimaryUnitDefaultColumns.class, "PrimaryUnitDefaultColumns.columnName." + columnId(), product);
    }

    public final static class SelectedColumn extends DefaultColumns.DefaultCheckBoxColumn<PrimaryUnitImportStudentItem, ConfigurableImportTarget, XmlCsvImportSettings<PrimaryUnitsXmlCsvItem>, PrimaryUnitUpdateStudentsTableModel> {

        private VetoableChangeListener listener;

        SelectedColumn() {
            super("selected", 50);
        }

        @Override
        protected void initialize(PrimaryUnitImportStudentItem il) {
            il.addVetoableChangeListener(listener);
        }

        @Override
        public Boolean getColumnValue(PrimaryUnitImportStudentItem il) {
            return il.isSelected();
        }

        @Override
        public boolean setColumnValue(PrimaryUnitImportStudentItem il, Object value) {
            il.setSelected((boolean) value);
            return false;
        }

        @Override
        public String getDisplayName() {
            return "";
        }

    }

    static class KlasseColumn extends PrimaryUnitDefaultColumns {

        KlasseColumn(String product) {
            super("klasse", 120, false, 60, product);
        }

        @Override
        public String getColumnValue(PrimaryUnitImportStudentItem il) {
            return il.getSourceUnit();
        }

        @Override
        public void configureTableColumn(PrimaryUnitUpdateStudentsTableModel model, TableColumnExt col) {
            col.addHighlighter(new ColorHighlighter(Color.LIGHT_GRAY, null));
        }

    }

    static class StatusColumn extends PrimaryUnitDefaultColumns {

        StatusColumn(String product) {
            super("status", 170, false, 60, product);
        }

        @Override
        public Object getColumnValue(PrimaryUnitImportStudentItem il) {
            return il.getStatus();
        }

        @Override
        public void configureTableColumn(PrimaryUnitUpdateStudentsTableModel model, TableColumnExt col) {
            col.addHighlighter(new ColorHighlighter(Color.LIGHT_GRAY, null));
        }
    }

    static class VCardColumn extends PrimaryUnitDefaultColumns implements StringValue {

        VCardColumn(String product) {
            super("vCard", 500, false, 150, product);
        }

        @Override
        public VCard getColumnValue(PrimaryUnitImportStudentItem il) {
            return il.getVCard();
        }

        @Override
        public void configureTableColumn(final PrimaryUnitUpdateStudentsTableModel model, final TableColumnExt col) {
            class HP implements HighlightPredicate {

                @Override
                public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
                    int ri = adapter.convertRowIndexToModel(adapter.row);
                    return model.getItemAt(ri).isVCardUpdated();
                }
            }
            col.addHighlighter(new FontFaceHighlighter(Font.BOLD, new HP()));
            col.setCellRenderer(new DefaultTableRenderer(this));
        }

        @Override
        public String getString(Object value) {
            return value instanceof VCard ? VCardUtil.oneLine((VCard) value) : " ";
        }
    }

    static class PrimaryUnitsColumn extends PrimaryUnitDefaultColumns implements HighlightPredicate {

        PrimaryUnitsColumn(String product) {
            super("primary.units", 600, false, 60, product);
        }

        @Override
        public Object getColumnValue(PrimaryUnitImportStudentItem il) {
            return null; //il.getPrimaryUnitsAsString();
        }

        @Override
        public void configureTableColumn(PrimaryUnitUpdateStudentsTableModel model, TableColumnExt col) {
            col.addHighlighter(new ColorHighlighter(this, null, Color.RED));
        }

        @Override
        public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
//            adapter.getComponent()
            return false;
        }

    }

    static class IdColumn extends PrimaryUnitDefaultColumns implements StringValue {

        private String studentsAuthority;

        IdColumn(String product) {
            super("student-id", 750, false, 100, product);
        }

        @Override
        public void initialize(final ConfigurableImportTarget configuration, final XmlCsvImportSettings<PrimaryUnitsXmlCsvItem> wizard) {
            super.initialize(configuration, wizard);
            studentsAuthority = configuration.getStudentsAuthority();
        }

        @Override
        public void configureTableColumn(final PrimaryUnitUpdateStudentsTableModel model, final TableColumnExt col) {
            col.setCellRenderer(new DefaultTableRenderer(this));
        }

        @Override
        public Object getColumnValue(PrimaryUnitImportStudentItem il) {
            return il.getStudentId();
        }

        @Override
        public String getString(Object id) {
            if (id instanceof StudentId) {
                final StudentId stud = (StudentId) id;
                String ret = Long.toString(stud.getId());
                if (stud.getAuthority().equals(studentsAuthority)) {
                    return ret;
                }
                ret = ret + " (" + stud.getAuthority() + ")";
                return ret;
            }
            return " ";
        }
    }
}
