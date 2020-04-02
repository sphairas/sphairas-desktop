/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminconfig.uiutil;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.renderer.CheckBoxProvider;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.Tag;
import org.thespheres.betula.document.MarkerFactory;
import org.thespheres.betula.tag.TagConvention;
import org.thespheres.betula.ui.util.WideJXComboBox;

/**
 *
 * @author boris.heithecker
 */
@Messages({"EditBemerkungenSetModelColumnFactory.always=Immer",
    "EditBemerkungenSetModelColumnFactory.header.hidden=Sichtbar",
    "EditBemerkungenSetModelColumnFactory.header.term=Halbjahr",
    "EditBemerkungenSetModelColumnFactory.header.level=Jahrgänge"})
class EditXmlMarkerConventionColumnFactory extends ColumnFactory {

    private final DefaultComboBoxModel<Tag> model = new DefaultComboBoxModel<>();
    private final JCheckBox hiddenBox = new JCheckBox();
    private final JTextField levelField = new JTextField();
    private final WideJXComboBox scopeBox;
    private final StringValue scopeStringValue = o -> {
        final Tag val = (Tag) o;
        if (val == null) {
            return "";
        }
        return Tag.NULL.equals(val) ? NbBundle.getMessage(EditXmlMarkerConventionColumnFactory.class, "EditBemerkungenSetModelColumnFactory.always") : val.getLongLabel();
    };

    EditXmlMarkerConventionColumnFactory() {
        final TagConvention<Tag> cnv = MarkerFactory.findTagConvention("de.halbjahre");
        model.addElement(null);
        for (final Tag t : cnv) {
            model.addElement(t);
        }
        scopeBox = new WideJXComboBox();
        scopeBox.setModel(model);
        scopeBox.setRenderer(new DefaultListRenderer(scopeStringValue));
        scopeBox.setEditable(false);
        scopeBox.setBorder(BorderFactory.createEmptyBorder());
//        AutoCompleteDecorator.decorate(scopeBox);
    }

    @Override
    public void configureColumnWidths(JXTable table, TableColumnExt col) {
        super.configureColumnWidths(table, col);
        int index = col.getModelIndex();
        switch (index) {
            case 0:
                col.setPreferredWidth(600);
                break;
            case 1:
                col.setPreferredWidth(30);
                break;
            case 2:
                col.setPreferredWidth(150);
                break;
            case 3:
                col.setPreferredWidth(150);
                break;
        }
    }

    @Override
    public void configureTableColumn(TableModel model, TableColumnExt col) {
        super.configureTableColumn(model, col);
        final int index = col.getModelIndex();
        switch (index) {
            case 0:
                col.setHeaderValue("");
                break;
            case 1:
                configureHiddenColumn(col);
                break;
            case 2:
                configureScopeColumn(col);
                break;
            case 3:
                configureLevelColumn(col);
                break;
        }
    }

    private void configureHiddenColumn(TableColumnExt col) {
        final String title = NbBundle.getMessage(EditXmlMarkerConventionColumnFactory.class, "EditBemerkungenSetModelColumnFactory.header.hidden");
        col.setHeaderValue(title);
        col.setCellRenderer(new DefaultTableRenderer(new CheckBoxProvider()));
        col.setCellEditor(new DefaultCellEditor(hiddenBox));
    }

    private void configureScopeColumn(TableColumnExt col) {
        final String title = NbBundle.getMessage(EditXmlMarkerConventionColumnFactory.class, "EditBemerkungenSetModelColumnFactory.header.term");
        col.setHeaderValue(title);
        col.setCellEditor(new DefaultCellEditor(scopeBox));
        col.setCellRenderer(new DefaultTableRenderer(scopeStringValue));
    }

    private void configureLevelColumn(TableColumnExt col) {
        final String title = NbBundle.getMessage(EditXmlMarkerConventionColumnFactory.class, "EditBemerkungenSetModelColumnFactory.header.level");
        col.setHeaderValue(title);
        col.setCellEditor(new DefaultCellEditor(levelField));
//        col.setCellRenderer(new DefaultTableRenderer(scopeStringValue));
    }
}
