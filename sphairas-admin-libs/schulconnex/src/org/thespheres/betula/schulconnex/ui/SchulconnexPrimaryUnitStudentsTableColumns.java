/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.schulconnex.ui;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import org.openide.util.NbBundle;
import org.thespheres.betula.schulconnex.Schulconnex;
import org.thespheres.betula.schulconnex.SchulconnexImportConfiguration;
import org.thespheres.betula.schulconnex.SchulconnexImportData;
import org.thespheres.betula.schulconnex.SchulconnexKlasseItem;
import org.thespheres.betula.schulconnex.SchulconnexStudentItem;
import org.thespheres.betula.schulconnex.SchulconnexUtil;
import org.thespheres.betula.xmlimport.uiutil.DefaultColumns;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;

/**
 * Default columns for Schulconnex student selection table model. Adapted from
 * PrimaryUnitDefaultColumns in table-import module.
 *
 * @author boris.heithecker
 */
public abstract class SchulconnexPrimaryUnitStudentsTableColumns {

    static Set<ImportTableColumn> create() {
        final String product = Schulconnex.getProduct().getDisplay();
        final Set<ImportTableColumn> ret = new LinkedHashSet<>();
//        ret.add(new DefaultColumns.NodeColumn(product));
        ret.add(new SelectedColumn());
        ret.add(new PersonColumn(product));
        ret.add(new BirthDateColumn(product));
        ret.add(new BirthPlaceColumn(product));
        ret.add(new SchulconnexUuidColumn(product));
        return ret;
    }

    public static final class SelectedColumn extends DefaultColumns.DefaultCheckBoxColumn<SchulconnexStudentItem, SchulconnexImportConfiguration, SchulconnexImportData<SchulconnexKlasseItem>, SchulconnexPrimaryUnitStudentsTableModel> {

        SelectedColumn() {
            super("selected", 50);
        }

        @Override
        protected void initialize(SchulconnexStudentItem il) {
        }

        @Override
        public Boolean getColumnValue(SchulconnexStudentItem il) {
            return il.isSelected();
        }

        @Override
        public boolean setColumnValue(SchulconnexStudentItem il, Object value) {
            boolean before = il.isSelected();
            il.setSelected((boolean) value);
            return before = !((boolean) value);
        }

        @Override
        public String getDisplayName() {
            return "";
        }
    }

    @NbBundle.Messages({
        "SchulconnexPrimaryUnitStudentsTableColumns.PersonColumn.name=Schüler/-in"
    })
    public static final class PersonColumn extends ImportTableColumn<SchulconnexStudentItem, ConfigurableImportTarget, SchulconnexImportData<SchulconnexKlasseItem>, SchulconnexPrimaryUnitStudentsTableModel> {

        PersonColumn(String product) {
            super("person", 200, false, 300);
        }

        @Override
        protected void initialize(SchulconnexStudentItem il) {
        }

        @Override
        public String getColumnValue(SchulconnexStudentItem il) {
            return SchulconnexUtil.createSortableName(il.getPerson().getName());
        }

        @Override
        public boolean setColumnValue(SchulconnexStudentItem il, Object value) {
            return false; // Read-only
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(SchulconnexPrimaryUnitStudentsTableColumns.class, "SchulconnexPrimaryUnitStudentsTableColumns.PersonColumn.name");
        }
    }

    @NbBundle.Messages({
        "SchulconnexPrimaryUnitStudentsTableColumns.BirthDateColumn.name=Geburtsdatum"
    })
    public static final class BirthDateColumn extends ImportTableColumn<SchulconnexStudentItem, ConfigurableImportTarget, SchulconnexImportData<SchulconnexKlasseItem>, SchulconnexPrimaryUnitStudentsTableModel> {

        BirthDateColumn(String product) {
            super("birth-date", 300, false, 140);
        }

        @Override
        protected void initialize(SchulconnexStudentItem il) {
        }

        @Override
        public String getColumnValue(SchulconnexStudentItem il) {
            final LocalDate date = il.getPerson().getGeburt() != null
                    ? il.getPerson().getGeburt().getDatum() : null;
            return date != null ? date.toString() : null;
        }

        @Override
        public boolean setColumnValue(SchulconnexStudentItem il, Object value) {
            return false; // Read-only
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(SchulconnexPrimaryUnitStudentsTableColumns.class, "SchulconnexPrimaryUnitStudentsTableColumns.BirthDateColumn.name");
        }
    }

    @NbBundle.Messages({
        "SchulconnexPrimaryUnitStudentsTableColumns.BirthPlaceColumn.name=Geburtsort"
    })
    public static final class BirthPlaceColumn extends ImportTableColumn<SchulconnexStudentItem, ConfigurableImportTarget, SchulconnexImportData<SchulconnexKlasseItem>, SchulconnexPrimaryUnitStudentsTableModel> {

        BirthPlaceColumn(String product) {
            super("birth-place", 400, false, 240);
        }

        @Override
        protected void initialize(SchulconnexStudentItem il) {
        }

        @Override
        public String getColumnValue(SchulconnexStudentItem il) {
            return il.getPerson().getGeburt() != null
                    ? il.getPerson().getGeburt().getGeburtsort() : null;
        }

        @Override
        public boolean setColumnValue(SchulconnexStudentItem il, Object value) {
            return false; // Read-only
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(SchulconnexPrimaryUnitStudentsTableColumns.class, "SchulconnexPrimaryUnitStudentsTableColumns.BirthPlaceColumn.name");
        }
    }

    @NbBundle.Messages({
        "SchulconnexPrimaryUnitStudentsTableColumns.SchulconnexUuidColumn.name=Schulconnex-UUID"
    })
    public static final class SchulconnexUuidColumn extends ImportTableColumn<SchulconnexStudentItem, ConfigurableImportTarget, SchulconnexImportData<SchulconnexKlasseItem>, SchulconnexPrimaryUnitStudentsTableModel> {

        SchulconnexUuidColumn(String product) {
            super("schulconnex-uuid", 500, false, 360);
        }

        @Override
        protected void initialize(SchulconnexStudentItem il) {
        }

        @Override
        public String getColumnValue(SchulconnexStudentItem il) {
            return il.getUUID();
        }

        @Override
        public boolean setColumnValue(SchulconnexStudentItem il, Object value) {
            return false; // Read-only
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(SchulconnexPrimaryUnitStudentsTableColumns.class, "SchulconnexPrimaryUnitStudentsTableColumns.SchulconnexUuidColumn.name");
        }
    }
}
