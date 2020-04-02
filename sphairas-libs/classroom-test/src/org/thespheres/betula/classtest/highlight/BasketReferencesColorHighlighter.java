/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.highlight;

import java.awt.Color;
import java.awt.Component;
import java.util.Optional;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.Highlighter;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.openide.windows.TopComponent;
import org.thespheres.betula.classtest.model.EditableBasket;
import org.thespheres.betula.classtest.model.EditableProblem;
import org.thespheres.betula.ui.swingx.HighlighterInstanceFactory;

/**
 *
 * @author boris.heithecker
 */
class BasketReferencesColorHighlighter extends ClasstestCurrentNodeHighlighter {

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    private BasketReferencesColorHighlighter(JXTable table, TopComponent tc) {
        super(table, tc, true);
        final Color bg = table.getSelectionBackground();
        float[] hsb = Color.RGBtoHSB(bg.getRed(), bg.getGreen(), bg.getBlue(), null);
        Color lowsat = Color.getHSBColor(hsb[0], hsb[1] * 0.4f, hsb[2] * 1.6f);
        this.setBackground(lowsat);
    }

    @Override
    public boolean isHighlighted(Component cmpnt, ComponentAdapter ca) {
        final Optional<EditableProblem> cap = findAssessmentProvider(ca);
        synchronized (this) {
            EditableProblem p;
            if (current.size() == 1 && (p = current.get(0)).isBasket()) {
//                List<EditableProblem> l = ((EditableBasket) p).getReferenced().stream()
//                        .map(ProviderReference::getReferenced)
//                        .collect(Collectors.toList());
                return cap.map(ap -> ((EditableBasket) p).getReferenced().contains(ap))
                        .orElse(false);
            }
        }
        return false;
    }

    @MimeRegistration(mimeType = "text/betula-classtest-file+xml", service = HighlighterInstanceFactory.class)
    public static class Factory implements HighlighterInstanceFactory {

        @Override
        public Highlighter createHighlighter(JXTable table, TopComponent tc) {
            return new BasketReferencesColorHighlighter(table, tc);
        }
    }
}
