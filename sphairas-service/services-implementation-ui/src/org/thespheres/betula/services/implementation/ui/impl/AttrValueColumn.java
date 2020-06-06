/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.impl;

import java.awt.Color;
import java.awt.Font;
import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.NbBundle;
import org.thespheres.betula.adminconfig.layerxml.AbstractLayerFile;
import org.thespheres.betula.adminconfig.layerxml.LayerFileSystem;
import org.thespheres.betula.services.implementation.ui.layerxml.LayerFileAttribute;
import org.thespheres.betula.ui.swingx.FontFaceHighlighter;
import org.thespheres.betula.ui.swingx.treetable.NbPluggableSwingXTreeTableModel;
import org.thespheres.betula.ui.util.PluggableTableColumn;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"AttrValueColumn.displayName=Attributwert"})
class AttrValueColumn extends PluggableTableColumn<LayerFileSystem, AbstractLayerFile> implements StringValue {

    static final Highlighter UPDATED = new FontFaceHighlighter(Font.BOLD, (c, ca) -> ca.getValue() instanceof LayerFileAttribute && (((LayerFileAttribute) ca.getValue()).isModified() || ((LayerFileAttribute) ca.getValue()).isTemplate()));
    private final JTextField editor = new JTextField();

    AttrValueColumn() {
        super("value", 1000, false, 200);
        editor.setBorder(new LineBorder(Color.black, 2));
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(AttrValueColumn.class, "AttrValueColumn.displayName");
    }

    @Override
    public boolean isCellEditable(final AbstractLayerFile il) {
        return il instanceof LayerFileAttribute;
    }

    @Override
    public Object getColumnValue(final AbstractLayerFile il) {
        if (il instanceof LayerFileAttribute) {
            return (LayerFileAttribute) il;
        }
        return null;
    }

    @Override
    public String getString(Object value) {
        return value != null ? ((LayerFileAttribute) value).toString() : "";
    }

    @Override
    public boolean setColumnValue(final AbstractLayerFile il, final Object value) {
        try {
            ((LayerFileAttribute) il).setAttribute(value.toString());
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    @Override
    public void configureTableColumn(final NbPluggableSwingXTreeTableModel<LayerFileSystem, AbstractLayerFile> model, final TableColumnExt col) {
        col.setCellRenderer(new DefaultTableRenderer(this));
        final class KeyTextEditor extends DefaultCellEditor {

            KeyTextEditor() {
                super(editor);
                editor.removeActionListener(delegate);
                delegate = new DefaultCellEditor.EditorDelegate() {
                    @Override
                    public void setValue(final Object value) {
                        editor.setText((value != null) ? ((LayerFileAttribute) value).toString() : "");
                    }

                    @Override
                    public Object getCellEditorValue() {
                        return editor.getText();
                    }
                };
                editor.addActionListener(delegate);
            }

        }
        col.setCellEditor(new KeyTextEditor());
        col.addHighlighter(UPDATED);
    }

}
