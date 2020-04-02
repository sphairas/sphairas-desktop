/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis.ui.impl;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.beans.PropertyVetoException;
import java.text.ParseException;
import java.util.HashSet;
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
import org.jdesktop.swingx.decorator.FontHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.NbBundle;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerConvention;
import org.thespheres.betula.document.MarkerFactory;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.gpuntis.UntisImportConfiguration;
import org.thespheres.betula.gpuntis.UntisImportData;
import org.thespheres.betula.gpuntis.UntisImportSigneeItem;
import org.thespheres.betula.services.util.SigneeStatus;
import org.thespheres.betula.gpuntis.ui.UntisSigneeImportTableModel;
import org.thespheres.betula.xmlimport.uiutil.DefaultColumns.DefaultCheckBoxColumn;
import org.thespheres.betula.xmlimport.uiutil.DefaultColumns.DefaultMarkerColumn;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"UntisSigneesDefaultColumns.columnName.signee=Kennung/iserv-Email",
    "UntisSigneesDefaultColumns.columnName.name=Name",
    "UntisSigneesDefaultColumns.columnName.status=Status"})
public abstract class UntisSigneesDefaultColumns extends ImportTableColumn<UntisImportSigneeItem, UntisImportConfiguration, UntisImportData, UntisSigneeImportTableModel> {

    private final String product;

    UntisSigneesDefaultColumns(String id, int position, boolean editable, int width, String product) {
        super(id, position, editable, width);
        this.product = product;
    }

    public static Set<ImportTableColumn> create(String product) {
        HashSet<ImportTableColumn> ret = new HashSet<>();
        ret.add(new UntisSigneeSelectedColumn());
        ret.add(new SigneeColumn(product));
        ret.add(new NameColumn(product));
        ret.add(new StatusColumn());
        return ret;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(UntisSigneesDefaultColumns.class, "UntisSigneesDefaultColumns.columnName." + columnId(), product);
    }

    final static class UntisSigneeSelectedColumn extends DefaultCheckBoxColumn<UntisImportSigneeItem, UntisImportConfiguration, UntisImportData, UntisSigneeImportTableModel> {

        UntisSigneeSelectedColumn() {
            super("selected", 100);
        }

        @Override
        public String getDisplayName() {
            return "";
        }

        @Override
        public Object getColumnValue(UntisImportSigneeItem il) {
            return il.isSelected();
        }

        @Override
        public boolean setColumnValue(UntisImportSigneeItem il, Object value) {
            try {
                il.setSelected((Boolean) value);
            } catch (PropertyVetoException ex) {
            }
            return false;
        }

    }

    final static class SigneeColumn extends UntisSigneesDefaultColumns {

        private SigneeColumn(String product) {
            super("signee", 200, false, 400, product);
        }

        @Override
        public Object getColumnValue(UntisImportSigneeItem il) {
            return il.getSignee();
        }

        @Override
        public void configureTableColumn(UntisSigneeImportTableModel model, TableColumnExt col) {
            super.configureTableColumn(model, col);
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

            class PredicateExisting implements HighlightPredicate {

                @Override
                public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
                    UntisSigneeImportTableModel m = (UntisSigneeImportTableModel) ((JTable) adapter.getComponent()).getModel();
                    int r = adapter.convertRowIndexToModel(adapter.row);
                    if (r < m.getRowCount()) {
                        UntisImportSigneeItem sig = m.getItemAt(r);
                        return sig.isRemote();
                    }
                    return false;
                }
            }

            class AlienSuffixPredicate implements HighlightPredicate {

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
            col.setCellEditor(new CellEditor());
            col.setCellRenderer(new DefaultTableRenderer((java.lang.Object value) -> ((Signee) value).toString()));
            col.addHighlighter(new ColorHighlighter(new PredicateExisting(), null, Color.BLUE));
            col.addHighlighter(new ColorHighlighter(new AlienSuffixPredicate(), null, Color.RED));
        }

    }

    final static class NameColumn extends UntisSigneesDefaultColumns implements HighlightPredicate {

        private NameColumn(String product) {
            super("name", 300, true, 300, product);
        }

        @Override
        public Object getColumnValue(UntisImportSigneeItem il) {
            return il.getName();
        }

        @Override
        public boolean setColumnValue(UntisImportSigneeItem il, Object value) {
            il.setUserName((String) value);
            return false;
        }

        @Override
        public void configureTableColumn(UntisSigneeImportTableModel model, TableColumnExt col) {
            super.configureTableColumn(model, col);
            class UpdatedNameHighlighter extends FontHighlighter {

                @Override
                protected boolean canHighlight(Component component, ComponentAdapter adapter) {
                    if (getFont() == null) {
                        setFont(adapter.getComponent().getFont().deriveFont(Font.BOLD));
                    }
                    return super.canHighlight(component, adapter); 
                }

            }
            FontHighlighter hl = new UpdatedNameHighlighter();
            hl.setHighlightPredicate(this);
            col.addHighlighter(hl);
        }

        @Override
        public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
            UntisSigneeImportTableModel m = (UntisSigneeImportTableModel) ((JTable) adapter.getComponent()).getModel();
            int r = adapter.convertRowIndexToModel(adapter.row);
            if (r < m.getRowCount()) {
                UntisImportSigneeItem i = m.getItemAt(r);
                return i.isUserName();
            }
            return false;
        }
    }

    static class StatusColumn extends DefaultMarkerColumn<UntisImportSigneeItem, UntisImportConfiguration, UntisImportData, UntisSigneeImportTableModel> implements HighlightPredicate {

        private final MarkerConvention[] conventions;

        StatusColumn() {
            super("status", 400, 150);
            conventions = new MarkerConvention[]{MarkerFactory.findConvention(SigneeStatus.NAME)};
        }

        @Override
        protected MarkerConvention[] getMarkerConventions(UntisImportConfiguration configuration) {
            return conventions;
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(UntisSigneesDefaultColumns.class, "UntisSigneesDefaultColumns.columnName.status");
        }

        @Override
        public Object getColumnValue(UntisImportSigneeItem il) {
            return il.getStatus();
        }

        @Override
        public boolean setColumnValue(UntisImportSigneeItem il, Object value) {
            il.setStatus((Marker) value);
            return false;
        }

        @Override
        public void configureTableColumn(UntisSigneeImportTableModel model, TableColumnExt col) {
            super.configureTableColumn(model, col);
            Highlighter hl = new FontHighlighter(this, box.getFont().deriveFont(Font.BOLD));
            col.addHighlighter(hl);
        }

        @Override
        public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
            UntisSigneeImportTableModel m = (UntisSigneeImportTableModel) ((JTable) adapter.getComponent()).getModel();
            int r = adapter.convertRowIndexToModel(adapter.row);
            if (r < m.getRowCount()) {
                UntisImportSigneeItem i = m.getItemAt(r);
//                return !i.getClearType().equals(i.getProposedClearType());
            }
            return false;
        }

    }

    static class SigneeFormatter extends JFormattedTextField.AbstractFormatter implements DocumentListener {

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
