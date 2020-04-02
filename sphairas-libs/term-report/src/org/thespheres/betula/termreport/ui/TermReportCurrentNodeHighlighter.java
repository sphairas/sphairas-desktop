/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport.ui;

import org.thespheres.betula.ui.swingx.AbstractCurrentNodeHighlighter;
import java.util.Optional;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.openide.windows.TopComponent;
import org.thespheres.betula.termreport.AssessmentProvider;
import org.thespheres.betula.termreport.TermReport;

/**
 *
 * @author boris.heithecker
 */
abstract class TermReportCurrentNodeHighlighter extends AbstractCurrentNodeHighlighter<AssessmentProvider> {

    TermReportCurrentNodeHighlighter(JXTable table, TopComponent tc, boolean surviveFocusChange) {
        super(table, tc, surviveFocusChange, AssessmentProvider.class);
    }

    protected Optional<AssessmentProvider> findAssessmentProvider(ComponentAdapter ca) {
        int index = ca.convertColumnIndexToModel(ca.column) - 1;
        TermReport tr = component.getLookup().lookup(TermReport.class);
        if (tr != null && index >= 0 && index < tr.getProviders().size()) {
            return Optional.of(tr.getProviders().get(index));
        }
        return Optional.empty();
    }

}
