/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.table;

import com.google.common.eventbus.Subscribe;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.AbstractHighlighter;
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
class ReadyHighlighter extends AbstractHighlighter implements HighlightPredicate {

    private JournalEditor editor;

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    private ReadyHighlighter() {
        super();
        setHighlightPredicate(this);
    }

    protected JournalEditor getEditor(ComponentAdapter ca) {
        if (editor == null) {
            TopComponent tc = findTC(ca.getComponent());
            if (tc != null) {
                JournalEditor ed = tc.getLookup().lookup(JournalEditor.class);
                if (ed != null) {
                    this.editor = ed;
                    editor.getEditableJournal().getEventBus().register(this);
                }
            }
        }
        return editor;
    }

    private TopComponent findTC(Component comp) {
        //von ExplorerManger.find.....
        for (;;) {
            if (comp == null) {
                return null;
            }

            if (comp instanceof TopComponent) {
                return (TopComponent) comp;
            }
            comp = comp.getParent();
        }
    }

    @Override
    protected Component doHighlight(Component comp, ComponentAdapter ca) {
        int borderTopWidth = 0;
        int borderBottomWidth = 0;
        final int index = ca.convertRowIndexToModel(ca.row);
        EditableRecord er = editor.getEditableJournal().getEditableRecords().get(index);
//        if (!er.canJoinWithPreceding() && index != 0) {
//            borderTopWidth = 1;
//        }
        if (!er.canJoinWithNext() || index == editor.getEditableJournal().getEditableRecords().size() - 1) {
            borderBottomWidth = 1;
        }
        ((JComponent) comp).setBorder(BorderFactory.createMatteBorder(borderTopWidth, 0, borderBottomWidth, 0, comp.getForeground()));
        return comp;
    }

    @Override
    public boolean isHighlighted(Component comp, ComponentAdapter ca) {
        getEditor(ca);
        if (editor != null) {
            final int index = ca.convertRowIndexToModel(ca.row);
            EditableRecord er = editor.getEditableJournal().getEditableRecords().get(index);
            return !ExpiringColorHighlighter.isExpiring(er);
        }
        return false;
    }

    @Subscribe
    public void onPropertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() instanceof EditableRecord && EditableRecord.PROP_TEXT.equals(evt.getPropertyName())) {
            fireStateChanged();
        }
    }


    @MimeRegistration(mimeType = "text/betula-journal-file+xml", service = HighlighterInstanceFactory.class)
    public static class Factory extends AbstractHighlighterFactory {

        public Factory() {
            super(null); //RecordsTableModel.ID);
        }

        @Override
        protected Highlighter doCreateHighlighter(JXTable table, TopComponent tc) {
            return new ReadyHighlighter();
        }
    }
}
