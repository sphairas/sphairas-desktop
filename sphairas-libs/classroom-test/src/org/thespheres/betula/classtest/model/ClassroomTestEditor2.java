/*
 * ClassroomTestEditor.java
 *
 * Created on 1. November 2007, 07:49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.model;

import com.google.common.eventbus.Subscribe;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Objects;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEditSupport;
import org.jdesktop.swingx.JXTable;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.Student;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.Unit;
import org.thespheres.betula.assess.AssessmentContext;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.classtest.Assessable;
import org.thespheres.betula.classtest.Basket;
import org.thespheres.betula.classtest.Hierarchical;
import org.thespheres.betula.classtest.module2.BasketNode;
import org.thespheres.betula.classtest.table2.ClasstestTableModel2;
import org.thespheres.betula.classtest.table2.ClasstestTableSupport;
import org.thespheres.betula.util.XmlStudent;
import org.thespheres.betula.util.CollectionChangeEvent;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 * @param <P>
 * @param <ECT>
 */
public class ClassroomTestEditor2<P extends Assessable.Problem & Basket.Ref<?> & Basket<?, ?> & Hierarchical, ECT extends EditableClassroomTest<P, ?, ?>> implements DropTargetListener {

    private final ECT etest;
    private final Lookup context;
    private final ClasstestTableSupport env;
    private AbstractNode nodeWithProblemChildren;
    private AbstractNode nodeWithStudentChildren;

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    public ClassroomTestEditor2(ECT test, Lookup context, ClasstestTableSupport env) {
        this.etest = test;
        this.context = context;
        this.env = env;
        etest.getEventBus().register(this);
    }

    public DataObject getDataObject() {
        return env.getDataObject();
    }

    private void setDOModified(boolean mod) {
        env.getDataObject().setModified(mod);
    }

    public Node getNodeWithProblemChildren() {
        if (nodeWithProblemChildren == null) {
            final ProblemChildren ch = new ProblemChildren(this, null);
            nodeWithProblemChildren = new AbstractNode(ch, Lookups.singleton(ch.getIndex()));
        }
        return nodeWithProblemChildren;
    }

    public Node getNodeWithStudentChildren() {
        if (nodeWithStudentChildren == null) {
            final StudentChildren ch = new StudentChildren(this);
            nodeWithStudentChildren = new AbstractNode(ch, Lookups.singleton(ch.getIndex()));
        }
        return nodeWithStudentChildren;
    }

    public ECT getEditableClassroomTest() {
        return etest;
    }

    public EditableStudent insertStudent(Student student) {
        EditableStudent stud = etest.updateStudent(new XmlStudent(student.getStudentId(), student.getDirectoryName()));
        return stud;
    }

    public void addStudentForIdFromUnit(StudentId id) {
        Unit unit = getContext().lookup(Unit.class);
        Student s = null;
        if (unit != null && (s = unit.findStudent(id)) != null) {
            etest.updateStudent(s);
        }
    }

    public void removeStudent(StudentId stud) {
        EventQueue.invokeLater(() -> etest.removeStudent(stud));
    }

    public EditableProblem insertProblem(P p) {
        final EditableProblem ret = etest.updateProblem(p);
        getUndoSupport().postEdit(new InsertProblemEdit(p));
        return ret;
    }

    public void updateStudents() {
        Unit unit = context.lookup(Unit.class);
        if (unit != null) {
            unit.getStudents()
                    .forEach(s -> etest.updateStudent(s));
        }
    }

    public ClasstestTableModel2 getModel() {
        return env.getModel();
    }

    @Subscribe
    public void classtestProperty(PropertyChangeEvent evt) {
        this.setDOModified(true);
    }

    @Subscribe
    public void onModelChange(CollectionChangeEvent event) {
        setDOModified(true);
    }

    public Lookup getContext() {
        return context;
    }

