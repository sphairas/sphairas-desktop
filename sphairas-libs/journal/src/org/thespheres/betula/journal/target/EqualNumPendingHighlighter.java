/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.target;

import com.google.common.eventbus.Subscribe;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.beans.IndexedPropertyChangeEvent;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.table.TableColumnExt;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.openide.windows.TopComponent;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.journal.model.EditableJournal;
import org.thespheres.betula.journal.model.EditableRecord;
import org.thespheres.betula.journal.JournalConfiguration;
import org.thespheres.betula.ui.swingx.AbstractHighlighterFactory;
import org.thespheres.betula.ui.swingx.HighlighterInstanceFactory;

/**
 *
 * @author boris.heithecker
 */
class EqualNumPendingHighlighter extends ColorHighlighter implements HighlightPredicate {

    private final JXTable table;
    private int colIndex = -1;
    private Boolean state;

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    private EqualNumPendingHighlighter(JXTable table) {
        super(Color.PINK, null, Color.RED, null);
        setHighlightPredicate(this);
        this.table = table;
    }

    @Override
    public boolean isHighlighted(Component cmpnt, ComponentAdapter ca) {
        final TableColumnExt col = table.getColumnExt(ca.column);
        final Grade g = (Grade) col.getClientProperty(TargetAssessmentTableModel.CLIENT_PROP_GRADE);
        if (g != null && JournalConfiguration.getInstance().getJournalUndefinedGrade().equals(g)) {
            final int cr = ca.convertColumnIndexToModel(ca.column);
            if (colIndex == -1) {
                final TargetAssessmentTableModel m = (TargetAssessmentTableModel) table.getModel();
                final EditableJournal ej;
                if (m != null && (ej = m.getItemsModel()) != null) {
                    colIndex = cr;
                    ej.getEventBus().register(this);
                }
            }
            return getState();
        }
        return false;
    }

    public boolean getState() {
        if (state == null) {
            state = evaluateState();
        }
        return state;
    }

    private boolean evaluateState() {
        Object v = null;
        if (colIndex != -1) {
            for (int i = 0; i < table.getModel().getRowCount();) {
                final Object va = table.getModel().getValueAt(i++, colIndex);
                if (v != null && !v.equals(va)) {
                    return true;
                }
                v = va;
            }
        }
        return false;
    }

    @Subscribe
    public void onGradeChange(IndexedPropertyChangeEvent ipce) {
        if (ipce.getPropertyName().equals(EditableRecord.PROP_GRADE)) {
            EventQueue.invokeLater(() -> {
                state = null;
                EqualNumPendingHighlighter.this.fireStateChanged();
            });
        }
    }

    @MimeRegistration(mimeType = "text/betula-journal-file-target-table", service = HighlighterInstanceFactory.class, position = 10000)
    public static class Factory extends AbstractHighlighterFactory {

        public Factory() {
            super(null); //RecordsTableModel.ID);
        }

        @Override
        protected Highlighter doCreateHighlighter(JXTable table, TopComponent tc) {
            return new EqualNumPendingHighlighter(table);
        }
    }
}
