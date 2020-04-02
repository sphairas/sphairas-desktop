/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport.ui;

import java.awt.Component;
import java.util.Optional;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.Highlighter;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.openide.windows.TopComponent;
import org.thespheres.betula.termreport.AssessmentProvider;
import org.thespheres.betula.ui.swingx.HighlighterInstanceFactory;

/**
 *
 * @author boris.heithecker
 */
class SelectedNodeColorHighlighter extends TermReportCurrentNodeHighlighter {

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    private SelectedNodeColorHighlighter(JXTable table, TopComponent tc) {
        super(table, tc, true);
        this.setForeground(table.getSelectionForeground());
        this.setBackground(table.getSelectionBackground());
    }

    @Override
    public boolean isHighlighted(Component cmpnt, ComponentAdapter ca) {
        final Optional<AssessmentProvider> cap = findAssessmentProvider(ca);
        synchronized (this) {
            return cap.map(ap -> current.contains(ap)).orElse(false);
        }
    }

    @MimeRegistration(mimeType = "text/term-report-file+xml", service = HighlighterInstanceFactory.class)
    public static class Factory implements HighlighterInstanceFactory {

        @Override
        public Highlighter createHighlighter(JXTable table, TopComponent tc) {
            return new SelectedNodeColorHighlighter(table, tc);
        }
    }
}