    public AssessmentContext getAssessmentContext() {
        return context.lookupAll(AssessmentContext.class).stream()
                .collect(CollectionUtil.singleOrNull());
    }

    public AssessmentConvention getAssessmentConvention() {
        AssessmentContext assctx = getAssessmentContext();
        return assctx instanceof AssessmentConvention ? (AssessmentConvention) assctx : null;
    }

    public UndoableEditSupport getUndoSupport() {
        return etest.undoSupport;
    }

    public synchronized void addUndoableEditListener(UndoableEditListener l) {
        etest.undoSupport.addUndoableEditListener(l);
    }

    public synchronized void removeUndoableEditListener(UndoableEditListener l) {
        etest.undoSupport.removeUndoableEditListener(l);
    }

    public void setGradeAt(final EditableStudent es, Grade g) {
        final Grade old = es.getStudentScores().getGrade();
        if (!Objects.equals(g, old)) {
            es.getStudentScores().setGrade(g);
            boolean setAutoDistributing = g == null;
            es.getStudentScores().setAutoDistributing(setAutoDistributing);
            getUndoSupport().postEdit(new SetGradeEdit(es, old, g, setAutoDistributing));
        }
    }

    public void setStudScore(final int problemIndex, final int studentIndex, Object value) {
        if (value != null) {
            final Double score = (Double) value;
            final EditableProblem problem = etest.getEditableProblems().get(problemIndex);
            final EditableStudent es = etest.getEditableStudents().get(studentIndex);
            final Double old = es.getStudentScores().get(problem.getId());
            if (!Objects.equals(old, score)) {
                es.getStudentScores().put(problem.getId(), score);
                getUndoSupport().postEdit(new SetScoreEdit(es, old, score, problem));
            }
        }
    }

    public void setProblemDisplayName(final EditableProblem problem, final String name) {
        final String old = problem.getDisplayName();
        problem.setDisplayName(name);
        getUndoSupport().postEdit(new SetProblemDisplayNameEdit(problem, old, name));
    }

    public void setProbMax(int index, Object val) {
        if (val instanceof Double) {
            final Double d = (Double) val;
            final EditableProblem ep = etest.getEditableProblems().get(index);
            final Integer old = ep.getMaxScore();
            ep.setMaxScore(d.intValue());
            getUndoSupport().postEdit(new SetProblemMaxScoreEdit(ep, old, d.intValue()));
        }
    }

    public void setProbWeight(int index, Object val) {
        if (val instanceof Double) {
            final Double w = (Double) val;
            final EditableProblem ep = etest.getEditableProblems().get(index);
            final Double old = ep.getWeight();
            ep.setWeight(w);
            getUndoSupport().postEdit(new SetProblemWeightEdit(ep, old, w));
        }
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        Node n = NodeTransfer.node(dtde.getTransferable(), NodeTransfer.CLIPBOARD_COPY);
        if (n != null && n.getLookup().lookup(EditableProblem.class) != null && !n.getLookup().lookup(EditableProblem.class).isBasket()) {
            dtde.acceptDrag(NodeTransfer.CLIPBOARD_COPY);
        } else if (NodeTransfer.findPaste(dtde.getTransferable()) != null) {
            dtde.acceptDrag(NodeTransfer.CLIPBOARD_COPY);
        } else {
            dtde.rejectDrag();
        }
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        Node n = NodeTransfer.node(dtde.getTransferable(), NodeTransfer.CLIPBOARD_COPY);
        if (n != null && n.getLookup().lookup(EditableProblem.class) != null && !n.getLookup().lookup(EditableProblem.class).isBasket()) {
            EditableProblem dragged = n.getLookup().lookup(EditableProblem.class);
            Component cmp = dtde.getDropTargetContext().getComponent();
            JXTable tbl = null;
            if (cmp instanceof JScrollPane && ((JScrollPane) cmp).getViewport().getView() instanceof JXTable) {
                tbl = (JXTable) ((JScrollPane) cmp).getViewport().getView();
            } else if (cmp instanceof JXTable) {
                tbl = (JXTable) cmp;
            }
            final JXTable table = tbl;
            if (table != null && table.getModel() instanceof ClasstestTableModel2) {
                ClasstestTableModel2 currentModel = (ClasstestTableModel2) table.getModel();
                Point p = SwingUtilities.convertPoint(table, dtde.getLocation(), table);  //letzerer OutlineView.this
                int index = table.convertColumnIndexToModel(table.columnAtPoint(p)) - 1;
                if (index >= 0 && index < currentModel.getItemsModel().getEditableProblems().size()) {
                    final EditableProblem<?> ep = currentModel.getItemsModel().getEditableProblems().get(index);
                    ((EditableBasket) ep).addReference(dragged);
                }
            }
            dtde.dropComplete(true);
        } else {
            dtde.rejectDrop();
        }
    }

