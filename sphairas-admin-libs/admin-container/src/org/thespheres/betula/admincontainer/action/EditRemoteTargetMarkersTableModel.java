/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admincontainer.action;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import org.jdesktop.swingx.renderer.CheckBoxProvider;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.thespheres.betula.admincontainer.action.EditRemoteTargetMarkersEdit.MarkerSelection;
import org.thespheres.betula.admincontainer.action.EditRemoteTargetMarkersTableModel.CF;
import org.thespheres.betula.ui.util.AbstractPluggableTableModel;
import org.thespheres.betula.ui.util.PluggableTableColumn;
import org.thespheres.betula.ui.util.AbstractPluggableTableModel.PluggableColumnFactory;

/**
 *
 * @author boris.heithecker
 */
class EditRemoteTargetMarkersTableModel extends AbstractPluggableTableModel<EditRemoteTargetMarkersEdit, EditRemoteTargetMarkersEdit.MarkerSelection, PluggableTableColumn<EditRemoteTargetMarkersEdit, EditRemoteTargetMarkersEdit.MarkerSelection>, CF> {

    private List<EditRemoteTargetMarkersEdit.MarkerSelection> list;

    EditRemoteTargetMarkersTableModel() {
        super("EditRemoteTargetMarkersTableModel", createColumns());
    }

    static Set<PluggableTableColumn<EditRemoteTargetMarkersEdit, EditRemoteTargetMarkersEdit.MarkerSelection>> createColumns() {
        final HashSet<PluggableTableColumn<EditRemoteTargetMarkersEdit, EditRemoteTargetMarkersEdit.MarkerSelection>> ret = new HashSet<>();
        ret.add(new SelectedColumn());
        ret.add(new MarkerColumn());
        return ret;
    }

    @Override
    public synchronized void initialize(EditRemoteTargetMarkersEdit model, Lookup context) {
        try {
            list = model.createSelectionList();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
        super.initialize(model, context);
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
    protected EditRemoteTargetMarkersEdit.MarkerSelection getItemAt(int row) {
        return list.get(row);
    }

    final static class SelectedColumn extends PluggableTableColumn<EditRemoteTargetMarkersEdit, EditRemoteTargetMarkersEdit.MarkerSelection> {

        protected final JCheckBox box;

        SelectedColumn() {
            super("selected", 10, true, 16);
            box = new JCheckBox();
        }

        @Override
        public Boolean getColumnValue(EditRemoteTargetMarkersEdit.MarkerSelection il) {
            return il.isSelected();
        }

        @Override
        public boolean setColumnValue(EditRemoteTargetMarkersEdit.MarkerSelection il, Object value) {
            il.setSelected((boolean) value);
            return false;
        }

        @Override
        public String getDisplayName() {
            return "";
        }

        @Override
        public void configureTableColumn(AbstractPluggableTableModel<EditRemoteTargetMarkersEdit, EditRemoteTargetMarkersEdit.MarkerSelection, ?, ?> model, TableColumnExt col) {
            col.setCellEditor(new DefaultCellEditor(box));
            col.setCellRenderer(new DefaultTableRenderer(new CheckBoxProvider()));
            col.setMinWidth(getPreferredWidth());
            col.setMaxWidth(getPreferredWidth());
        }

    }

    @NbBundle.Messages({"EditRemoteTargetMarkersTableModel.MarkerColumn.columnName=Markierung"})
    static class MarkerColumn extends PluggableTableColumn<EditRemoteTargetMarkersEdit, EditRemoteTargetMarkersEdit.MarkerSelection> {

        MarkerColumn() {
            super("marker", 100, false, 200);
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(EditRemoteTargetMarkersTableModel.class, "EditRemoteTargetMarkersTableModel.MarkerColumn.columnName");
        }

        @Override
        public Object getColumnValue(EditRemoteTargetMarkersEdit.MarkerSelection il) {
            return il.getMarker().getLongLabel();
        }

    }

    class CF extends PluggableColumnFactory {
    }

}
