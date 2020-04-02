/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.reports.module;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.StyledDocument;
import org.openide.util.Lookup;
import org.thespheres.betula.reports.model.EditableReportCollection;
import org.thespheres.betula.reports.model.Report;

/**
 *
 * @author boris.heithecker
 */
class DefaultEditableReportCollection extends EditableReportCollection<DefaultEditableReport> {

    final ArrayList<DefaultEditableReport> reports = new ArrayList<>();

    protected DefaultEditableReportCollection(StyledDocument document, Lookup context) {
        super(context, document);
    }

    DefaultEditableReport addReport(Report r) {
        DefaultEditableReport ret = new DefaultEditableReport(this, r);
        reports.add(ret);
        return ret;
    }

    @Override
    public List<DefaultEditableReport> getReports() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
