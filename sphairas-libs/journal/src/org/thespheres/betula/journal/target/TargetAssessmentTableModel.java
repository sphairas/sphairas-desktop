/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.target;

import com.google.common.eventbus.Subscribe;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.TableColumnExt;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.openide.util.Lookup;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.journal.model.EditableJournal;
import org.thespheres.betula.journal.model.EditableParticipant;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.assess.TargetAssessment;
import org.thespheres.betula.journal.model.JournalEditor;
import org.thespheres.betula.journal.module.Constants;
import org.thespheres.betula.journal.target.TargetAssessmentTableModel.TargetColFactory;
import org.thespheres.betula.listprint.builder.TableItem;
import org.thespheres.betula.ui.util.AbstractPluggableTableModel;
import org.thespheres.betula.ui.util.AbstractPluggableTableModel.PluggableColumnFactory;
import org.thespheres.betula.ui.util.PluggableTableColumn;
import org.thespheres.betula.util.CollectionChangeEvent;
import org.thespheres.betula.util.CollectionChangeEvent.Type;

/**
 *
 * @author boris.heithecker
 */
class TargetAssessmentTableModel extends AbstractPluggableTableModel<EditableJournal<?, ?>, EditableParticipant, PluggableTableColumn<EditableJournal<?, ?>, EditableParticipant>, TargetColFactory> implements TableItem, TargetAssessment.Listener<Grade> {

//    private EditableJournal<?, ?> journal;
    private TargetAssessment<Grade, TargetAssessment.Listener<Grade>> target;
//    private final NumberFormat nf;
//    private AssessmentConvention currentJournalConvention;
//    private JournalEditor editor;
    static final String CLIENT_PROP_GRADE = "grade";

    TargetAssessmentTableModel(final Set<PluggableTableColumn<EditableJournal<?, ?>, EditableParticipant>> s) {
        super("TargetTableModel", s);
//        nf = NumberFormat.getNumberInstance(Locale.getDefault());
//        nf.setMaximumFractionDigits(2);
//        nf.setMinimumFractionDigits(0);
    }

    static TargetAssessmentTableModel create() {
        final Set<PluggableTableColumn<EditableJournal<?, ?>, EditableParticipant>> s = TargetDefaultColumns.create();
        MimeLookup.getLookup(Constants.JOURNAL_TARGETTABLE_MIME)
                .lookupAll(PluggableTableColumn.Factory.class).stream()
                .map(PluggableTableColumn.Factory::createInstance)
                .forEach(s::add);
        return new TargetAssessmentTableModel(s);
    }

    @Override
    protected TargetColFactory createColumnFactory() {
        return new TargetColFactory();
    }

    @Override
    protected int getItemSize() {
        return model.getEditableParticipants().size();
    }

    @Override
    protected EditableParticipant getItemAt(int row) {
        return model.getEditableParticipants().get(row);
    }

//    public EditableJournal getEditableJournal() {
//        return model;
//    }
//    public AssessmentConvention getCurrentJournalConvention() {
//        return currentJournalConvention;
//    }
//    public TargetAssessment<Grade, TargetAssessment.Listener<Grade>> getTargetAssessment() {
//        return target;
//    }
    @Override
    public synchronized void initialize(EditableJournal<?, ?> ej, Lookup context) {
        final EditableJournal old = this.model;
        super.initialize(ej, context);
        final TargetAssessment ta = context.lookup(TargetAssessment.class);
        final TargetAssessment taold = this.target;
//        boolean ch = false;
        if (!Objects.equals(model, old)) {
            if (old != null) {
                old.getEventBus().unregister(this);
            }
//            this.editor = context.lookup(JournalEditor.class);
//            String pc = JournalConfiguration.getInstance().getJournalEntryPreferredConvention();
//            if (pc != null) {
//                currentJournalConvention = GradeFactory.findConvention(pc);
//            }
            model.getEventBus().register(this);
//            ch = true;
        }
        if (!Objects.equals(ta, taold)) {
            if (taold != null) {
                taold.removeListener(this);
            }
            this.target = ta;
            this.target.addListener(this);
//            ch = true;
        }
//        if (ch) {
//            fireTableStructureChanged();
//        }
    }

