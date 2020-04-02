/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.model;

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.Objects;
import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author boris.heithecker
 */
@ActionReferences({
    @ActionReference(path = "Loaders/application/betula-classroomtest-student-context/Actions", id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"), position = 500)
})
class StudentNode extends AbstractNode implements NodeTransfer.Paste {

    public static final String STUDENT_CONTEXT_MIME = "application/betula-classroomtest-student-context";
    private final EditableStudent student;

    StudentNode(EditableStudent stud) {
        super(Children.LEAF, Lookups.singleton(stud));
        student = stud;
        //TODO: new???
        setIconBaseWithExtension("org/thespheres/betula/classtest/resources/betulastud16.png");
//        this.context = context;
    }

    @Override
    public String getDisplayName() {
        return student.getStudent().getDirectoryName() + " (" + student.getUnit() + ")";
    }

    @Override
    public boolean canCut() {
        return true;
    }

    @Override
    public boolean canCopy() {
        return true;
    }

    @Override
    public Action[] getActions(boolean context) {
        return Utilities.actionsForPath("Loaders/" + STUDENT_CONTEXT_MIME + "/Actions").stream()
                .map(Action.class::cast)
                .toArray(Action[]::new);
    }

    @Override
    public Transferable clipboardCopy() throws IOException {
        return NodeTransfer.createPaste(this);
    }

    @NbBundle.Messages({"StudentNode.PasteAndRemove.name=Einfügen und entfernen",
        "StudentNode.PasteAndClear.name=Einfügen und leeren",
        "StudentNode.PasteAndClear.listing.cleared=Übertragen"})
    @Override
    public PasteType[] types(Node target) {
        final EditableClassroomTest test = target.getLookup().lookup(EditableClassroomTest.class);

        class PasteAndRemove extends PasteType {

            @Override
            public String getName() {
                return NbBundle.getMessage(StudentNode.class, "StudentNode.PasteAndRemove.name");
            }

            @Override
            public Transferable paste() throws IOException {
                if (!Objects.equals(test, student.getEditableClassroomTest())) {
                    Mutex.EVENT.writeAccess(() -> {
                        try {
                            final EditableStudent es = test.updateStudent(student.getStudent());
                            es.updateScores(student.getStudentScores());
                            student.getEditableClassroomTest().removeStudent(student.getStudentId());
                        } catch (Exception e) {
                            throw e;
                        }
                    }
                    );
                }
                return ExTransferable.EMPTY;
            }

        }
        class PasteAndClear extends PasteType {

            @Override
            public String getName() {
                return NbBundle.getMessage(StudentNode.class, "StudentNode.PasteAndClear.name");
            }

            @Override
            public Transferable paste() throws IOException {
//                if (!Objects.equals(test, record.getEditableJournal())) {
//                    try {
//                        test.updateRecord(record.getRecordId(), record.getRecord());
//                        final String listing = NbBundle.getMessage(RecordNode.class, "RecordNode.PasteAndClear.listing.cleared");
//                        record.setListing(listing);
//                        record.getEditableJournal().getEditableParticipants().stream()
//                                .forEach(ep -> record.setGradeAt(ep.getIndex(), JournalConfiguration.getInstance().getJournalUndefinedGrade(), null));
//                    } catch (Exception e) {
//                        throw e;
//                    }
//                }
                return ExTransferable.EMPTY;
            }

        }
        if (test != null) {
            return new PasteType[]{new PasteAndRemove(), new PasteAndClear()};
        }
        return new PasteType[]{};
    }

}
