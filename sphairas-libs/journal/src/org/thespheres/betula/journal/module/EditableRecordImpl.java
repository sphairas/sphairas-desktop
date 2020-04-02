/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.module;

import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Objects;
import javax.swing.Action;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.RecordId;
import org.thespheres.betula.journal.JournalConfiguration;
import org.thespheres.betula.journal.JournalRecord;
import org.thespheres.betula.journal.model.EditableJournal;
import org.thespheres.betula.journal.model.EditableRecord;
import org.thespheres.betula.journal.model.JournalEditor;
import org.thespheres.betula.journal.table.FormatUtil;

/**
 *
 * @author boris.heithecker
 */
class EditableRecordImpl<R extends JournalRecord> extends EditableRecord<R> {

    private RecordNode node;

    EditableRecordImpl(RecordId rid, R xmlrecord, EditableJournalImpl ej) {
        super(rid, xmlrecord, ej);
    }

    @Override
    public Node getNodeDelegate() {
        if (node == null) {
            final Lookup base = Lookups.fixed(this, getEditableJournal());
            final Lookup lookup = LookupProviderSupport.createCompositeLookup(base, "Loaders/" + Constants.JOURNAL_RECORD_CONTEXT + "/Lookup");
            node = new RecordNode(this, lookup);
        }
        return node;
    }

    EditableJournalImpl getEditableJournalImpl() {
        return (EditableJournalImpl) journal;
    }

    @Messages({"RecordNode.PasteAndRemove.name=Einfügen und entfernen",
        "RecordNode.PasteAndClear.name=Einfügen und leeren",
        "RecordNode.PasteAndClear.listing.cleared=Übertragen"})
    @ActionReferences({
        @ActionReference(path = "Loaders/text/betula-journal-record-context/Actions",
                id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"), position = 700, separatorAfter = 1000),
        @ActionReference(path = "Loaders/text/betula-journal-record-context/Actions",
                id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"), position = 800, separatorAfter = 1000)})
    static class RecordNode extends AbstractNode implements PropertyChangeListener, NodeTransfer.Paste {

        private final EditableRecordImpl<?> record;

        @SuppressWarnings({"LeakingThisInConstructor",
            "OverridableMethodCallInConstructor"})
        RecordNode(EditableRecordImpl record, Lookup lkp) {//TODO: user createAdditionalLookup.... register JournalEditor....
            super(Children.LEAF, lkp);
            this.record = record;
            setName(record.getRecordId().getId());
            final JournalEditor ed = record.getEditableJournalImpl().getContext().lookup(JournalEditor.class);
            if (ed != null) {
                setDisplayName(ed.formatLocalDate(record));
            } else {
                setDisplayName(FormatUtil.formatRecordId(record.getRecordId(), false));
            }
            setIconBaseWithExtension("org/thespheres/betula/journal/resources/betularec16.png");
        }

        @Override
        public Action[] getActions(boolean context) {
            return Utilities.actionsForPath("Loaders/" + Constants.JOURNAL_RECORD_CONTEXT + "/Actions").stream()
                    .map(Action.class::cast)
                    .toArray(Action[]::new);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
        }

        @Override
        public boolean canDestroy() {
            return true;
        }

        @Override
        public void destroy() throws IOException {
            try {
                record.getEditableJournalImpl().removeRecord(record.getIndex());
                super.destroy();
            } catch (Exception e) {
                throw e;
            }
        }

        @Override
        public boolean canCut() {
            return true;
        }

        @Override
        public Transferable clipboardCut() throws IOException {
            return NodeTransfer.createPaste(this);
        }

        @Override
        public Transferable clipboardCopy() throws IOException {
            return NodeTransfer.createPaste(this);
        }

        @Override
        public PasteType[] types(Node target) {
            final EditableJournal ej = target.getLookup().lookup(EditableJournal.class);

            class PasteAndRemove extends PasteType {

                @Override
                public String getName() {
                    return NbBundle.getMessage(RecordNode.class, "RecordNode.PasteAndRemove.name");
                }

                @Override
                public Transferable paste() throws IOException {
                    if (!Objects.equals(ej, record.getEditableJournal())) {
                        try {
                            final int index = record.getIndex();
                            ej.updateRecord(record.getRecordId(), record.getRecord());
                            record.getEditableJournalImpl().removeRecord(index);
                        } catch (Exception e) {
                            throw e;
                        }
                    }
                    return ExTransferable.EMPTY;
                }

            }
            class PasteAndClear extends PasteType {

                @Override
                public String getName() {
                    return NbBundle.getMessage(RecordNode.class, "RecordNode.PasteAndClear.name");
                }

                @Override
                public Transferable paste() throws IOException {
                    if (!Objects.equals(ej, record.getEditableJournal())) {
                        try {
                            ej.updateRecord(record.getRecordId(), record.getRecord());
                            final String listing = NbBundle.getMessage(RecordNode.class, "RecordNode.PasteAndClear.listing.cleared");
                            record.setListing(listing);
                            record.getEditableJournal().getEditableParticipants().stream()
                                    .forEach(ep -> record.setGradeAt(ep.getIndex(), JournalConfiguration.getInstance().getJournalUndefinedGrade(), null));
                        } catch (Exception e) {
                            throw e;
                        }
                    }
                    return ExTransferable.EMPTY;
                }

            }
            if (ej != null) {
                return new PasteType[]{new PasteAndRemove(), new PasteAndClear()};
            }
            return new PasteType[]{};
        }
    }

    @ServiceProvider(service = LookupProvider.class, path = "Loaders/text/betula-journal-record-context/Lookup")
    public static class UnitIntegrationLookup implements LookupProvider {

        @Override
        public Lookup createAdditionalLookup(Lookup base) {
            EditableRecordImpl record = base.lookup(EditableRecordImpl.class);
            final JournalEditor ed = record.getEditableJournalImpl().getContext().lookup(JournalEditor.class);
            if (ed != null) {
                return Lookups.singleton(ed);
            }
            return Lookup.EMPTY;
        }
    }

}
