/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport.module;

import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.Lookup;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.termreport.AssessmentProvider;
import org.thespheres.betula.termreport.TermReport;
import org.thespheres.betula.ui.util.AbstractPluggableTableModel;
import org.thespheres.betula.ui.util.PluggableTableColumn;

/**
 *
 * @author boris.heithecker
 */
class AssessmentProviderColumn extends PluggableTableColumn.IndexedColumn<TermReport, StudentId> {

    AssessmentProviderColumn() {
        super("providers", 2000, true, 40);
    }

    @Override
    public void initialize(TermReport ecal, Lookup context) {
        super.initialize(ecal, context);
    }

    @Override
    public String getDisplayName(int index) {
        return getAssessmentProvider(index).getDisplayName();
    }

    @Override
    public int getColumnsSize() {
        return initialized ? model.getProviders().size() : 0;
    }

    @Override
    public Object getColumnValue(StudentId il, int index) {
        return getAssessmentProvider(index).select(il);
    }

    @Override
    public boolean isCellEditable(StudentId il, int index) {
        return getAssessmentProvider(index).isEditable();
    }

    @Override
    public boolean setColumnValue(StudentId il, int index, final Object value) {
        final AssessmentProvider prov = getAssessmentProvider(index);
        prov.submit(il, value, Timestamp.now());
        return false;
    }

    @Override
    public void configureTableColumn(AbstractPluggableTableModel<TermReport, StudentId, ?, ?> tm, TableColumnExt col) {
        super.configureTableColumn(tm, col);
        final int index = (int) col.getClientProperty(AbstractPluggableTableModel.PROP_COLUMNS_INDEX);
        final AssessmentProvider prov = getAssessmentProvider(index);
        prov.getTableColumnConfiguration().configureTableColumn(tm, col);
    }

    AssessmentProvider getAssessmentProvider(int modelIndex) {
        return model.getProviders().get(modelIndex);
    }

}
