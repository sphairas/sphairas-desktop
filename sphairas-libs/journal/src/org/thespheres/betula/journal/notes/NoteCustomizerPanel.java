/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.notes;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.journal.model.EditableJournal;
import org.thespheres.betula.journal.model.EditableParticipant;
import org.thespheres.betula.journal.model.EditableRecord;
import org.thespheres.betula.journal.model.JournalEditor;
import org.thespheres.betula.ui.GradeComboBoxModel;

@Messages({"NoteCustomizerPanel.name=Anmerkung"})
final class NoteCustomizerPanel extends JPanel {

    private EditableJournal<?, ?> journal;
    private JournalEditor editor;
    private final DefaultComboBoxModel participantsModel = new DefaultComboBoxModel();
    private final StringValue participantsString = v -> v instanceof EditableParticipant ? ((EditableParticipant) v).getDirectoryName() : " ";
    private final DefaultComboBoxModel recordsModel = new DefaultComboBoxModel();
    private final StringValue recordsString = v -> {
        if (v instanceof EditableRecord) {
            EditableRecord er = (EditableRecord) v;
            if (editor != null) {
                return editor.formatLocalDate(er, false);
            }
            return er.getRecordId().getId();
        }
        return " ";
    };
    private final GradeComboBoxModel gcbm;
    private EditableRecord record;
    private EditableParticipant participant;

    NoteCustomizerPanel() {
        gcbm = new GradeComboBoxModel();
        initComponents();
        participantsModel.removeAllElements();
        participantsBox.setRenderer(new DefaultListRenderer(participantsString));
        recordsBox.setRenderer(new DefaultListRenderer(recordsString));
        gradesBox.setRenderer(new DefaultListRenderer(gcbm));
        gcbm.setUseLongLabel(false);
        gcbm.initialize(gradesBox);
    }

    void initialize(EditableJournal journal, JournalEditor editor, EditableParticipant student, EditableRecord record, String[] conventions, Grade grade, String text) {
        this.journal = journal;
        this.editor = editor;
        this.participant = student;
        this.record = record;
        gcbm.setConventions(conventions);
        participantsModel.removeAllElements();
        if (student == null) {
            participantsModel.addElement(null);
            journal.getEditableParticipants().stream()
                    .forEach(participantsModel::addElement);
            participantsBox.setEnabled(true);
        } else {
            participantsModel.addElement(student);
            participantsBox.setEnabled(false);
        }
        recordsModel.removeAllElements();
        if (record == null) {
            recordsModel.addElement(null);
            journal.getEditableRecords().stream()
                    .forEach(recordsModel::addElement);
            recordsBox.setEnabled(true);
        } else {
            recordsModel.addElement(record);
            recordsBox.setEnabled(false);
        }
        if (grade != null) {
            gcbm.setSelectedItem(grade);
            addGradeCheckBox.setSelected(true);
        }
        textArea.setText(StringUtils.trimToEmpty(text));
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(NoteCustomizerPanel.class, "NoteCustomizerPanel.name");
    }

    EditableRecord getRecord() {
        return record != null ? record : (EditableRecord) recordsModel.getSelectedItem();
    }

    EditableParticipant getParticipant() {
        return participant != null ? participant : (EditableParticipant) participantsModel.getSelectedItem();
    }

