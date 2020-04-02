/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.highlight;

import java.awt.Component;
import java.util.Optional;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.Highlighter;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.openide.windows.TopComponent;
import org.thespheres.betula.classtest.model.EditableProblem;
import org.thespheres.betula.ui.swingx.HighlighterInstanceFactory;

/**
 *
 * @author boris.heithecker
 */
class SelectedNodeColorHighlighter extends ClasstestCurrentNodeHighlighter {

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    private SelectedNodeColorHighlighter(JXTable table, TopComponent tc) {
        super(table, tc, true);
        this.setForeground(table.getSelectionForeground());
        this.setBackground(table.getSelectionBackground());
    }

    @Override
    public boolean isHighlighted(Component cmpnt, ComponentAdapter ca) {
        final Optional<EditableProblem> cap = findAssessmentProvider(ca);
        synchronized (this) {
            return cap.map(ap -> current.contains(ap))
                    .orElse(false);
        }
    }

    @MimeRegistration(mimeType = "text/betula-classtest-file+xml", service = HighlighterInstanceFactory.class)
    public static class Factory implements HighlighterInstanceFactory {

        @Override
        public Highlighter createHighlighter(JXTable table, TopComponent tc) {
            return new SelectedNodeColorHighlighter(table, tc);
        }
    }
}
