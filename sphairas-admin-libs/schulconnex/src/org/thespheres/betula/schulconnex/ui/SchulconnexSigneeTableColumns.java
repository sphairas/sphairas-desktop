/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.schulconnex.ui;

import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyVetoException;
import java.text.ParseException;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.swing.DefaultCellEditor;
import javax.swing.JFormattedTextField;
import javax.swing.JTable;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.NbBundle;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerConvention;
import org.thespheres.betula.document.MarkerFactory;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.schulconnex.Schulconnex;
import org.thespheres.betula.schulconnex.SchulconnexImportConfiguration;
import org.thespheres.betula.schulconnex.SchulconnexImportData;
import org.thespheres.betula.schulconnex.SchulconnexSigneeItem;
import org.thespheres.betula.services.util.SigneeStatus;
import org.thespheres.betula.xmlimport.ImportItem;
import org.thespheres.betula.xmlimport.uiutil.DefaultColumns;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn;

/**
 * Default columns for Schulconnex signee import.
 *
 * @author boris.heithecker
 */
class SchulconnexSigneeTableColumns {

    static Set<ImportTableColumn> create() {
        final String product = Schulconnex.getProduct().getDisplay();
        final Set<ImportTableColumn> ret = new LinkedHashSet<>();
        ret.add(new SelectedColumn());
        ret.add(new LehrendNodeColumn());
        ret.add(new SigneeColumn(product));
        ret.add(new StatusColumn());
        return ret;
    }

    static final class SelectedColumn extends DefaultColumns.DefaultCheckBoxColumn<SchulconnexSigneeItem, SchulconnexImportConfiguration, SchulconnexImportData<SchulconnexSigneeItem>, SchulconnexSigneeTableModel> {

        SelectedColumn() {
            super("selected", 100);
        }

        @Override
        public String getDisplayName() {
            return "";
        }

        @Override
        public Object getColumnValue(final SchulconnexSigneeItem item) {
            return item.isSelected();
        }

        @Override
        public boolean setColumnValue(final SchulconnexSigneeItem item, final Object value) {
            try {
                item.setSelected((Boolean) value);
            } catch (PropertyVetoException ex) {
            }
            return false;
        }
    }
    
    @NbBundle.Messages("SchulconnexSigneeTableColumns.LehrendNodeColumn.name=Lehrkraft")
    static class LehrendNodeColumn extends ImportTableColumn<SchulconnexSigneeItem, SchulconnexImportConfiguration, SchulconnexImportData<SchulconnexSigneeItem>, SchulconnexSigneeTableModel> {

        public LehrendNodeColumn() {
            super("node", 100, false, 250);
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(SchulconnexSigneeTableColumns.class, "SchulconnexSigneeTableColumns.LehrendNodeColumn.name");
        }

        @Override
        public Object getColumnValue(SchulconnexSigneeItem il) {
            return il;
        }

        @Override
        public void configureTableColumn(SchulconnexSigneeTableModel model, TableColumnExt col) {
            col.setCellRenderer(new DefaultTableRenderer(v -> ((ImportItem) v).getSourceNodeLabel()));
            col.addHighlighter(new ColorHighlighter(Color.LIGHT_GRAY, null));
        }

    }

    @NbBundle.Messages("SchulconnexSigneeTableColumns.SigneeColumn.name=Kennung/E-Mail")
    static final class SigneeColumn extends ImportTableColumn<SchulconnexSigneeItem, SchulconnexImportConfiguration, SchulconnexImportData<SchulconnexSigneeItem>, SchulconnexSigneeTableModel> {

        SigneeColumn(final String product) {
            super("signee", 200, true, 400);
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(SchulconnexSigneeTableColumns.class, "SchulconnexSigneeTableColumns.SigneeColumn.name");
        }

        @Override
        public Object getColumnValue(final SchulconnexSigneeItem item) {
            return item.getSignee();
        }