    @Messages({"SetGradeEdit.name=Zensur für {0}"})
    class SetGradeEdit extends AbstractTableEdit<Grade, EditableStudent> {

        private final boolean setAutoDistributing;

        SetGradeEdit(EditableStudent student, Grade before, Grade newValue, boolean setAutoDistributing) {
            super(student, before, newValue);
            this.setAutoDistributing = setAutoDistributing;
        }

        @Override
        protected String getName() {
            return NbBundle.getMessage(SetGradeEdit.class, "SetGradeEdit.name", item.getStudent().getFullName());
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            item.getStudentScores().setGrade(overridden);
            item.getStudentScores().setAutoDistributing(!setAutoDistributing);
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            item.getStudentScores().setGrade(override);
            item.getStudentScores().setAutoDistributing(setAutoDistributing);
        }
    }

    @Messages({"SetScoreEdit.name=Punktzahl für {0} in Teilaufgabe {1}"})
    class SetScoreEdit extends AbstractTableEdit<Double, EditableStudent> {

        private final EditableProblem problem;

        SetScoreEdit(EditableStudent student, Double before, Double newValue, EditableProblem problem) {
            super(student, before, newValue);
            this.problem = problem;
        }

        @Override
        protected String getName() {
            return NbBundle.getMessage(SetGradeEdit.class, "SetScoreEdit.name", item.getStudent().getFullName(), problem.getDisplayName());
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            item.getStudentScores().put(problem.getId(), overridden);
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            item.getStudentScores().put(problem.getId(), override);
        }
    }

    @Messages({"SetProblemMaxScoreEdit.name=Max. Punktezahl für Aufgabe {0}"})
    class SetProblemMaxScoreEdit extends AbstractTableEdit<Integer, EditableProblem> {

        SetProblemMaxScoreEdit(EditableProblem problem, Integer before, Integer newValue) {
            super(problem, before, newValue);
        }

        @Override
        protected String getName() {
            return NbBundle.getMessage(SetGradeEdit.class, "SetProblemMaxScoreEdit.name", item.getDisplayName());
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            item.setMaxScore(overridden);
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            item.setMaxScore(override);
        }
    }

    @Messages({"SetProblemWeightEdit.name=Gewicht für Aufgabe {0}"})
    class SetProblemWeightEdit extends AbstractTableEdit<Double, EditableProblem> {

        SetProblemWeightEdit(EditableProblem problem, Double before, Double newValue) {
            super(problem, before, newValue);
        }

        @Override
        protected String getName() {
            return NbBundle.getMessage(SetGradeEdit.class, "SetProblemWeightEdit.name", item.getDisplayName());
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            item.setWeight(overridden);
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            item.setWeight(override);
        }
    }

    @Messages({"SetProblemDisplayNameEdit.name=Aufgabenname"})
    class SetProblemDisplayNameEdit extends AbstractTableEdit<String, EditableProblem> {

        SetProblemDisplayNameEdit(EditableProblem problem, String before, String newValue) {
            super(problem, before, newValue);
        }

