/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminconfig;

import java.awt.Color;
import java.awt.Font;
import java.util.HashSet;
import java.util.Set;
import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.NbBundle;
import org.thespheres.betula.ui.swingx.FontFaceHighlighter;
import org.thespheres.betula.ui.util.AbstractPluggableTableModel;
import org.thespheres.betula.ui.util.PluggableTableColumn;

/**
 *
 * @author boris.heithecker
 */
public abstract class AppResourcesConfigTableColumn extends PluggableTableColumn<AppResourcesProperties, AppResourcesProperty> {

    static final Highlighter FOR_REMOVAL = new ColorHighlighter((c, ca) -> ca.getValue() instanceof AppResourcesProperty && ((AppResourcesProperty) ca.getValue()).isForRemoval(), null, Color.GRAY);
    static final Highlighter UPDATED = new FontFaceHighlighter(Font.BOLD, (c, ca) -> ca.getValue() instanceof AppResourcesProperty && (((AppResourcesProperty) ca.getValue()).isModified() || ((AppResourcesProperty) ca.getValue()).isTemplate()));

    protected AppResourcesConfigTableColumn(String id, int position, boolean editable, int width) {
        super(id, position, editable, width);
    }

    public static Set<PluggableTableColumn<AppResourcesProperties, AppResourcesProperty>> createDefaultSet() {
        final HashSet<PluggableTableColumn<AppResourcesProperties, AppResourcesProperty>> ret = new HashSet<>();
        ret.add(new KeyColumn());
        ret.add(new ValueColumn());
        return ret;
    }

    @NbBundle.Messages({"KeyColumn.displayName=Schlüssel"})
    public static class KeyColumn extends AppResourcesConfigTableColumn implements StringValue {

        private final JTextField editor = new JTextField();

        protected KeyColumn() {
            super("key", 100, false, 400);
            editor.setBorder(new LineBorder(Color.black, 2));
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(KeyColumn.class, "KeyColumn.displayName");
        }

        @Override
        public boolean isCellEditable(final AppResourcesProperty il) {
            return il.isTemplate();
        }

        @Override
        public String getString(Object value) {
            return ((AppResourcesProperty) value).getKey();
        }

        @Override
        public Object getColumnValue(final AppResourcesProperty il) {
            return il;
        }

        @Override
        public boolean setColumnValue(final AppResourcesProperty il, final Object value) {
            final String val = (String) value;
            return il.setKey(val);
        }

        @Override
        public void configureTableColumn(final AbstractPluggableTableModel<AppResourcesProperties, AppResourcesProperty, ?, ?> model, final TableColumnExt col) {
            col.setCellRenderer(new DefaultTableRenderer(this));
            final class KeyTextEditor extends DefaultCellEditor {

                KeyTextEditor() {
                    super(editor);
                    editor.removeActionListener(delegate);
                    delegate = new DefaultCellEditor.EditorDelegate() {
                        @Override
                        public void setValue(final Object value) {
                            editor.setText((value != null) ? ((AppResourcesProperty) value).getKey() : "");
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
            col.addHighlighter(FOR_REMOVAL);
        }
    }

    @NbBundle.Messages({"ValueColumn.displayName=Wert"})
    public static class ValueColumn extends AppResourcesConfigTableColumn implements StringValue {

        private final JTextField editor = new JTextField();

        ValueColumn() {
            super("value", 200, true, 600);
            editor.setBorder(new LineBorder(Color.black, 2));
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(ValueColumn.class, "ValueColumn.displayName");
        }

        @Override
        public Object getColumnValue(final AppResourcesProperty il) {
            return il;
        }

        @Override
        public String getString(Object value) {
            return ((AppResourcesProperty) value).getValue();
        }

        @Override
        public boolean setColumnValue(final AppResourcesProperty il, final Object value) {
            final String val = (String) value;
            return il.setValue(val);
        }

        @Override
        public void configureTableColumn(final AbstractPluggableTableModel<AppResourcesProperties, AppResourcesProperty, ?, ?> model, final TableColumnExt col) {
            col.setCellRenderer(new DefaultTableRenderer(this));
            final class ValueTextEditor extends DefaultCellEditor {

                ValueTextEditor() {
                    super(editor);
                    editor.removeActionListener(delegate);
                    delegate = new EditorDelegate() {
                        @Override
                        public void setValue(final Object value) {
                            editor.setText((value != null) ? ((AppResourcesProperty) value).getValue() : "");
                        }

                        @Override
                        public Object getCellEditorValue() {
                            return editor.getText();
                        }
                    };
                    editor.addActionListener(delegate);
                }

            }
            col.setCellEditor(new ValueTextEditor());
            col.addHighlighter(UPDATED);
            col.addHighlighter(FOR_REMOVAL);
        }

    }

    public static abstract class Factory extends PluggableTableColumn.Factory<PluggableTableColumn<AppResourcesProperties, AppResourcesProperty>> {
    }
}
