/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.table;

import java.awt.Color;
import java.awt.Component;
import org.apache.commons.lang3.StringUtils;
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
class ExpiringColorHighlighter extends CalendarColorHighlighter implements HighlightPredicate {
    
    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    private ExpiringColorHighlighter() {
        super(new String[]{EditableRecord.PROP_TEXT});
        this.setBackground(new Color(233, 239, 248));
        this.setSelectedBackground(new Color(128, 173, 127));
        this.setHighlightPredicate(this);
    }
    
    @Override
    public boolean isHighlighted(Component comp, ComponentAdapter ca) {
        JournalEditor editor = getEditor(ca);
        if (editor != null) {
            final int index = ca.convertRowIndexToModel(ca.row);
            EditableRecord er = editor.getEditableJournal().getEditableRecords().get(index);
            return isExpiring(er);
        }
        return false;
    }
    
    public static boolean isExpiring(EditableRecord er) {
        return er.isExpiring() && StringUtils.isBlank(er.getListingText());
    }
    
    @MimeRegistration(mimeType = "text/betula-journal-file+xml", service = HighlighterInstanceFactory.class)
    public static class Factory extends AbstractHighlighterFactory {
        
        public Factory() {
            super(null); //(RecordsTableModel.ID);
        }
        
        @Override
        protected Highlighter doCreateHighlighter(JXTable table, TopComponent tc) {
            return new ExpiringColorHighlighter();
        }
    }
}
