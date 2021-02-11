/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tableimport.ui;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.renderer.CheckBoxProvider;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;
import org.thespheres.betula.tableimport.csv.XmlCsvDictionary;
import org.thespheres.betula.tableimport.csv.XmlCsvDictionary.Entry;
import org.thespheres.betula.tableimport.csv.XmlCsvFile;
import org.thespheres.betula.tableimport.csv.XmlCsvFile.Column;
import org.thespheres.betula.ui.util.WideJXComboBox;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
class ConfigureDictionaryTableModel extends AbstractTableModel {

    final List<Item> items = new ArrayList<>();
    private XmlCsvDictionary dictionary;
    private boolean useGrouping = true;
    private final DefaultComboBoxModel<XmlCsvDictionary.Entry> editor = new DefaultComboBoxModel<>();

    void initialize(final XmlCsvFile[] f, final XmlCsvDictionary dict) {
        items.clear();
        this.dictionary = dict;
        for (final XmlCsvFile csv : f) {
            Arrays.stream(csv.getColumns())
                    .sorted(Comparator.comparing(c -> c.getLabel(), Collator.getInstance(Locale.getDefault())))
                    .map(c -> {
                        final Entry entry = findEntry(c);
                        return new Item(c, entry, csv.getId());
                    })
                    .forEach(items::add);
        }
        editor.removeAllElements();
        editor.addElement(null);
        if (dict != null) {
            Arrays.stream(dict.getEntries())
                    .forEach(editor::addElement);
        }
        fireTableDataChanged();
    }

    private Entry findEntry(final Column c) {
        return Arrays.stream(this.dictionary.getEntries())
                .filter(e -> e.getAssignedKey().equals(c.getAssignedKey()))
                .collect(CollectionUtil.requireSingleOrNull());
    }

    boolean useGrouping() {
        return useGrouping;
    }

    void updateGrouping(boolean value) {
        final boolean before = this.useGrouping;
        if (before != value) {
            this.useGrouping = value;
            fireTableStructureChanged();
        }
    }

    boolean changed() {
        return items.stream().anyMatch(i -> i.updated);
    }

    void reset() {
        items.stream().forEach(i -> i.updated = false);
    }

    ColumnFactory createColumnFactory() {
        return new ConfigureDictionaryColumnFactory();
    }

    @Override
    public int getRowCount() {
        return items.size();
    }

    @Override
    public int getColumnCount() {
        return useGrouping ? 3 : 2;
    }

    @Override
    public Object getValueAt(int r, int c) {
        final Item i = items.get(r);
        final int col = useGrouping ? c : c + 1;
        switch (col) {
            case 0:
                return i.column.isGroupingColumn();
            case 1:
                return i.getLabel();
            case 2:
                return i.entry;
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public boolean isCellEditable(int r, int c) {
        final int col = useGrouping ? c : c + 1;
        return col == 0 || col == 2;
    }

    @Override
    public void setValueAt(Object val, int r, int c) {
        final Item i = items.get(r);
        final int col = useGrouping ? c : c + 1;
        switch (col) {
            case 0:
                i.updateIsGroupingKey((boolean) val);
                break;
            case 2:
                i.updateEntry((Entry) val);
                break;
        }
    }

    private class Item {

        private final Column column;
        private XmlCsvDictionary.Entry entry;
        private final String csvId;
        private boolean updated = false;

        private Item(Column column, XmlCsvDictionary.Entry entry, String csvId) {
            this.column = column;
            this.entry = entry;
            this.csvId = csvId;
        }

        private void updateEntry(final Entry val) {
            this.column.setAssignedKey(val != null ? val.getAssignedKey() : null);
            this.entry = findEntry(column);
            updated = true;
        }

        private void updateIsGroupingKey(final boolean v) {
            this.column.setGroupingColumn(v);
            updated = true;
        }

        private String getLabel() {
            final String lbl = column.getLabel();
            if (csvId != null) {
                return " [" + csvId + "]" + lbl;
            } else {
                return lbl;
            }
        }

    }

    private class ConfigureDictionaryColumnFactory extends ColumnFactory implements StringValue {

        private final JXComboBox keysCombo;

        private ConfigureDictionaryColumnFactory() {
            keysCombo = new WideJXComboBox();
            keysCombo.setRenderer(new DefaultListRenderer(this));
            keysCombo.setModel(editor);
        }

        @Override
        public void configureColumnWidths(JXTable table, TableColumnExt tc) {
            final int c = tc.getModelIndex();
            final int col = useGrouping ? c : c + 1;
            if (col == 0) {
                tc.setMinWidth(16);
                tc.setMaxWidth(16);
            } else if (col == 1) {
                tc.setPreferredWidth(120);
            }
        }

        @Override
        public void configureTableColumn(TableModel model, TableColumnExt tc) {
            final int c = tc.getModelIndex();
            final int col = useGrouping ? c : c + 1;
            switch (col) {
                case 0:
                    tc.setHeaderValue("");
                    tc.setCellEditor(new DefaultCellEditor(new JCheckBox()));
                    tc.setCellRenderer(new DefaultTableRenderer(new CheckBoxProvider()));
                    break;
                case 1:
                    tc.setHeaderValue("Spalte");
                    break;
                case 2:
                    tc.setHeaderValue("");
                    tc.setCellEditor(new DefaultCellEditor(keysCombo));
                    tc.setCellRenderer(new DefaultTableRenderer(this));
                    break;
                default:
                    break;
            }
        }

        @Override
        public String getString(final Object value) {
            if (value instanceof XmlCsvDictionary.Entry) {
                final XmlCsvDictionary.Entry entry = (XmlCsvDictionary.Entry) value;
                return entry.getValue();
            }
            return "---";
        }

    }
}
