/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.uiutil;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.table.TableColumnExt;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.DocumentId.Version;
import org.thespheres.betula.ui.util.WideJXComboBox;
import org.thespheres.betula.xmlimport.ImportItem;
import org.thespheres.betula.xmlimport.ImportTarget;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import static org.thespheres.betula.xmlimport.uiutil.IdFormatter.NULL_LABEL;

/**
 *
 * @author boris.heithecker
 */
public class DocumentBaseColumn extends DefaultColumns {

    protected final WideJXComboBox box;
//    private final StringValue stringValue = v -> v instanceof ImportTargetsItem ? Optional.ofNullable(((ImportTargetsItem) v).getTargetDocumentIdBase()).map(DocumentId::getId).orElse(null) : null;
    private final StringValue docStringValue = v -> v instanceof DocumentId ? ((DocumentId) v).getId() : null;
    private final DefaultComboBoxModel model;
    private WeakReference<ImportItem> current;
    private final DocumentFormatter docIdFormatter = new DocumentFormatter();

    public DocumentBaseColumn(String product) {
        super("documentBase", 500, false, 170, product);
        box = new WideJXComboBox();
        model = new DefaultComboBoxModel();
        box.setModel(model);
        box.setRenderer(new DefaultListRenderer(docStringValue));
        box.setEditable(false);
        final JComponent ec = (JComponent) box.getEditor().getEditorComponent();
        ec.setBorder(BorderFactory.createEmptyBorder());
    }

    @Override
    public void initialize(ImportTarget configuration, ImportWizardSettings wizard) {
        docIdFormatter.initialize(configuration);
    }

    @Override
    public Object getColumnValue(final ImportItem il) {
        if (il instanceof ImportTargetsItem) {
            final ImportTargetsItem iti = (ImportTargetsItem) il;
            final boolean update = !Optional.ofNullable(current)
                    .map(WeakReference::get)
                    .map(il::equals)
                    .orElse(false);
            if (update) {
                final DocumentId[] options = iti.getTargetDocumentIdBaseOptions();
                model.removeAllElements();
                Arrays.stream(options).forEach(model::addElement);
                current = new WeakReference<>(il);
            }
            box.setEditable(iti.getUnitId() == null);
            return iti.getTargetDocumentIdBase();
        }
        return null;
    }

    @Override
    public boolean isCellEditable(ImportItem il) {
        if (il instanceof ImportTargetsItem) {
            final ImportTargetsItem iti = (ImportTargetsItem) il;
            final int options = iti.getTargetDocumentIdBaseOptions().length;
            return options > 1 || iti.getUnitId() == null;
        }
        return super.isCellEditable(il);
    }

    @Override
    public boolean setColumnValue(ImportItem il, Object value) {
        if (il instanceof ImportTargetsItem) {
            final ImportTargetsItem iti = (ImportTargetsItem) il;
            if (value instanceof DocumentId) {
                iti.setTargetDocumentIdBase((DocumentId) value);
                return true;
            }
        }
        return false;
    }

    @Override
    public void configureTableColumn(ImportTableModel m, TableColumnExt col) {
        final JFormattedTextField tfield = new JFormattedTextField(docIdFormatter);
        tfield.setBorder(BorderFactory.createEmptyBorder());

        class CellEditor extends BasicComboBoxEditor {

            @Override
            protected JTextField createEditorComponent() {
                return tfield;
            }

            @Override
            public Object getItem() {
                return tfield.getValue();
            }

            @Override
            public void setItem(Object value) {
                tfield.setValue(value);
            }

        }

        box.setEditor(new CellEditor());

        col.setCellEditor(new DefaultCellEditor(box));
        col.setCellRenderer(new DefaultTableRenderer(docStringValue));
    }

    private static class DocumentFormatter extends IdFormatter<DocumentId> {

        @Override
        public DocumentId stringToValue(String text) throws ParseException {
            if (config != null && checkUid(text)) {
                final String uid = StringUtils.trimToNull(text);
                return uid != null ? new DocumentId(config.getAuthority(), text.trim(), Version.LATEST) : null;
            } else if (NULL_LABEL.equals(StringUtils.trimToEmpty(text))) {
                return null;
            }
            throw new ParseException(text, 0);
        }

        @Override
        public String valueToString(Object value) throws ParseException {
            return Optional.ofNullable(value)
                    .filter(DocumentId.class::isInstance)
                    .map(DocumentId.class::cast)
                    .map(DocumentId::getId)
                    .orElse(NULL_LABEL);
        }

    }
}
