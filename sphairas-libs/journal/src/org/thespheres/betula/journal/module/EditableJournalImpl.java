/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.module;

import com.google.common.eventbus.Subscribe;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.RecordId;
import org.thespheres.betula.Student;
import org.thespheres.betula.journal.Journal;
import org.thespheres.betula.journal.JournalRecord;
import org.thespheres.betula.journal.model.EditableJournal;
import org.thespheres.betula.journal.model.EditableParticipant;
import org.thespheres.betula.journal.model.EditableRecord;
import org.thespheres.betula.util.CollectionChangeEvent;

/**
 *
 * @author boris.heithecker
 */
public abstract class EditableJournalImpl<R extends JournalRecord, J extends Journal<R>> extends EditableJournal<R, J> {

    private final Lookup context;
    private Node nodeWithRecordChildren;
    private Node nodeWithParticipantsChildren;

    protected EditableJournalImpl(J rset, Lookup context) {
        super(rset);
        this.context = context;
    }

    public Lookup getContext() {
        return context;
    }

    public Node getNodeWithRecordChildren() {
        if (nodeWithRecordChildren == null) {
            final RecordChildren ch = new RecordChildren(this);
            nodeWithRecordChildren = new AbstractNode(ch, Lookups.singleton(ch.getIndex()));
        }
        return nodeWithRecordChildren;
    }

    public Node getNodeWithParticipantsChildren() {
        if (nodeWithParticipantsChildren == null) {
            final ParticipantChildren ch = new ParticipantChildren(this);
            nodeWithParticipantsChildren = new AbstractNode(ch, Lookups.singleton(ch.getIndex()));
        }
        return nodeWithParticipantsChildren;
    }

    @Override
    protected EditableRecord<R> createEditableRecord(RecordId date) {
        return new EditableRecordImpl(date, journal.getRecords().get(date), this);
    }

    @Override
    protected EditableParticipant createEditableParticipant(Student s) {
        return new EditableParticipantImpl(s, this);
    }

    static class RecordChildren extends Index.KeysChildren<EditableRecord> {

        @SuppressWarnings({"LeakingThisInConstructor",
            "OverridableMethodCallInConstructor"})
        RecordChildren(EditableJournal ej) {
            super(ej.getEditableRecords());
//            ej.addPropertyChangeListener(this);
            ej.getEventBus().register(this);
        }

        @Override
        protected Node[] createNodes(EditableRecord key) {
            return new Node[]{key.getNodeDelegate()};
        }

        @Subscribe
        public void onModelChange(CollectionChangeEvent event) {
            if (EditableJournal.COLLECTION_RECORDS.equals(event.getCollectionName())) {
                Mutex.EVENT.writeAccess(this::update);
            }
        }
    }

    static final class ParticipantChildren extends Index.KeysChildren<EditableParticipant> {

        @SuppressWarnings({"LeakingThisInConstructor",
            "OverridableMethodCallInConstructor"})
        ParticipantChildren(EditableJournal ej) {
            super(ej.getEditableParticipants());
        }

        @Override
        protected Node[] createNodes(EditableParticipant key) {
            return new Node[]{key.getNodeDelegate()};
        }

        @Subscribe
        public void onModelChange(CollectionChangeEvent event) {
            if (EditableJournal.COLLECTION_PARTICIPANTS.equals(event.getCollectionName())) {
                Mutex.EVENT.writeAccess(this::update);
            }
        }
    }
}
