/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.uiutil;

import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import org.jdesktop.swingx.renderer.CheckBoxProvider;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.ui.util.AbstractPluggableTableModel;
import org.thespheres.betula.ui.util.PluggableTableColumn;
import org.thespheres.betula.ui.util.AbstractPluggableTableModel.PluggableColumnFactory;
import org.thespheres.betula.xmlimport.uiutil.MultiMarkerColumnTableModel.CF;
import org.thespheres.betula.xmlimport.uiutil.MultiMarkerColumnTableModel.MarkerSelection;

/**
 *
 * @author boris.heithecker
 */
class MultiMarkerColumnTableModel extends AbstractPluggableTableModel<Object, MarkerSelection, PluggableTableColumn<Object, MarkerSelection>, CF> {

    final List<MarkerSelection> list;
    private final Marker[] markers;

    MultiMarkerColumnTableModel(final Marker[] all, final Marker[] selected, final String displayContextName) {
        super("MultiMarkerColumnTableModel", createColumns(displayContextName));
        this.markers = all;
        list = createSelectionList(selected);
    }

    static Set<PluggableTableColumn<Object, MarkerSelection>> createColumns(final String displayContext) {
        final HashSet<PluggableTableColumn<Object, MarkerSelection>> ret = new HashSet<>();
        ret.add(new SelectedColumn());
        ret.add(new MarkerColumn() {

            @Override
            public String getDisplayName() {
                return displayContext;
            }

        });
        return ret;
    }

    @Override
    public synchronized void initialize(Object model, Lookup context) {
        super.initialize(model, context);
    }

    private List<MarkerSelection> createSelectionList(final Marker[] sel) {
        final List<MarkerSelection> ret = Arrays.stream(markers)
                .sorted(Comparator.comparing(m -> m.getLongLabel(), Collator.getInstance(Locale.getDefault())))
                .map(MarkerSelection::new)
                .collect(Collectors.toList());
        ret.stream()
                .filter(ms -> Arrays.stream(sel).anyMatch(ms.getMarker()::equals))
                .forEach(ms -> ms.setSelected(true));
        return ret;
    }

    public MarkerSelection[] getSelection() {
        return list == null ? null : list.stream().toArray(MarkerSelection[]::new);
    }

    @Override
    protected CF createColumnFactory() {
        return new CF();
    }

    @Override
    protected int getItemSize() {
        return list.size();
    }

    @Override
    protected MarkerSelection getItemAt(int row) {
        return list.get(row);
    }

    final static class SelectedColumn extends PluggableTableColumn<Object, MarkerSelection> {

        protected final JCheckBox box;

        SelectedColumn() {
            super("selected", 10, true, 16);
            box = new JCheckBox();
        }

        @Override
        public Boolean getColumnValue(MarkerSelection il) {
            return il.isSelected();
        }

        @Override
        public boolean setColumnValue(MarkerSelection il, Object value) {
            il.setSelected((boolean) value);
            return false;
        }

        @Override
        public String getDisplayName() {
            return "";
        }

        @Override
        public void configureTableColumn(AbstractPluggableTableModel<Object, MarkerSelection, ?, ?> model, TableColumnExt col) {
            col.setCellEditor(new DefaultCellEditor(box));
            col.setCellRenderer(new DefaultTableRenderer(new CheckBoxProvider()));
            col.setMinWidth(getPreferredWidth());
            col.setMaxWidth(getPreferredWidth());
        }

    }

    @NbBundle.Messages({"MultiMarkerColumnTableModel.MarkerColumn.columnName=Markierung"})
    static class MarkerColumn extends PluggableTableColumn<Object, MarkerSelection> {

        MarkerColumn() {
            super("marker", 100, false, 200);
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(MultiMarkerColumnTableModel.class, "MultiMarkerColumnTableModel.MarkerColumn.columnName");
        }

        @Override
        public Object getColumnValue(MarkerSelection il) {
            return il.getMarker().getLongLabel();
        }

    }

    class CF extends PluggableColumnFactory {
    }

    public static class MarkerSelection {

        private final Marker marker;
        private boolean selected = false;

        MarkerSelection(Marker m) {
            this.marker = m;
        }

        public Marker getMarker() {
            return marker;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

    }
}
