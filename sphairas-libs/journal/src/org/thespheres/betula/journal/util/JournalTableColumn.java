/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.util;

import org.thespheres.betula.ui.util.PluggableTableColumn;
import org.thespheres.betula.journal.model.EditableJournal;
import org.thespheres.betula.journal.model.EditableRecord;

/**
 *
 * @author boris.heithecker
 */
public abstract class JournalTableColumn extends PluggableTableColumn<EditableJournal<?, ?>, EditableRecord<?>> {

    protected EditableJournal journal;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    protected JournalTableColumn(String id, int position, boolean editable, int width) {
        super(id, position, editable, width);

    }

    public static abstract class Factory extends PluggableTableColumn.Factory<JournalTableColumn> {
    }
}
