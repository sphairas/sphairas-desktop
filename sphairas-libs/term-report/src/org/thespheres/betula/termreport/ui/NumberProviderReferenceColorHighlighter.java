/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport.ui;

import java.awt.Color;
import java.awt.Component;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.Highlighter;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.openide.windows.TopComponent;
import org.thespheres.betula.termreport.AssessmentProvider;
import org.thespheres.betula.termreport.NumberAssessmentProvider;
import org.thespheres.betula.termreport.NumberAssessmentProvider.ProviderReference;
import org.thespheres.betula.ui.swingx.HighlighterInstanceFactory;

/**
 *
 * @author boris.heithecker
 */
class NumberProviderReferenceColorHighlighter extends TermReportCurrentNodeHighlighter {

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    private NumberProviderReferenceColorHighlighter(JXTable table, TopComponent tc) {
        super(table, tc, true);
        final Color bg = table.getSelectionBackground();
        float[] hsb = Color.RGBtoHSB(bg.getRed(), bg.getGreen(), bg.getBlue(), null);
        Color lowsat = Color.getHSBColor(hsb[0], hsb[1] * 0.4f, hsb[2] * 1.6f);
        this.setBackground(lowsat);
    }

    @Override
    public boolean isHighlighted(Component cmpnt, ComponentAdapter ca) {
        final Optional<AssessmentProvider> cap = findAssessmentProvider(ca);
        synchronized (this) {
            AssessmentProvider p;
            if (current.size() == 1 && (p = current.get(0)) instanceof NumberAssessmentProvider) {
                List<AssessmentProvider> l = ((NumberAssessmentProvider) p).getProviderReferences().stream()
                        .map(ProviderReference::getReferenced)
                        .collect(Collectors.toList());
                return cap.map(ap -> l.contains(ap)).orElse(false);
            }
        }
        return false;
    }

    @MimeRegistration(mimeType = "text/term-report-file+xml", service = HighlighterInstanceFactory.class)
    public static class Factory implements HighlighterInstanceFactory {

        @Override
        public Highlighter createHighlighter(JXTable table, TopComponent tc) {
            return new NumberProviderReferenceColorHighlighter(table, tc);
        }
    }
}