    void initialize(JournalEditor currentEditor) {
//        EditableJournal ej = currentEditor.getEditableJournal();
//        TargetAssessment ta = currentEditor.getLookup().lookup(TargetAssessment.class);
//        EditableJournal old = this.journal;
//        TargetAssessment taold = this.target;
//        boolean ch = false;
//        if (!Objects.equals(ej, old)) {
//            if (this.journal != null) {
//                this.journal.getEventBus().unregister(this);
//            }
//            this.journal = ej;
//            this.editor = currentEditor;
//            String pc = JournalConfiguration.getInstance().getJournalEntryPreferredConvention();
//            if (pc != null) {
//                currentJournalConvention = GradeFactory.findConvention(pc);
//            }
//            this.journal.getEventBus().register(this);
//            ch = true;
//        }
//        if (!Objects.equals(ta, taold)) {
//            if (taold != null) {
//                taold.removeListener(this);
//            }
//            this.target = ta;
//            this.target.addListener(this);
//            ch = true;
//        }
//        if (ch) {
//            fireTableStructureChanged();
//        }
    }

//    @Override
//    public int getRowCount() {
//        return journal != null ? journal.getEditableParticipants().size() : 0;
//    }
//    @Override
//    public int getColumnCount() {
//        int ret = 0;
//        if (journal != null) {
//            ret = 3;
//            if (getCurrentJournalConvention() != null) {
//                ret += getCurrentJournalConvention().getAllGrades().length;
//            }
//        }
//        return ret;
//    }
//    @Override
//    public Object getValueAt(int rowIndex, int columnIndex) {
//        if (columnIndex == 0) {
//            return journal.getEditableParticipants().get(rowIndex);
//        } else if (columnIndex == getColumnCount() - 2) {
//            return scoreSum(rowIndex);
//        } else if (columnIndex == getColumnCount() - 1) {
//            return grade(rowIndex);
//        } else {
//            return score(columnIndex - 1, rowIndex);
//        }
//    }
//    private Long score(int col, int row) {
//        if (getCurrentJournalConvention() != null) {
//            Grade g = getCurrentJournalConvention().getAllGrades()[col];
//            long count = journal.getEditableParticipants().get(row).getGradeCount(g);
//            return count == 0l ? null : count;
//        }
//        return null;
//    }
//
//    private String scoreSum(int row) {
//        double mean = journal.getEditableParticipants().get(row).getWeightedGradesMean().getWeightedMean();
//        return nf.format(mean);
//    }
//
//    private Grade grade(int index) {
//        if (getTargetAssessment() != null) {
//            EditableParticipant p = journal.getEditableParticipants().get(index);
//            return getTargetAssessment().select(p.getStudentId());
//        }
//        return null;
//    }
//    @Override
//    public void setValueAt(Object val, int rowIndex, int columnIndex) {
//        final Grade g = (Grade) val;
//        final TargetAssessment<Grade, TargetAssessment.Listener<Grade>> ta;
//        if ((ta = getTargetAssessment()) != null) {
//            final StudentId s = journal.getEditableParticipants().get(rowIndex).getStudentId();
//            final Grade old = ta.select(s);
//            final GradeEdit edit = new GradeEdit(s, old, g);
//            ta.submit(s, g, Timestamp.now());
//            editor.getUndoSupport().postEdit(edit);
//        }
//    }
//
//    @Override
//    public boolean isCellEditable(int rowIndex, int columnIndex) {
//        return columnIndex == getColumnCount() - 1 && target != null;
//    }
    @Subscribe
    public void onModelChange(CollectionChangeEvent event) {
        if (EditableJournal.COLLECTION_PARTICIPANTS.equals(event.getCollectionName())) {
            event.getItemAs(EditableParticipant.class).ifPresent(student -> {
                final int line = student.getIndex();
                if (event.getType().equals(Type.REORDER)) {
                    fireTableRowsUpdated(0, getRowCount() - 1);
                } else {
                    fireTableRowsInserted(line, line);
                }
            });
        }
    }

