/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.table;

import java.awt.Color;
import java.awt.Component;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.openide.windows.TopComponent;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.journal.JournalConfiguration;
import org.thespheres.betula.ui.swingx.AbstractHighlighterFactory;
import org.thespheres.betula.ui.swingx.HighlighterInstanceFactory;

/**
 *
 * @author boris.heithecker
 */
class PendingHighlighter extends ColorHighlighter implements HighlightPredicate {

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    private PendingHighlighter() {
        super(Color.YELLOW, null, Color.ORANGE, null);
        setHighlightPredicate(this);
    }

    @Override
    public boolean isHighlighted(Component cmpnt, ComponentAdapter ca) {
        final Grade pending = JournalConfiguration.getInstance().getJournalUndefinedGrade();
        return ca.getValue() != null && ca.getValue().equals(pending);
    }

    @MimeRegistration(mimeType = "text/betula-journal-file-entries", service = HighlighterInstanceFactory.class, position = 10000)
    public static class Factory extends AbstractHighlighterFactory {

        public Factory() {
            super(null); //RecordsTableModel.ID);
        }

        @Override
        protected Highlighter doCreateHighlighter(JXTable table, TopComponent tc) {
            return new PendingHighlighter();
        }
    }
}
