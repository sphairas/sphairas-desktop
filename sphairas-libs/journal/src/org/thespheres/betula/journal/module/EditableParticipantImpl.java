/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.module;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.Student;
import org.thespheres.betula.journal.model.EditableParticipant;

/**
 *
 * @author boris.heithecker
 */
class EditableParticipantImpl extends EditableParticipant {

    private ParticipantNode node;

    EditableParticipantImpl(Student student, EditableJournalImpl ej) {
        super(student, ej);
    }

    @Override
    public Node getNodeDelegate() {
        if (node == null) {
            node = new ParticipantNode(this);
        }
        return node;
    }

    EditableJournalImpl getEditableRecordImpl() {
        return (EditableJournalImpl) ecal;
    }

    static class ParticipantNode extends AbstractNode implements PropertyChangeListener {

        private final EditableParticipantImpl student;

        @SuppressWarnings({"LeakingThisInConstructor",
            "OverridableMethodCallInConstructor"})
        ParticipantNode(EditableParticipantImpl student) {
            super(Children.LEAF,  Lookups.fixed(student, student.getEditableJournal()));
            this.student = student;
            setIconBaseWithExtension("org/thespheres/betula/journal/resources/betulastud16.png");
            setDisplayName(student.getDirectoryName());
        }

        @Override
        public Action[] getActions(boolean context) {
            return Utilities.actionsForPath("Loaders/text/betula-journal-participant-context/Actions").stream()
                    .map(Action.class::cast)
                    .toArray(Action[]::new);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
        }

    }
}
