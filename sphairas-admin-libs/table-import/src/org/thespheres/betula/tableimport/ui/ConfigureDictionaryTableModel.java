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
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
class ConfigureDictionaryTableModel extends AbstractTableModel {

    final List<Item> items = new ArrayList<>();
    private boolean useGrouping = true;
    private final DefaultComboBoxModel<XmlCsvDictionary.Entry> editor = new DefaultComboBoxModel<>();

    void initialize(final XmlCsvFile[] f, final XmlCsvDictionary defaultDictionary) {
        items.clear();
        for (XmlCsvFile csv : f) {
            final XmlCsvDictionary d = csv.getDictionary();
            if (d == null) {
                continue;
            }
            Arrays.stream(csv.getColumns())
                    .sorted(Comparator.comparing(c -> c.getLabel(), Collator.getInstance(Locale.getDefault())))
                    .map(c -> {
                        final Entry entry = findEntry(d, c);
                        return new Item(c, entry, csv.getId(), d);
                    })
                    .forEach(items::add);
        }
        editor.removeAllElements();
        editor.addElement(null);
        if (defaultDictionary != null) {
            Arrays.stream(defaultDictionary.getEntries())
                    .forEach(editor::addElement);
        }
        fireTableDataChanged();
    }

    private static Entry findEntry(final XmlCsvDictionary d, Column c) {
        final Entry entry = Arrays.stream(d.getEntries())
                .filter(e -> e.getAssignedKey().equals(c.getAssignedKey()))
                .collect(CollectionUtil.requireSingleOrNull());
        return entry;
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
                return i.entry != null && i.entry.isIsGroupingKey();
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
                i.updateIsGroupingKey((Boolean) val);
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
        private final XmlCsvDictionary dictionary;

        private Item(Column column, XmlCsvDictionary.Entry entry, String csvId, final XmlCsvDictionary d) {
            this.column = column;
            this.entry = entry;
            this.csvId = csvId;
            this.dictionary = d;
        }

        private void updateEntry(Entry val) {
            this.column.setAssignedKey(val != null ? val.getAssignedKey() : null);
            this.entry = findEntry(dictionary, column);
            updated = true;
        }

        private void updateIsGroupingKey(Boolean v) {
            if (entry != null) {
                entry.setIsGroupingKey(v);
                updated = true;
            }
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
            keysCombo = new JXComboBox();
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
        public String getString(Object value) {
            if (value instanceof XmlCsvDictionary.Entry) {
                final XmlCsvDictionary.Entry entry = (XmlCsvDictionary.Entry) value;
                return entry.getValue().split(",")[0];
            }
            return "---";
        }

    }
}