    @Override
    public void valueForStudentChanged(Object source, StudentId student, Grade old, Grade newGrade, Timestamp timestamp) {
        if (getItemsModel() != null) {
            final EditableParticipant p = getItemsModel().findParticipant(student);
            if (p != null) {
                final int r = p.getIndex();
                try {
                    fireTableRowsUpdated(r, r);
                } catch (IndexOutOfBoundsException e) {
                    fireTableStructureChanged();
                }
            }
        }
    }

    public class TargetColFactory extends PluggableColumnFactory {

        @Override
        public void initialize(Object ecal, Lookup context) {
//            if (taResult != null) {
//                taResult.removeLookupListener(this);
//            }
//            taResult = currentEditor.getLookup().lookupResult(TargetAssessment.class);
//            updateTarget();
//            taResult.addLookupListener(this);
        }

//        protected JournalTableModel model(JXTable t) {
//            return model(t.getModel());
//        }
//
//        protected JournalTableModel model(TableModel t) {
//            return (JournalTableModel) t;
//        }
        @Override
        protected TableCellRenderer getHeaderRenderer(final JXTable table, TableColumnExt col) {
            final TargetAssessmentTableModel m = (TargetAssessmentTableModel) table.getModel();
            final int index = col.getModelIndex();
            if (index != 0 && index < m.getColumnCount() - 2) {

                return (JTable table1, Object value, boolean isSelected, boolean hasFocus, int row, int column) -> {
                    DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) table1.getTableHeader().getDefaultRenderer();
                    renderer.setHorizontalAlignment(JLabel.CENTER);
                    return renderer.getTableCellRendererComponent(table1, value, isSelected, hasFocus, row, column);
                };
            }
            return super.getHeaderRenderer(table, col);
        }

    }

//    class GradeEdit extends AbstractUndoableEdit {
//
//        private final StudentId student;
//        private final Grade old;
//        private final Grade grade;
//
//        private GradeEdit(StudentId s, Grade old, Grade g) {
//            this.student = s;
//            this.old = old;
//            this.grade = g;
//        }
//
//        @Override
//        public void redo() throws CannotRedoException {
//            super.redo();
//            final TargetAssessment ta;
//            if ((ta = getTargetAssessment()) != null) {
//                ta.submit(student, grade, Timestamp.now());
//            } else {
//                throw new CannotRedoException();
//            }
//        }
//
//        @Override
//        public void undo() throws CannotUndoException {
//            super.undo();
//            final TargetAssessment ta;
//            if ((ta = getTargetAssessment()) != null) {
//                ta.submit(student, old, Timestamp.now());
//            } else {
//                throw new CannotUndoException();
//            }
//        }
//
//    }
    public static class GradeEdit2 extends AbstractUndoableEdit {

        private final StudentId student;
        private final Grade old;
        private final Grade grade;
        private final WeakReference<TargetAssessment<Grade, TargetAssessment.Listener<Grade>>> target;

        public GradeEdit2(StudentId s, Grade old, Grade g, TargetAssessment<Grade, TargetAssessment.Listener<Grade>> target) {
            this.student = s;
            this.old = old;
            this.grade = g;
            this.target = new WeakReference<>(target);
        }

        private TargetAssessment<Grade, TargetAssessment.Listener<Grade>> getTargetAssessment() {
            return target.get();
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            final TargetAssessment ta;
            if ((ta = getTargetAssessment()) != null) {
                ta.submit(student, grade, Timestamp.now());
            } else {
                throw new CannotRedoException();
            }
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            final TargetAssessment ta;
            if ((ta = getTargetAssessment()) != null) {
                ta.submit(student, old, Timestamp.now());
            } else {
                throw new CannotUndoException();
            }
        }

    }
}
