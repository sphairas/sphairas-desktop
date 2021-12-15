/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.sibank.ui2.impl;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.thespheres.betula.document.AbstractMarker;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerConvention;
import org.thespheres.betula.sibank.SiBankImportData;
import org.thespheres.betula.sibank.SiBankKursItem;
import org.thespheres.betula.sibank.SiBankImportTarget;
import org.thespheres.betula.sibank.ui2.SiBankCreateDocumentsTableModel;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn;
import org.thespheres.betula.xmlimport.uiutil.MultiSubjectColumn;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"SiBankDefaultColumns.product=SiBank",
    "SiBankDefaultColumns.columnName.targetId=Listenkennung",
    "SiBankDefaultColumns.columnName.subject=Fach"})
public abstract class SiBankDefaultColumns extends ImportTableColumn<SiBankKursItem, SiBankImportTarget, SiBankImportData<SiBankKursItem>, SiBankCreateDocumentsTableModel> {

    private final String product;

    SiBankDefaultColumns(String id, int position, boolean editable, int width, String product) {
        super(id, position, editable, width);
        this.product = product;
    }

    public static Set<ImportTableColumn> create(String product) {
        HashSet<ImportTableColumn> ret = new HashSet<>();
        ret.add(new SiBankSubjectColumn());
        ret.add(new TargetIdColumn(product));
        return ret;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(SiBankDefaultColumns.class, "SiBankDefaultColumns.columnName." + columnId(), product);
    }

    @NbBundle.Messages({"SiBankSubjectColumn.altSubjectName.label=Fachname eingeben"})
    final static class SiBankSubjectColumn extends MultiSubjectColumn<SiBankKursItem, SiBankImportTarget, SiBankImportData<SiBankKursItem>, SiBankCreateDocumentsTableModel> {

        final static Marker ALT_SUBJECT = new AbstractMarker("null", "ALT_SUBJECT", null) {
            @Override
            public String getLongLabel(Object... formattingArgs) {
                return NbBundle.getMessage(SiBankSubjectColumn.class, "SiBankSubjectColumn.altSubjectName.label");
            }

        };

        SiBankSubjectColumn() {
            super(200, 125);
        }

        @Override
        public void initialize(final SiBankImportTarget config, final SiBankImportData<SiBankKursItem> wizard) {
            super.initialize(config, wizard);
            final DefaultComboBoxModel<Marker> fcbm = (DefaultComboBoxModel<Marker>) box.getModel();
            if (config.permitAltSubjectNames()) {
                fcbm.addElement(ALT_SUBJECT);
            }
        }

        @Override
        public Marker getColumnValue(final SiBankKursItem il) {
            if (!StringUtils.isBlank(il.getSubjectAlternativeName())) {
                return new AbstractMarker("null", "ALTERNATIVE_SUBJECT_NAME", null) {
                    @Override
                    public String getLongLabel(Object... formattingArgs) {
                        return il.getSubjectAlternativeName();
                    }

                };
            }
            return super.getColumnValue(il);
        }

        @Override
        public boolean setColumnValue(final SiBankKursItem il, final Object value) {
            final Marker mValue = (Marker) value;
            if (mValue.equals(ALT_SUBJECT)) {
                final String n = showDialog();
                il.setSubjectAlternativeName(n);
                il.setSubjectMarker(new Marker[0]);
                return false;
            } else {
                return super.setColumnValue(il, value);
            }
        }

        @Override
        protected MarkerConvention[] getMarkerConventions(final SiBankImportTarget configuration) {
            return configuration.getSubjectMarkerConventions();
        }

        String showDialog() {
            final JTextField panel = new JTextField();
            final DialogDescriptor dd = new DialogDescriptor(
                    panel, NbBundle.getMessage(SiBankSubjectColumn.class, "SiBankSubjectColumn.altSubjectName.label"),
                    true,
                    DialogDescriptor.OK_CANCEL_OPTION,
                    DialogDescriptor.OK_OPTION,
                    null);
            final Object result = DialogDisplayer.getDefault().notify(dd); //NOI18N
            if (DialogDescriptor.OK_OPTION == result) {
                return StringUtils.trimToNull(panel.getText());
            }
            return null;
        }
    }

    final static class TargetIdColumn extends SiBankDefaultColumns {

        private TargetIdColumn(String product) {
            super("targetId", 250, true, 80, product);
        }

        @Override
        public Object getColumnValue(SiBankKursItem il) {
            return il.getCustomDocumentIdIdentifier();
        }

        @Override
        public boolean setColumnValue(SiBankKursItem il, Object value) {
            il.setCustomDocumentIdIdentifier((String) value);
            return true;
        }

        @Override
        public void configureTableColumn(SiBankCreateDocumentsTableModel model, TableColumnExt col) {
            final JTextField tfield = new JTextField();
            tfield.setBorder(new LineBorder(Color.black, 2));
            col.setCellEditor(new DefaultCellEditor(tfield));
        }

    }
}
