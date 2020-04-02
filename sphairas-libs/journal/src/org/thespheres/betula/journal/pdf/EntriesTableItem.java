/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.pdf;

import org.openide.util.NbBundle;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.journal.model.EditableJournal;
import org.thespheres.betula.journal.model.EditableRecord;
import org.thespheres.betula.journal.model.JournalEditor;
import org.thespheres.betula.listprint.builder.TableItem;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"EntriesTableItem.column.student=Name"})
class EntriesTableItem implements TableItem {

    private final EditableJournal<?, ?> journal;
    private final JournalEditor editor;

    EntriesTableItem(EditableJournal journal, JournalEditor editor) {
        this.journal = journal;
        this.editor = editor;
    }

    @Override
    public int getRowCount() {
        return journal.getEditableRecords().size();
    }

    @Override
    public int getColumnCount() {
        return journal.getEditableParticipants().size() + 1;
    }

    @Override
    public String getColumnName(int ci) {
        if (ci == 0) {
            return NbBundle.getMessage(EntriesTableItem.class, "EntriesTableItem.column.student");
        } else {
            final int index = ci - 1;
            return journal.getEditableParticipants().get(index).getDirectoryName();
        }
    }

    @Override
    public String getValueAt(final int rowIndex, int ci) {
        EditableRecord er = journal.getEditableRecords().get(rowIndex);
        if (ci == 0) {
            return editor.formatLocalDate(er);
        } else {
            final int index = ci - 1;
            Grade g = er.getGradeAt(index);
            return g != null ? g.getShortLabel() : "";
        }
    }

}