    Grade getGrade() {
        return addGradeCheckBox.isSelected() ? gcbm.getSelectedValue() : null;
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        studentLabel = new javax.swing.JLabel();
        recordLabel = new javax.swing.JLabel();
        gradeLabel = new javax.swing.JLabel();
        gradesBox = new org.jdesktop.swingx.JXComboBox();
        participantsBox = new org.jdesktop.swingx.JXComboBox();
        recordsBox = new org.jdesktop.swingx.JXComboBox();
        addGradeCheckBox = new javax.swing.JCheckBox();
        textLabel = new javax.swing.JLabel();
        textPane = new javax.swing.JScrollPane();
        textArea = new org.jdesktop.swingx.JXTextArea();

        org.openide.awt.Mnemonics.setLocalizedText(studentLabel, org.openide.util.NbBundle.getMessage(NoteCustomizerPanel.class, "NoteCustomizerPanel.studentLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(recordLabel, org.openide.util.NbBundle.getMessage(NoteCustomizerPanel.class, "NoteCustomizerPanel.recordLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(gradeLabel, org.openide.util.NbBundle.getMessage(NoteCustomizerPanel.class, "NoteCustomizerPanel.gradeLabel.text")); // NOI18N

        gradesBox.setModel(gcbm);

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, addGradeCheckBox, org.jdesktop.beansbinding.ELProperty.create("${selected}"), gradesBox, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        participantsBox.setModel(participantsModel);

        recordsBox.setModel(recordsModel);

        org.openide.awt.Mnemonics.setLocalizedText(addGradeCheckBox, org.openide.util.NbBundle.getMessage(NoteCustomizerPanel.class, "NoteCustomizerPanel.addGradeCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(textLabel, org.openide.util.NbBundle.getMessage(NoteCustomizerPanel.class, "NoteCustomizerPanel.textLabel.text")); // NOI18N

        textArea.setColumns(20);
        textArea.setRows(5);
        textPane.setViewportView(textArea);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(textPane)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(gradeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                        .addComponent(gradesBox, javax.swing.GroupLayout.PREFERRED_SIZE, 261, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(studentLabel)
                            .addComponent(recordLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(participantsBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(recordsBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(addGradeCheckBox)
                            .addComponent(textLabel))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(studentLabel)
                    .addComponent(participantsBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(recordLabel)
                    .addComponent(recordsBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(textLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textPane)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(addGradeCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(gradeLabel)
                    .addComponent(gradesBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        bindingGroup.bind();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox addGradeCheckBox;
    private javax.swing.JLabel gradeLabel;
    private org.jdesktop.swingx.JXComboBox gradesBox;
    private org.jdesktop.swingx.JXComboBox participantsBox;
    private javax.swing.JLabel recordLabel;
    private org.jdesktop.swingx.JXComboBox recordsBox;
    private javax.swing.JLabel studentLabel;
    private org.jdesktop.swingx.JXTextArea textArea;
    private javax.swing.JLabel textLabel;
    private javax.swing.JScrollPane textPane;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables

    public static class NoteCustomizerWizardPanel implements WizardDescriptor.Panel<WizardDescriptor> {

        private NoteCustomizerPanel component;

        @Override
        public NoteCustomizerPanel getComponent() {
            if (component == null) {
                component = new NoteCustomizerPanel();
            }
            return component;
        }

        @Override
        public HelpCtx getHelp() {
            // Show no Help button for this panel:
            return HelpCtx.DEFAULT_HELP;
            // If you have context help:
            // return new HelpCtx("help.key.here");
        }

        @Override
        public boolean isValid() {
            // If it is always OK to press Next or Finish, then:
            return true;
            // If it depends on some condition (form filled out...) and
            // this condition changes (last form field filled in...) then
            // use ChangeSupport to implement add/removeChangeListener below.
            // WizardDescriptor.ERROR/WARNING/INFORMATION_MESSAGE will also be useful.
        }

        @Override
        public void addChangeListener(ChangeListener l) {
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
        }

        @Override
        public void readSettings(WizardDescriptor wiz) {
            final EditableJournal j = (EditableJournal) wiz.getProperty(AddRecordNoteAction.JOURNAL);
            final JournalEditor e = (JournalEditor) wiz.getProperty(AddRecordNoteAction.EDITOR);
            final EditableParticipant p = (EditableParticipant) wiz.getProperty(AddRecordNoteAction.PARTICIPANT);
            final EditableRecord r = (EditableRecord) wiz.getProperty(AddRecordNoteAction.RECORD);
            final Grade g = (Grade) wiz.getProperty(AddRecordNoteAction.GRADE);
            final String[] c = (String[]) wiz.getProperty(AddRecordNoteAction.CONVENTIONS);
            final String t = (String) wiz.getProperty(AddRecordNoteAction.TEXT);
            getComponent().initialize(j, e, p, r, c, g, t);
        }

        @Override
        public void storeSettings(WizardDescriptor wiz) {
            final EditableParticipant p = getComponent().getParticipant();
            wiz.putProperty(AddRecordNoteAction.PARTICIPANT, p);
            final EditableRecord r = getComponent().getRecord();
            wiz.putProperty(AddRecordNoteAction.RECORD, r);
            final Grade g = getComponent().getGrade();
            wiz.putProperty(AddRecordNoteAction.GRADE, g);
            final String t = getComponent().textArea.getText();
            wiz.putProperty(AddRecordNoteAction.TEXT, StringUtils.trimToNull(t));
        }

    }
}
