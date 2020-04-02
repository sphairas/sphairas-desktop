/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.model;

import com.google.common.eventbus.Subscribe;
import java.beans.PropertyChangeEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoableEditSupport;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.thespheres.betula.RecordId;
import org.thespheres.betula.journal.table.FormatUtil;
import org.thespheres.betula.journal.table.JournalTableSupport;
import org.thespheres.betula.journal.util.PeriodsList;
import org.thespheres.betula.services.scheme.spi.Period;
import org.thespheres.betula.util.CollectionChangeEvent;
import org.thespheres.betula.util.XmlStudent;

/**
 *
 * @author boris.heithecker
 */
public class JournalEditor implements Lookup.Provider {

    private final EditableJournal<?, ?> journal;
    private final Lookup lookup;
    private final JournalTableSupport env;
//    private final Listener listener = new Listener();
    private PeriodsList periodList;

    @SuppressWarnings({"LeakingThisInConstructor"})
    public JournalEditor(EditableJournal j, Lookup context, JournalTableSupport env) {
        this.journal = j;
        this.lookup = context;
        this.env = env;
        //TODO: lookup
//        initListeners();
        //
//        ecal.addCalendarListener(listener);
//        ecal.addPropertyChangeListener(listener);
        journal.getEventBus().register(this);
    }

    public EditableJournal<?, ?> getEditableJournal() {
        return journal;
    }

    @Override
    public Lookup getLookup() {
        return this.lookup;
    }

    public EditableParticipant insertStudent(XmlStudent student) {
        EditableParticipant part = journal.updateParticipant(student);
        return part;
    }

    public void removeStudent(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeRecord(int index) {
        EditableRecord er = journal.removeRecord(index);
    }

    private PeriodsList getPeriodList() {
        if (periodList == null) {
            DataObject data = lookup.lookup(DataObject.class);
            if (data != null) {
                Project prj = FileOwnerQuery.getOwner(data.getPrimaryFile());
                if (prj != null) {
                    periodList = PeriodsList.create(prj, journal);
                }
            }
        }
        return periodList;
    }

    public String formatLocalDate(EditableRecord er) {
        return this.formatLocalDate(er, true);
    }

    public String formatLocalDate(EditableRecord er, boolean joinRecords) {
        final RecordId ri = er.getRecordId();
        final boolean omitDate = joinRecords && er.canJoinWithPreceding();
        if (getPeriodList() != null) {
            Period period = getPeriodList().findForRecordId(ri);
            if (period != null) {
                return FormatUtil.formatRecordId(ri, period, omitDate);
            }
        }
        return FormatUtil.formatRecordId(ri, omitDate);
    }

    private void setDOModified(boolean mod) {
        env.getDataObject().setModified(mod);
    }

    @Subscribe
    public void onModelChange(CollectionChangeEvent event) {
        final String cn = event.getCollectionName();
        if (EditableJournal.COLLECTION_RECORDS.equals(cn)
                || EditableJournal.COLLECTION_PARTICIPANTS.equals(cn)
                || EditableJournal.COLLECTION_NOTES.equals(cn)) {
            setDOModified(true);
        }
    }

    @Subscribe
    public void onPropertyChange(PropertyChangeEvent evt) {
        final Object s = evt.getSource();
        if (s instanceof EditableJournal
                || s instanceof EditableParticipant
                || s instanceof EditableRecord
                || s instanceof EditableNote) {
            if (EditableJournal.PROP_JOURNAL_START.equals(evt.getPropertyName())) {
                periodList = null;
            }
            setDOModified(true);
        }
    }

    public UndoableEditSupport getUndoSupport() {
        return journal.undoSupport;
    }

    public synchronized void addUndoableEditListener(UndoableEditListener l) {
        journal.undoSupport.addUndoableEditListener(l);
    }

    public synchronized void removeUndoableEditListener(UndoableEditListener l) {
        journal.undoSupport.removeUndoableEditListener(l);
    }
}
