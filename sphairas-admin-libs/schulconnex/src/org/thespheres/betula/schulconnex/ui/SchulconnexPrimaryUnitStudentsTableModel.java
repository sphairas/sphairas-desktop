/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.schulconnex.ui;

import java.util.List;
import java.util.Set;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.schulconnex.SchulconnexImportData;
import org.thespheres.betula.schulconnex.SchulconnexKlasseItem;
import org.thespheres.betula.schulconnex.SchulconnexStudentItem;
import org.thespheres.betula.util.ChangeSet;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn;
import org.thespheres.betula.xmlimport.uiutil.ImportTableModel;

/**
 * Table model for student/person selection in Schulconnex import. Displays list
 * of persons with selection checkbox for update. Adapted from
 * PrimaryUnitUpdateStudentsTableModel in table-import module.
 *
 * @author boris.heithecker
 */
final class SchulconnexPrimaryUnitStudentsTableModel extends ImportTableModel<SchulconnexStudentItem, SchulconnexImportData<SchulconnexKlasseItem>> {

    SchulconnexPrimaryUnitStudentsTableModel() {
        super(createColumns());
    }

    private static Set<ImportTableColumn> createColumns() {
        final Set<ImportTableColumn> s = SchulconnexPrimaryUnitStudentsTableColumns.create();
        Lookups.forPath("SchulconnexStudentSelectionTableModel/Columns").lookupAll(ImportTableColumn.Factory.class).stream()
                .map(ImportTableColumn.Factory::createInstance)
                .forEach(s::add);
        return s;
    }

    @Override
    public void initialize(SchulconnexImportData<SchulconnexKlasseItem> wiz) {
        selected.clear();
        @SuppressWarnings("unchecked")
        final ChangeSet<SchulconnexKlasseItem> s = (ChangeSet<SchulconnexKlasseItem>) wiz.getProperty(AbstractFileImportAction.SELECTED_NODES);
        s.stream()
                .filter(SchulconnexKlasseItem::isSelected)
                .flatMap(kl -> kl.getImportStudents().stream())
                //                .peek(il -> il.initialize(wiz.getConfiguration(), wiz))
                //                .peek(il -> wiz.addPropertyChangeListener(createPCL(il, wiz)))
                .forEach(selected::add);
        fireTableDataChanged();
    }

    public List<SchulconnexStudentItem> getSelectedPersons() {
        return new java.util.ArrayList<>(selected);
    }
}
