/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.schulconnex.ui;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbBundle;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.document.AbstractMarker;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerConvention;
import org.thespheres.betula.schulconnex.Schulconnex;
import org.thespheres.betula.schulconnex.SchulconnexImportConfiguration;
import org.thespheres.betula.schulconnex.SchulconnexImportData;
import org.thespheres.betula.schulconnex.SchulconnexKursItem;
import org.thespheres.betula.xmlimport.uiutil.DefaultColumns;
import org.thespheres.betula.xmlimport.uiutil.DocumentBaseColumn;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn;
import org.thespheres.betula.xmlimport.uiutil.MultiSubjectColumn;
import org.thespheres.betula.xmlimport.uiutil.UnitColumn;

/**
 * Default columns for Schulconnex target items (courses) import.
 *
 * @author boris.heithecker
 */
class SchulconnexTargetsDocumentsTableColumns {

    static Set<ImportTableColumn> create() {
        final String product = Schulconnex.getProduct().getDisplay();
        final Set<ImportTableColumn> ret = new HashSet<>();
        ret.add(new SelectedColumn());
        ret.add(new DefaultColumns.NodeColumn(product));
        ret.add(new SchulconnexNumParticipantsColumn());
        ret.add(new SchulconnexKursTypColumn());
        ret.add(new UnitColumn(product));
        ret.add(new DefaultColumns.UnitDisplayColumn(product));
        ret.add(new SchulconnexSubjectColumn());
        ret.add(new SchulconnexKursartColumn());
        ret.add(new DocumentBaseColumn(product));
        ret.add(new DefaultColumns.SigneeColumn(product));
        ret.add(new SchulconnexConventionColumn());
        ret.add(new DefaultColumns.DeleteDateColumn());
        return ret;
    }

    static final class SelectedColumn extends DefaultColumns.DefaultCheckBoxColumn<SchulconnexKursItem, SchulconnexImportConfiguration, SchulconnexImportData<SchulconnexKursItem>, SchulconnexTargetsDocumentsTableModel> {

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

    static final class SchulconnexSubjectColumn extends MultiSubjectColumn<SchulconnexKursItem, SchulconnexImportConfiguration, SchulconnexImportData<SchulconnexKursItem>, SchulconnexTargetsDocumentsTableModel> {

        protected boolean permitAltSubjectNames;

        SchulconnexSubjectColumn() {
            super(200, 125);
            this.prependNull = true;
        }

        @Override
        public void initialize(final SchulconnexImportConfiguration config, final SchulconnexImportData<SchulconnexKursItem> wizard) {
            super.initialize(config, wizard);
            permitAltSubjectNames = config.permitAltSubjectNames();
            updateEditableSubjectEntry(this.permitAltSubjectNames);
        }

        @Override
        public Marker getColumnValue(final SchulconnexKursItem il) {
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
        public boolean setColumnValue(final SchulconnexKursItem il, final Object value) {
            if (!(value instanceof Marker) && this.permitAltSubjectNames) {
                final String v = (String) value;
                final String n = StringUtils.trimToNull(v);
                il.setSubjectAlternativeName(n);
                il.setSubjectMarker(new Marker[0]);
                return false;
            } else {
                return super.setColumnValue(il, value);
            }
        }

        @Override
        protected MarkerConvention[] getMarkerConventions(SchulconnexImportConfiguration configuration) {
            return configuration.getSubjectMarkerConventions();
        }

    }

    @NbBundle.Messages("SchulconnexTargetsDocumentsTableColumns.SchulconnexKursartColumn.name=Kursart")
    static final class SchulconnexKursartColumn extends DefaultColumns.DefaultMarkerColumn<SchulconnexKursItem, SchulconnexImportConfiguration, SchulconnexImportData<SchulconnexKursItem>, SchulconnexTargetsDocumentsTableModel> {

        SchulconnexKursartColumn() {
            super("kursart", 250, 125);
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(SchulconnexTargetsDocumentsTableColumns.class, "SchulconnexTargetsDocumentsTableColumns.SchulconnexKursartColumn.name");
        }

        @Override
        protected MarkerConvention[] getMarkerConventions(SchulconnexImportConfiguration configuration) {
            return configuration.getRealmMarkerConventions();
        }

        @Override
        public Marker getColumnValue(SchulconnexKursItem il) {
            return il.getRealm();
        }

        @Override
        public boolean setColumnValue(SchulconnexKursItem il, Object value) {
            il.getUniqueMarkerSet().add((Marker) value);
            return false;
        }

    }

    @NbBundle.Messages("SchulconnexTargetsDocumentsTableColumns.SchulconnexNumParticipantsColumn.name=Anz.")
    static final class SchulconnexNumParticipantsColumn extends DefaultColumns<SchulconnexKursItem, SchulconnexImportConfiguration, SchulconnexImportData<SchulconnexKursItem>, SchulconnexTargetsDocumentsTableModel> {

        public SchulconnexNumParticipantsColumn() {
            super("schulconnex-kurs-num-participants", 110, false, 35, null);
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(SchulconnexTargetsDocumentsTableColumns.class, "SchulconnexTargetsDocumentsTableColumns.SchulconnexNumParticipantsColumn.name");
        }

        @Override
        public Object getColumnValue(SchulconnexKursItem il) {
            return il.getUnitStudentsSize();
        }
    }

    @NbBundle.Messages("SchulconnexTargetsDocumentsTableColumns.SchulconnexKursTypColumn.name=Typ")
    static final class SchulconnexKursTypColumn extends DefaultColumns.DefaultEnumColumn<SchulconnexKursItem, SchulconnexImportConfiguration, SchulconnexImportData<SchulconnexKursItem>, SchulconnexTargetsDocumentsTableModel> {

        SchulconnexKursTypColumn() {
            super(SchulconnexKursItem.Typ.values(), "schulconnex-kurs-typ", 120, false, 120);
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(SchulconnexTargetsDocumentsTableColumns.class, "SchulconnexTargetsDocumentsTableColumns.SchulconnexKursTypColumn.name");
        }

        @Override
        public Object getColumnValue(SchulconnexKursItem il) {
            return il.getTyp();
        }
    }

    static final class SchulconnexConventionColumn extends DefaultColumns.DefaultConventionColumn<SchulconnexKursItem, SchulconnexImportConfiguration, SchulconnexImportData<SchulconnexKursItem>, SchulconnexTargetsDocumentsTableModel> {

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