        @Override
        protected String getName() {
            return NbBundle.getMessage(SetGradeEdit.class, "SetProblemDisplayNameEdit.name", item.getDisplayName());
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            item.setDisplayName(overridden);
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            item.setDisplayName(override);
        }
    }

    static class MissingProblemException extends CannotUndoException {

        private String msg;

        MissingProblemException(String msg) {
            this.msg = msg;
        }

        @Override
        public String getMessage() {
            return msg;
        }

    }

    @Messages({"InsertProblemEdit.name=Aufgabe hinzufügen",
        "InsertProblemEdit.missingEditableProblem=Die Aufgabe ist nicht mehr vorhanden."})
    class InsertProblemEdit extends AbstractUndoableEdit {

        private final P problem;

        InsertProblemEdit(P problem) {
            this.problem = problem;
        }

        @Override
        public String getRedoPresentationName() {
            return getName();
        }

        @Override
        public String getUndoPresentationName() {
            return getName();
        }

        protected String getName() {
            return NbBundle.getMessage(SetGradeEdit.class, "InsertProblemEdit.name");
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            EditableProblem<?> ep = etest.findProblem(problem.getId());
            if (ep != null) {
                etest.removeProblem((EditableBasket) ep);
            } else {
                final String msg = NbBundle.getMessage(SetGradeEdit.class, "InsertProblemEdit.missingEditableProblem");
                throw new MissingProblemException(msg);
            }
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            etest.updateProblem(problem);
        }
    }

    static class ProblemChildren extends Index.KeysChildren<EditableProblem> {

        private final ClassroomTestEditor2 editor;
        private final String parentId;

        @SuppressWarnings({"LeakingThisInConstructor",
            "OverridableMethodCallInConstructor"})
        ProblemChildren(ClassroomTestEditor2 ed, String parent) {
            super(ed.getEditableClassroomTest().getEditableProblems());
            this.editor = ed;
            this.parentId = parent;
            this.editor.getEditableClassroomTest().getEventBus().register(this);
        }

        @Override
        protected Node[] createNodes(EditableProblem key) {
            final ArrayList<Node> n = new ArrayList<>(1);
            if (key instanceof EditableBasket && Objects.equals(key.getParent(), parentId)) {
                final EditableBasket b = (EditableBasket) key;
                n.add(BasketNode.create(b, editor.getContext()));
            }
            return n.stream().toArray(Node[]::new);
        }

        @Override
        protected void reorder(int[] perm) {
            super.reorder(perm);
            final EditableClassroomTest etest = editor.getEditableClassroomTest();
            CollectionChangeEvent pce = new CollectionChangeEvent(etest, EditableClassroomTest.COLLECTION_PROBLEMS, null, CollectionChangeEvent.Type.REORDER);
            etest.getEventBus().post(pce);
        }

        @Subscribe
        public void onCollectionChange(CollectionChangeEvent evt) {
            if (evt.getCollectionName().equals(EditableClassroomTest.COLLECTION_PROBLEMS)) {
                update();
            }
        }

    }

    static final class StudentChildren extends Index.KeysChildren<EditableStudent> {

        private final ClassroomTestEditor2 editor;

        @SuppressWarnings({"LeakingThisInConstructor",
            "OverridableMethodCallInConstructor"})
        StudentChildren(ClassroomTestEditor2 ed) {
            super(ed.getEditableClassroomTest().getEditableStudents());
            this.editor = ed;
            this.editor.getEditableClassroomTest().getEventBus().register(this);
        }

        @Override
        protected Node[] createNodes(EditableStudent key) {
            return new Node[]{key.getNodeDelegate()};
        }

        @Subscribe
        public void onCollectionChange(CollectionChangeEvent evt) {
            if (evt.getCollectionName().equals(EditableClassroomTest.COLLECTION_STUDENTS)) {
                update();
            }
        }

    }
}
