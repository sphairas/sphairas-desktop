/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tableimport.ui;

import java.util.Set;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.tableimport.action.XmlCsvImportSettings;
import org.thespheres.betula.tableimport.impl.AbstractXmlCsvImportItem;
import org.thespheres.betula.tableimport.impl.PrimaryUnitImportStudentItem;
import org.thespheres.betula.tableimport.impl.PrimaryUnitsXmlCsvItem;
import org.thespheres.betula.util.ChangeSet;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn;
import org.thespheres.betula.xmlimport.uiutil.ImportTableModel;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;

/**
 *
 * @author boris.heithecker
 */
class PrimaryUnitUpdateStudentsTableModel extends ImportTableModel<PrimaryUnitImportStudentItem, XmlCsvImportSettings<PrimaryUnitsXmlCsvItem>> {

//    public static final String[] STUDENTS_MARKER_PATH = new String[]{"students", "marker"};
    protected ConfigurableImportTarget configuration;

    PrimaryUnitUpdateStudentsTableModel() {
        super(createColumns());
    }

    private static Set<ImportTableColumn> createColumns() {
        final String product = NbBundle.getMessage(PrimaryUnitDefaultColumns.class, "PrimaryUnitDefaultColumns.product");
        final Set<ImportTableColumn> s = PrimaryUnitDefaultColumns.create(product);
        Lookups.forPath("PrimaryUnitUpdateStudentsTableModel/Columns").lookupAll(ImportTableColumn.Factory.class).stream()
                .map(ImportTableColumn.Factory::createInstance)
                .forEach(s::add);
        return s;
    }

    @Override
    public void initialize(XmlCsvImportSettings<PrimaryUnitsXmlCsvItem> descriptor) {
        configuration = (ConfigurableImportTarget) descriptor.getProperty(AbstractFileImportAction.IMPORT_TARGET);
        final ChangeSet<PrimaryUnitsXmlCsvItem> s = (ChangeSet<PrimaryUnitsXmlCsvItem>) descriptor.getProperty(AbstractFileImportAction.SELECTED_NODES);
        selected.clear();
        s.stream()
                .filter(AbstractXmlCsvImportItem::isSelected)
                .flatMap(ik -> ik.getImportStudents().values().stream())
                .forEach(selected::add);

        Mutex.EVENT.writeAccess(this::fireTableDataChanged);
    }
}
