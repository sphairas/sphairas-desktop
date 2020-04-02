/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GradeFactory;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.journal.model.EditableRecord;
import org.thespheres.betula.journal.JournalConfiguration;

/**
 *
 * @author boris.heithecker
 */
class FillAction implements ActionListener {

    protected final List<EditableRecord> list;
    protected final Grade override;
    private final Grade fill;

    FillAction(List<EditableRecord> context, Grade override, Grade fill) {
        list = context;
        this.override = override;
        this.fill = fill;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        list.forEach(this::fill);
    }

    protected void fill(EditableRecord er) {
        for (int i = 0; i < er.getEditableJournal().getEditableParticipants().size(); i++) {
            final Grade g = er.getGradeAt(i);
            overrideGrade(er, i, g);
        }
    }

    protected void overrideGrade(final EditableRecord record, int index, final Grade current) {
        final Timestamp ts = getOverrideTimestamp(record, index, current);
        if (override == null) {
            record.setGradeAt(index, fill, ts);
        } else if (current == null || current.equals(override)) {
            record.setGradeAt(index, fill, ts);
        }
    }

    protected Timestamp getOverrideTimestamp(final EditableRecord record, final int index, final Grade current) {
        return Timestamp.now();
    }

    @ActionID(category = "Betula", id = "org.thespheres.betula.journal.actions.FillAction.FillEntriesDefaultAction")
    @ActionReferences({
        @ActionReference(path = "Loaders/text/betula-journal-record-context/Actions", position = 6000, separatorAfter = 100000)
    })
    @ActionRegistration(displayName = "#FillEntriesDefaultAction.displayName", lazy = true)
    @NbBundle.Messages("FillEntriesDefaultAction.displayName=Alle „?“ durch „*“ ersetzen")
    public static class FillEntriesDefaultAction extends FillAction {

        public FillEntriesDefaultAction(final List<EditableRecord> context) {
            super(context, JournalConfiguration.getInstance().getJournalUndefinedGrade(), JournalConfiguration.getInstance().getJournalDefaultGrade());
        }

    }

    @ActionID(category = "Betula", id = "org.thespheres.betula.journal.actions.FillAction.FillEntriesUndefinedAction")
    @ActionReferences({
        @ActionReference(path = "Loaders/text/betula-journal-record-context/Actions", position = 6050, separatorAfter = 100000)
    })
    @ActionRegistration(displayName = "#FillEntriesUndefinedAction.displayName", lazy = true)
    @NbBundle.Messages("FillEntriesUndefinedAction.displayName=Alle Einträge zurücksetzen")
    public static class FillEntriesUndefinedAction extends FillAction {

        public FillEntriesUndefinedAction(List<EditableRecord> context) {
            super(context, null, JournalConfiguration.getInstance().getJournalUndefinedGrade());
        }
    }

    @ActionID(category = "Betula", id = "org.thespheres.betula.journal.actions.FillAction.FillEntriesEntfallAction")
    @ActionReferences({
        @ActionReference(path = "Loaders/text/betula-journal-record-context/Actions", position = 6060, separatorAfter = 100000)
    })
    @ActionRegistration(displayName = "#FillEntriesEntfallAction.displayName", lazy = true)
    @NbBundle.Messages("FillEntriesEntfallAction.displayName=Alle „?“ durch „---“ ersetzen")
    public static class FillEntriesEntfallAction extends FillAction {

        public FillEntriesEntfallAction(final List<EditableRecord> context) {
            super(context, JournalConfiguration.getInstance().getJournalUndefinedGrade(), GradeFactory.find("mitarbeit2", "entfall"));
        }

    }

}
