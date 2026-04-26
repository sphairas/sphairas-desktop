/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.schulconnex.ui;

import java.awt.EventQueue;
import java.util.Set;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.schulconnex.SchulconnexImportData;
import org.thespheres.betula.schulconnex.SchulconnexKursItem;
import org.thespheres.betula.util.ChangeSet;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn;
import org.thespheres.betula.xmlimport.uiutil.ImportTableModel;

final class SchulconnexTargetsDocumentsTableModel extends ImportTableModel<SchulconnexKursItem, SchulconnexImportData<SchulconnexKursItem>> {

    private final RequestProcessor rp = new RequestProcessor(SchulconnexTargetsDocumentsTableModel.class);

    SchulconnexTargetsDocumentsTableModel() {
        super(createColumns());
    }

    private static Set<ImportTableColumn> createColumns() {
        return SchulconnexTargetsDocumentsTableColumns.create();
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
}
