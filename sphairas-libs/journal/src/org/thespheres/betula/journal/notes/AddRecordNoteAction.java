/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.notes;

import org.thespheres.betula.journal.model.JournalAnnotatable;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.journal.model.EditableJournal;
import org.thespheres.betula.journal.model.EditableParticipant;
import org.thespheres.betula.journal.model.EditableRecord;
import org.thespheres.betula.journal.model.JournalEditor;
import org.thespheres.betula.journal.xml.XmlRecordNote;

public class AddRecordNoteAction implements ActionListener {

    static final String RECORD = "record";
    static final String PARTICIPANT = "participant";
    static final String JOURNAL = "journal";
    static final String EDITOR = "editor";
    static final String GRADE = "grade";
    static final String CONVENTIONS = "conventions";
    static final String TEXT = "text";
    private final EditableParticipant participant;
    private final EditableJournal<?, ?> journal;
    private final EditableRecord record;
    private final JournalEditor editor;

    AddRecordNoteAction(EditableParticipant participant, EditableRecord record) {
        this.participant = participant;
        this.record = record;
        Lookup context = Utilities.actionsGlobalContext();
        journal = context.lookup(EditableJournal.class);
        editor = context.lookup(JournalEditor.class);
    }

    @ActionID(category = "Betula", id = "org.thespheres.betula.journal.notes.AddParticipantNoteAction")
    @ActionReferences({
        @ActionReference(path = "Menu/betula-project-local", position = 80000, separatorBefore = 50000),
        @ActionReference(path = "Loaders/text/betula-journal-file+xml/Actions", position = 4000),
        @ActionReference(path = "Loaders/text/betula-journal-record-context/Actions", position = 3000),
        @ActionReference(path = "Shortcuts", name = "D-H")})
    @ActionRegistration(displayName = "#AddParticipantNoteAction.displayName")
    @Messages({"AddParticipantNoteAction.displayName=Anmerkung hinzufügen",
        "AddRecordNoteAction.cause.default.text=Anmerkung"})
    public static class AddParticipantNoteAction extends AddRecordNoteAction {

        public AddParticipantNoteAction(JournalAnnotatable annot) {
            super(annot.getAnnotatableParticipant(), annot.getAnnotatableRecord());
        }
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        if (journal == null) {
            return;
        }
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        panels.add(new NoteCustomizerPanel.NoteCustomizerWizardPanel());
        String[] steps = new String[panels.size()];
        for (int i = 0; i < panels.size(); i++) {
            Component c = panels.get(i).getComponent();
            // Default step name to component name of panel.
            steps[i] = c.getName();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
            }
        }
        WizardDescriptor wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<>(panels));
        wiz.putProperty(JOURNAL, journal);
        wiz.putProperty(EDITOR, editor);
        wiz.putProperty(PARTICIPANT, participant);
        wiz.putProperty(RECORD, record);
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle(NbBundle.getMessage(AddRecordNoteAction.class, "AddParticipantNoteAction.displayName"));
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            final EditableParticipant p = (EditableParticipant) wiz.getProperty(AddRecordNoteAction.PARTICIPANT);
            final EditableRecord r = (EditableRecord) wiz.getProperty(AddRecordNoteAction.RECORD);
            final Grade g = (Grade) wiz.getProperty(AddRecordNoteAction.GRADE);
            final String t = (String) wiz.getProperty(AddRecordNoteAction.TEXT);
            addNote(p, r, g, t != null ? t : NbBundle.getMessage(AddRecordNoteAction.class, "AddRecordNoteAction.cause.default.text"));
        }
    }

    private void addNote(EditableParticipant part, EditableRecord<?> rec, Grade grade, final String text) {
        XmlRecordNote xrn = new XmlRecordNote("default", part.getStudentId());
        xrn.setGrade(grade);
        xrn.setCause(text, Timestamp.now());
        if (rec != null) {
            journal.getRecordSet().getRecords().get(rec.getRecordId()).getNotes().add(xrn);
            journal.updateNote(xrn, rec.getIndex());
        } else {
            journal.getRecordSet().getNotes().add(xrn);
            journal.updateNote(xrn);
        }
    }

}
