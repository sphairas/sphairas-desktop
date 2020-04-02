/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis.ui.impl;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.NbBundle;
import org.thespheres.betula.document.MarkerConvention;
import org.thespheres.betula.gpuntis.ImportedLesson;
import org.thespheres.betula.gpuntis.UntisImportConfiguration;
import org.thespheres.betula.gpuntis.UntisImportData;
import org.thespheres.betula.gpuntis.ui.UntisCreateDocumentsTableModel;
import org.thespheres.betula.xmlimport.uiutil.DefaultColumns.DefaultSubjectColumn;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"UntisDefaultColumns.product=Untis",
    "UntisDefaultColumns.columnName.targetId=Listenkennung",
    "UntisDefaultColumns.columnName.submitTo=Update",
    "UntisDefaultColumns.columnName.subject=Fach",
    "UntisDefaultColumns.columnName.submitTo.target=Kurs",
    "UntisDefaultColumns.columnName.submitTo.timetable=Stundenplan",
    "UntisDefaultColumns.columnName.submitTo.targettimetable=Kurs & Stundenplan",
    "UntisDefaultColumns.columnName.submitTo.none=Nichts"})
public abstract class UntisDefaultColumns extends ImportTableColumn<ImportedLesson, UntisImportConfiguration, UntisImportData, UntisCreateDocumentsTableModel> {

    private final String product;

    UntisDefaultColumns(String id, int position, boolean editable, int width, String product) {
        super(id, position, editable, width);
        this.product = product;
    }

    public static Set<ImportTableColumn> create(String product) {
        HashSet<ImportTableColumn> ret = new HashSet<>();
        ret.add(new UntisSubjectColumn());
        ret.add(new TargetIdColumn(product));
        ret.add(new SubmitToColumn(product));
        return ret;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(UntisDefaultColumns.class, "UntisDefaultColumns.columnName." + columnId(), product);
    }

    final static class UntisSubjectColumn extends DefaultSubjectColumn<ImportedLesson, UntisImportConfiguration, UntisImportData, UntisCreateDocumentsTableModel> {

        UntisSubjectColumn() {
            super(200, 125);
        }

        @Override
        protected MarkerConvention[] getMarkerConventions(UntisImportConfiguration configuration) {
            return configuration.getSubjectMarkerConventions();
        }
    }

    final static class TargetIdColumn extends UntisDefaultColumns {

        private TargetIdColumn(String product) {
            super("targetId", 250, true, 80, product);
        }

        @Override
        public Object getColumnValue(ImportedLesson il) {
            return il.getCustomDocumentIdIdentifier();
        }

        @Override
        public boolean setColumnValue(ImportedLesson il, Object value) {
            il.setCustomDocumentIdIdentifier((String) value);
            return true;
        }

        @Override
        public void configureTableColumn(UntisCreateDocumentsTableModel model, TableColumnExt col) {
            JTextField tfield = new JTextField();
            tfield.setBorder(new LineBorder(Color.black, 2));
            col.setCellEditor(new DefaultCellEditor(tfield));
        }

    }

    final static class SubmitToColumn extends UntisDefaultColumns {

        private final JXComboBox submitToBox;
        private final StringValue submitToStringValue = (value) -> NbBundle.getMessage(UntisDefaultColumns.class, "UntisDefaultColumns.columnName.submitTo." + (String) value);

        private SubmitToColumn(String product) {
            super("submitTo", 850, true, 100, product);
            submitToBox = new JXComboBox();
            submitToBox.setRenderer(new DefaultListRenderer(submitToStringValue));
            submitToBox.setEditable(false);
        }

        @Override
        public void initialize(UntisImportConfiguration configuration, UntisImportData wiz) {
            DefaultComboBoxModel scbm = new DefaultComboBoxModel(ImportedLesson.SUBMITS);
            submitToBox.setModel(scbm);
        }

        @Override
        public Object getColumnValue(ImportedLesson il) {
            return il.getSubmit();
        }

        @Override
        public boolean setColumnValue(ImportedLesson il, Object value) {
            il.setSubmit((String) value);
            return false;
        }

        @Override
        public void configureTableColumn(UntisCreateDocumentsTableModel model, TableColumnExt col) {
            col.setCellEditor(new DefaultCellEditor(submitToBox));
            col.setCellRenderer(new DefaultTableRenderer(submitToStringValue));
        }

    }
}
