/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis.ui;

import org.thespheres.betula.gpuntis.UntisImportSigneeItem;
import java.awt.Color;
import java.awt.Component;
import java.text.ParseException;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JTable;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.renderer.CheckBoxProvider;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;
import org.thespheres.betula.document.Signee;

/**
 *
 * @author boris.heithecker
 */
class UntisSigneeImportColumnFactory extends ColumnFactory {

    @Override
    public void configureColumnWidths(JXTable table, TableColumnExt col) {
        super.configureColumnWidths(table, col);
        int index = col.getModelIndex();
        switch (index) {
            case 0:
                col.setPreferredWidth(16);
                col.setMaxWidth(16);
                break;
            case 1:
                col.setPreferredWidth(170);
                break;
            //                case 2:
            //                    col.setPreferredWidth(170);
            //                    break;
        }
    }

    @Override
    public void configureTableColumn(TableModel model, TableColumnExt col) {
        super.configureTableColumn(model, col);
        int index = col.getModelIndex();
//        col.setHeaderValue(UntisSigneeImportTableModel.columnNames[index]);
        if (index == 0) {
            col.setCellRenderer(new DefaultTableRenderer(new CheckBoxProvider()));
            col.setCellEditor(new DefaultCellEditor(new JCheckBox()));
        } else if (index == 1 || index == 2) {
            class NewPredicate implements HighlightPredicate {

                @Override
                public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
                    UntisSigneeImportTableModel m = (UntisSigneeImportTableModel) ((JTable) adapter.getComponent()).getModel();
                    int r = adapter.convertRowIndexToModel(adapter.row);
                    if (r < m.getRowCount()) {
                        UntisImportSigneeItem sig = m.getItemAt(r);
                        return !sig.isRemote();
                    }
                    return false;
                }
            }

            class ForeignSuffixPredicate implements HighlightPredicate {

                @Override
                public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
                    UntisSigneeImportTableModel m = (UntisSigneeImportTableModel) ((JTable) adapter.getComponent()).getModel();
                    int r = adapter.convertRowIndexToModel(adapter.row);
                    if (r < m.getRowCount()) {
                        UntisImportSigneeItem sig = m.getItemAt(r);
                        return sig.isForeignSuffix();
                    }
                    return false;
                }
            }
            col.addHighlighter(new ColorHighlighter(new NewPredicate(), null, Color.BLUE));
            col.addHighlighter(new ColorHighlighter(new ForeignSuffixPredicate(), null, Color.RED));
        }
        if (index == 1) {
            final JFormattedTextField tfield = new JFormattedTextField(new SigneeFormatter());
            tfield.setBorder(new LineBorder(Color.black, 2));

            class CellEditor extends DefaultCellEditor {

                private CellEditor() {
                    super(tfield);
                    tfield.removeActionListener(delegate);
                    delegate = new DefaultCellEditor.EditorDelegate() {
                        @Override
                        public void setValue(Object value) {
                            tfield.setValue(value);
                        }

                        @Override
                        public Object getCellEditorValue() {
                            return tfield.getValue();
                        }
                    };
                    tfield.addActionListener(delegate);
                }
            }
            col.setCellEditor(new CellEditor());
            col.setCellRenderer(new DefaultTableRenderer((java.lang.Object value) -> ((Signee) value).toString()));
        }
    }

    private static class SigneeFormatter extends JFormattedTextField.AbstractFormatter implements DocumentListener {

        private JFormattedTextField jft;

        @Override
        public void install(JFormattedTextField ftf) {
            super.install(ftf);
            this.jft = ftf;
            this.jft.getDocument().addDocumentListener(this);
        }

        @Override
        public void uninstall() {
            super.uninstall();
            if (this.jft != null) {
                this.jft.getDocument().removeDocumentListener(this);
                this.jft = null;
            }
        }

        @Override
        public Object stringToValue(String text) throws ParseException {
            checkFormat(text);
            String[] parts = StringUtils.trim(text).split("@");
            return new Signee(parts[0], parts[1], true);
        }

        @Override
        public String valueToString(Object value) throws ParseException {
            return value != null ? ((Signee) value).toString() : null;
        }

        private void check() {
            String text = jft.getText();
            try {
                checkFormat(text);
                jft.commitEdit();
            } catch (ParseException ex) {
                invalidEdit();
            }
        }

        private void checkFormat(String text) throws ParseException {
            if (!text.trim().matches("[\\w]+(.[\\w]+)*@[\\w]+(.[\\w]+)*")) {
                throw new ParseException("Unparseable signee: \"" + text + "\"", 0);
            }
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            check();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            check();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            check();
        }
    }
}
