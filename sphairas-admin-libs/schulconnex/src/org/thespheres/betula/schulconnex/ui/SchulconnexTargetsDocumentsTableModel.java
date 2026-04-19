/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.schulconnex.ui;

import java.awt.EventQueue;
import java.util.HashSet;
import java.util.Set;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.document.MarkerConvention;
import org.thespheres.betula.schulconnex.Schulconnex;
import org.thespheres.betula.schulconnex.SchulconnexImportConfiguration;
import org.thespheres.betula.schulconnex.SchulconnexImportData;
import org.thespheres.betula.schulconnex.SchulconnexKursItem;
import org.thespheres.betula.util.ChangeSet;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import org.thespheres.betula.xmlimport.uiutil.DefaultColumns;
import org.thespheres.betula.xmlimport.uiutil.DocumentBaseColumn;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn;
import org.thespheres.betula.xmlimport.uiutil.ImportTableModel;
import org.thespheres.betula.xmlimport.uiutil.UnitColumn;

final class SchulconnexTargetsDocumentsTableModel extends ImportTableModel<SchulconnexKursItem, SchulconnexImportData<SchulconnexKursItem>> {

    private final RequestProcessor rp = new RequestProcessor(SchulconnexTargetsDocumentsTableModel.class);

    SchulconnexTargetsDocumentsTableModel() {
        super(createColumns());
    }

    private static Set<ImportTableColumn> createColumns() {
        final String product = Schulconnex.getProduct().getDisplay();
        final Set<ImportTableColumn> ret = new HashSet<>();
        ret.add(new SelectedColumn());
        ret.add(new DefaultColumns.NodeColumn(product));
        ret.add(new SchulconnexKursTypColumn());
        ret.add(new UnitColumn(product));
        ret.add(new DefaultColumns.UnitDisplayColumn(product));
        ret.add(new SchulconnexSubjectColumn());
        ret.add(new DocumentBaseColumn(product));
        ret.add(new DefaultColumns.SigneeColumn(product));
        ret.add(new SchulconnexConventionColumn());
        ret.add(new DefaultColumns.DeleteDateColumn());
        return ret;
    }

    @Override
    public void initialize(final SchulconnexImportData<SchulconnexKursItem> wiz) {
        @SuppressWarnings("unchecked")
        final ChangeSet<SchulconnexKursItem> s = (ChangeSet<SchulconnexKursItem>) wiz.getProperty(AbstractFileImportAction.SELECTED_NODES);
        final RequestProcessor.Task t = rp.create(() -> {
            EventQueue.invokeLater(() -> {
                selected.clear();
                s.stream()
                        .forEach(selected::add);
                fireTableDataChanged();
            });
        });
        s.addChangeListener(e -> t.schedule(1000));
        t.schedule(1000);
    }

    private static final class SelectedColumn extends DefaultColumns.DefaultCheckBoxColumn<SchulconnexKursItem, SchulconnexImportConfiguration, SchulconnexImportData<SchulconnexKursItem>, SchulconnexTargetsDocumentsTableModel> {

        private SelectedColumn() {
            super("selected", 50);
        }

        @Override
        public Boolean getColumnValue(final SchulconnexKursItem item) {
            return item.isSelected();
        }

        @Override
        public boolean setColumnValue(final SchulconnexKursItem item, final Object value) {
            boolean before = item.isSelected();
            item.setSelected((boolean) value);
            return before = !((boolean) value);
        }

        @Override
        public String getDisplayName() {
            return "";
        }
    }

    final static class SchulconnexSubjectColumn extends DefaultColumns.DefaultSubjectColumn<SchulconnexKursItem, SchulconnexImportConfiguration, SchulconnexImportData<SchulconnexKursItem>, SchulconnexTargetsDocumentsTableModel> {

        SchulconnexSubjectColumn() {
            super(200, 125);
        }

        @Override
        protected MarkerConvention[] getMarkerConventions(SchulconnexImportConfiguration configuration) {
            return configuration.getSubjectMarkerConventions();
        }
    }

    @NbBundle.Messages("SchulconnexTargetsDocumentsTableModel.SchulconnexKursTypColumn.name=Typ")
    final static class SchulconnexKursTypColumn extends DefaultColumns.DefaultEnumColumn<SchulconnexKursItem, SchulconnexImportConfiguration, SchulconnexImportData<SchulconnexKursItem>, SchulconnexTargetsDocumentsTableModel> {

        SchulconnexKursTypColumn() {
            super(SchulconnexKursItem.Typ.values(), "schulconnex-kurs-typ", 120, 120);
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(SchulconnexKursTypColumn.class, "SchulconnexTargetsDocumentsTableModel.SchulconnexKursTypColumn.name");
        }

        @Override
        public Object getColumnValue(SchulconnexKursItem il) {
            return il.getTyp();
        }
    }

    final static class SchulconnexConventionColumn extends DefaultColumns.DefaultConventionColumn<SchulconnexKursItem, SchulconnexImportConfiguration, SchulconnexImportData<SchulconnexKursItem>, SchulconnexTargetsDocumentsTableModel> {

        SchulconnexConventionColumn() {
            super("convention", 750, 175);
        }

        @Override
        public AssessmentConvention getColumnValue(SchulconnexKursItem il) {
            return il.getAssessmentConvention();
        }

        @Override
        public boolean setColumnValue(SchulconnexKursItem il, Object value) {
            il.setAssessmentConvention((AssessmentConvention) value);
            return false;
        }

    }
}
