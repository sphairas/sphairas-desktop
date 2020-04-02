/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admincontainer.unitsui;

import java.util.List;
import java.util.StringJoiner;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingConstants;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.AlignmentHighlighter;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.NbBundle;
import org.thespheres.betula.Unit;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.ui.util.WideJXComboBox;

/**
 *
 * @author boris.heithecker
 */
class UnitsUIColumnFactory extends ColumnFactory {

    private final StringValue studentStringValue = value -> ((RemoteStudent) value).getDirectoryName();
    private final StringValue unitStringValue = value -> value == null ? "---" : ((Unit) value).getDisplayName();
    private final StringValue unitsStringValue = value -> UnitsUITableModel.unitsToString((List<Unit>) value);

    UnitsUIColumnFactory(UnitsUIElement element) {
    }

    @Override
    public void configureColumnWidths(JXTable table, TableColumnExt col) {
        super.configureColumnWidths(table, col);
        int index = col.getModelIndex();
        switch (index) {
            case 0:
                col.setPreferredWidth(200);
                break;
            default:
                col.setPreferredWidth(110);
                break;
        }
    }

    @Override
    public void configureTableColumn(TableModel model, TableColumnExt col) {
        super.configureTableColumn(model, col);
        int index = col.getModelIndex();
        if (index == 0) {
            configureNamesColumn(model, col);
        } else {
            configureTargetColumn(model, col);
        }
    }

//    @NbBundle.Messages({"TargetsforStudentsElement.columnHeader.names=Name"})
    private void configureNamesColumn(TableModel model, TableColumnExt col) {
        final StringJoiner sj = new StringJoiner("<br>", "<html>", "</html>");
        final String header = NbBundle.getBundle("org.thespheres.betula.admin.units.ui.Bundle").getString("TargetsforStudentsElement.columnHeader.names");
        sj.add(header).add(" ");
        col.setHeaderValue(sj.toString());
        col.setCellRenderer(new DefaultTableRenderer(studentStringValue));
    }

    private void configureTargetColumn(TableModel m, final TableColumnExt col) {
        final UnitsUITableModel model = (UnitsUITableModel) m;
        final String name = model.getColumnNameAt(col.getModelIndex() - 1);
        col.setIdentifier(name);
        col.setHeaderValue(name);
        col.setToolTipText(name);
//        class PCL implements PropertyChangeListener {
//
//            @Override
//            public void propertyChange(PropertyChangeEvent evt) {
//                switch (evt.getPropertyName()) {
//                    case Name.PROP_DISPLAYNAME:
//                        col.setHeaderValue(name.getColumnLabel());
//                        break;
//                }
//            }
//
//        }
//        final PCL pcl = new PCL();
//        rd.getName().addPropertyChangeListener(pcl);
        final List<Unit> uu = model.getUnitsForColumn(name);
        if (uu != null && !uu.isEmpty()) {
            final DefaultComboBoxModel cbm = new DefaultComboBoxModel();
            final JXComboBox box = new WideJXComboBox(cbm);
            cbm.addElement(null);
            uu.forEach(cbm::addElement);
            box.setEditable(false);
            box.setRenderer(new DefaultListRenderer(unitStringValue));
            col.setCellEditor(new DefaultCellEditor(box));
        }
        col.setCellRenderer(new DefaultTableRenderer(unitsStringValue));
        col.addHighlighter(new AlignmentHighlighter(SwingConstants.CENTER));
    }
}
