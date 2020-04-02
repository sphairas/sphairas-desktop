/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.table;

import java.awt.Component;
import java.awt.Font;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.openide.windows.TopComponent;
import org.thespheres.betula.journal.model.EditableRecord;
import org.thespheres.betula.journal.model.JournalEditor;
import org.thespheres.betula.ui.swingx.AbstractHighlighterFactory;
import org.thespheres.betula.ui.swingx.HighlighterInstanceFactory;

/**
 *
 * @author boris.heithecker
 */
class NoTemplateHighlighter extends CalendarFontHighlighter implements HighlightPredicate {

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    private NoTemplateHighlighter() {
        super(Font.BOLD);
        setHighlightPredicate(this);
    }

    @Override
    public boolean isHighlighted(Component cmpnt, ComponentAdapter ca) {
        JournalEditor e = getEditor(ca);
        if (e != null) {
            int line = ca.row;
            EditableRecord r = e.getEditableJournal().getEditableRecords().get(line);
            return !r.isTemplate();
        }
        return false;
    }

    @MimeRegistration(mimeType = "text/betula-journal-file+xml", service = HighlighterInstanceFactory.class)
    public static class Factory extends AbstractHighlighterFactory {

        public Factory() {
            super(null); //RecordsTableModel.ID);
        }

        @Override
        protected Highlighter doCreateHighlighter(JXTable table, TopComponent tc) {
            return new NoTemplateHighlighter();
        }
    }
}