        @Override
        public void configureTableColumn(final SchulconnexSigneeTableModel model, final TableColumnExt col) {
            super.configureTableColumn(model, col);
            final JFormattedTextField tfield = new JFormattedTextField(new SigneeFormatter());
            tfield.setBorder(new LineBorder(Color.black, 2));

            class CellEditor extends DefaultCellEditor {

                private CellEditor() {
                    super(tfield);
                    tfield.removeActionListener(delegate);
                    delegate = new DefaultCellEditor.EditorDelegate() {
                        @Override
                        public void setValue(final Object value) {
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

            class PredicateExisting implements HighlightPredicate {

                @Override
                public boolean isHighlighted(final Component renderer, final ComponentAdapter adapter) {
                    final SchulconnexSigneeTableModel m = (SchulconnexSigneeTableModel) ((JTable) adapter.getComponent()).getModel();
                    final int row = adapter.convertRowIndexToModel(adapter.row);
                    return row < m.getRowCount() && m.getItemAt(row).getNameFromDatabase() != null;
                }
            }

            class AlienSuffixPredicate implements HighlightPredicate {

                @Override
                public boolean isHighlighted(final Component renderer, final ComponentAdapter adapter) {
                    final SchulconnexSigneeTableModel m = (SchulconnexSigneeTableModel) ((JTable) adapter.getComponent()).getModel();
                    final int row = adapter.convertRowIndexToModel(adapter.row);
                    return row < m.getRowCount() && m.getItemAt(row).isForeignSuffix();
                }
            }

            col.setCellEditor(new CellEditor());
            col.setCellRenderer(new DefaultTableRenderer((Object value) -> ((Signee) value).toString()));
            col.addHighlighter(new ColorHighlighter(new PredicateExisting(), null, Color.BLUE));
            col.addHighlighter(new ColorHighlighter(new AlienSuffixPredicate(), null, Color.RED));
        }
    }

    @NbBundle.Messages("SchulconnexSigneeTableColumns.columnName.status=Status")
    static final class StatusColumn extends DefaultColumns.DefaultMarkerColumn<SchulconnexSigneeItem, SchulconnexImportConfiguration, SchulconnexImportData<SchulconnexSigneeItem>, SchulconnexSigneeTableModel> {

        private final MarkerConvention[] conventions;

        StatusColumn() {
            super("status", 400, 75);
            conventions = new MarkerConvention[]{MarkerFactory.findConvention(SigneeStatus.NAME)};
        }

        @Override
        protected MarkerConvention[] getMarkerConventions(final SchulconnexImportConfiguration configuration) {
            return conventions;
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(SchulconnexSigneeTableColumns.class, "SchulconnexSigneeTableColumns.columnName.status");
        }

        @Override
        public Object getColumnValue(final SchulconnexSigneeItem item) {
            return item.getStatus();
        }

        @Override
        public boolean setColumnValue(final SchulconnexSigneeItem item, final Object value) {
            item.setStatus((Marker) value);
            return false;
        }
    }

    static class SigneeFormatter extends JFormattedTextField.AbstractFormatter implements DocumentListener {

        private JFormattedTextField jft;

        @Override
        public void install(final JFormattedTextField ftf) {
            super.install(ftf);
            jft = ftf;
            jft.getDocument().addDocumentListener(this);
        }

        @Override
        public void uninstall() {
            super.uninstall();
            if (jft != null) {
                jft.getDocument().removeDocumentListener(this);
                jft = null;
            }
        }

        @Override
        public Object stringToValue(final String text) throws ParseException {
            checkFormat(text);
            final String[] parts = StringUtils.trim(text).split("@");
            return new Signee(parts[0], parts[1], true);
        }

        @Override
        public String valueToString(final Object value) throws ParseException {
            return value != null ? ((Signee) value).toString() : null;
        }

        private void check() {
            final String text = jft.getText();
            try {
                checkFormat(text);
                jft.commitEdit();
            } catch (ParseException ex) {
                invalidEdit();
            }
        }

        private void checkFormat(final String text) throws ParseException {
            if (!text.trim().matches("[\\w]+(.[\\w]+)*@[\\w]+(.[\\w]+)*")) {
                throw new ParseException("Unparseable signee: \"" + text + "\"", 0);
            }
        }

        @Override
        public void insertUpdate(final DocumentEvent e) {
            check();
        }

        @Override
        public void removeUpdate(final DocumentEvent e) {
            check();
        }

        @Override
        public void changedUpdate(final DocumentEvent e) {
            check();
        }
    }
}
