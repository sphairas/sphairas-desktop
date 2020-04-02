/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.table2;

import com.google.common.eventbus.Subscribe;
import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.util.StringJoiner;
import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import org.jdesktop.swingx.decorator.AlignmentHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.ToolTipHighlighter;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.table.TableColumnExt;
import org.thespheres.betula.classtest.StudentScores;
import org.thespheres.betula.classtest.model.EditableBasket;
import org.thespheres.betula.classtest.model.EditableClassroomTest;
import org.thespheres.betula.classtest.model.EditableProblem;
import org.thespheres.betula.classtest.model.EditableStudent;
import org.thespheres.betula.classtest.model.LineItem;
import org.thespheres.betula.classtest.model.LineItem.LineType;
import org.thespheres.betula.ui.util.AbstractPluggableTableModel;
import org.thespheres.betula.ui.util.PluggableTableColumn;
import org.thespheres.betula.util.CollectionElementPropertyChangeEvent;

/**
 *
 * @author boris.heithecker
 */
class ProblemColumn extends PluggableTableColumn.IndexedColumn<EditableClassroomTest<?, ?, ?>, LineItem> {

    private final LocalDoubleFormatter ldf = new LocalDoubleFormatter();
    private final Highlighter centerAlignment = new AlignmentHighlighter(SwingConstants.CENTER);
    private ClasstestTableModel2 tableModel;
    private int startColumn;

    ProblemColumn() {
        super("problems", 2000, true, 40);
    }

    @Override
    public String getDisplayName(int index) {
        return model.getEditableProblems().get(index).getDisplayName();
    }

    @Override
    public int getColumnsSize() {
        return model.getEditableProblems().size();
    }

    @Override
    public Object getColumnValue(LineItem il, int index) {
        final EditableProblem p = model.getEditableProblems().get(index);
        switch (il.getLineType()) {
            case PROBLEM_MAX:
                return p.getMaxScore();
            case PROBLEM_WEIGHT:
                return p.getWeight();
            case PROBLEM_MEAN:
                return p.getMean();
            case STUDENTS:
                return il.getStudent()
                        .map(es -> es.getStudentScores().get(p.getId()))
                        .orElse(null);
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean isCellEditable(LineItem il, int index) {
        switch (il.getLineType()) {
            case PROBLEM_MEAN:
                return false;
        }
        return !model.getEditableProblems().get(index).isBasket();
    }

    @Override
    public boolean setColumnValue(LineItem il, int index, final Object value) {
        if (!(value instanceof Number)) {
            Toolkit.getDefaultToolkit().beep();
            return false;
        }
        final Number n = (Number) value;
        final EditableProblem p = model.getEditableProblems().get(index);
        switch (il.getLineType()) {
            case PROBLEM_MAX:
                p.setMaxScore(n.intValue());
                return true;
            case PROBLEM_WEIGHT:
                p.setWeight(n.doubleValue());
                return true;
            case STUDENTS:
                il.getStudent()
                        .map(EditableStudent::getStudentScores)
                        .ifPresent(ss -> ss.put(p.getId(), n.doubleValue()));
                return false;
        }
        throw new IllegalStateException();
    }

    @Override
    public void configureTableColumn(AbstractPluggableTableModel<EditableClassroomTest<?, ?, ?>, LineItem, ?, ?> tm, TableColumnExt col) {
        super.configureTableColumn(tm, col);
        tableModel = (ClasstestTableModel2) tm;
        startColumn = (int) col.getClientProperty(AbstractPluggableTableModel.PROP_COLUMNS_START);
        final int index = (int) col.getClientProperty(AbstractPluggableTableModel.PROP_COLUMNS_INDEX);
        col.setToolTipText(getDisplayName(index));
        col.addHighlighter(centerAlignment);
        col.addHighlighter(new ProblemToolTip(model.getEditableProblems().get(index)));
        final JFormattedTextField tfield = new JFormattedTextField(ldf);
        tfield.setBorder(new LineBorder(Color.black, 2));
        tfield.setHorizontalAlignment(JTextField.CENTER);
        final class CellEditor extends DefaultCellEditor {

            @SuppressWarnings({"OverridableMethodCallInConstructor"})
            private CellEditor() {
                super(tfield);
                setClickCountToStart(1);
                tfield.removeActionListener(delegate);
                delegate = new DefaultCellEditor.EditorDelegate() {
                    @Override
                    public void setValue(Object value) {
                        tfield.setValue(value);
                    }

                    @Override
                    public Object getCellEditorValue() {
                        return tfield.getValue();
                    }
                };
                tfield.addActionListener(delegate);
            }
        }
        col.setCellEditor(new CellEditor());
        col.setCellRenderer(new DefaultTableRenderer(ldf));
    }

    private void cellUpdated(int row, int ci) {
        if (tableModel != null) {
            tableModel.updateCellValue(row, startColumn + ci);
        }
    }

    @Subscribe
    public void onPropertyChange(PropertyChangeEvent evt) {
        final String name = evt.getPropertyName();
//        if (evt.getSource() instanceof EditableStudent) {
//            EditableStudent es = (EditableStudent) evt.getSource();
//            if (EditableStudent.PROP_GRADE.equals(name) || EditableStudent.PROP_AUTODISTRIBUTING.equals(name)) {
//                update(getFields().getField(ClassroomTestFields.STUDENT_GRADE_COLUMN, ClassroomTestFields.STUDENT_ROWS, 0, es.getColumnIndex()));
//            }
//        } else 
        if (evt.getSource() instanceof EditableProblem && name != null) {
            EditableProblem ep = (EditableProblem) evt.getSource();
            final int size = model.getEditableStudents().size();
            switch (name) {
                case EditableProblem.PROP_MAXSCORE:
                    cellUpdated(LineItem.toRowIndex(LineType.PROBLEM_MAX, 0, size), ep.getIndex());
                    break;
                case EditableProblem.PROP_WEIGHT:
                    cellUpdated(LineItem.toRowIndex(LineType.PROBLEM_WEIGHT, 0, size), ep.getIndex());
                    break;
            }
        }
    }

    @Subscribe
    public void studentScoresChange(CollectionElementPropertyChangeEvent event) {
        if (event.getSource() instanceof EditableStudent) {
            EditableStudent es = (EditableStudent) event.getSource();
//            update(getFields().getField(ClassroomTestFields.MEAN_ROW, ClassroomTestFields.STUDENT_SCORES_MEAN_COLUMN, 0, 0));
//            update(getFields().getField(ClassroomTestFields.STUDENT_ROWS, ClassroomTestFields.STUDENT_SCORESUM_COLUMN, 0, es.getColumnIndex()));
            EditableProblem ep = model.findProblem(event.getElementKey());
            if (ep != null) {
                //                update(getFields().getField(ClassroomTestFields.MEAN_ROW, ClassroomTestFields.MEAN_COLUMNS, ep.getColumnIndex(), 0));
                cellUpdated(LineItem.toRowIndex(LineType.STUDENTS, es.getIndex(), model.getEditableStudents().size()), ep.getIndex());
            }
        }
    }

    class ProblemToolTip extends ToolTipHighlighter {

        private final EditableBasket<?> problem;

        @SuppressWarnings({"LeakingThisInConstructor",
            "OverridableMethodCallInConstructor"})
        private ProblemToolTip(EditableBasket<?> problem) {
            this.problem = problem;
            setHighlightPredicate(HighlightPredicate.ALWAYS);
        }

        @Override
        protected Component doHighlight(Component component, ComponentAdapter adapter) {
            final StringJoiner sj = new StringJoiner("<br>", "<html>", "</html>");
            if (initialized) {
                int ri = adapter.convertRowIndexToModel(adapter.row);
                final int size = getModel().getEditableStudents().size();
                final int student = LineItem.toModelIndex(ri, size);
                final LineType lt = LineItem.toLineType(ri, size);
                if (lt != null && lt.equals(LineType.STUDENTS) && student != -1) {
                    final EditableStudent<? extends StudentScores> es = getModel().getEditableStudents().get(student);
                    sj.add(es.getStudent().getDirectoryName());
                    sj.add(problem.getDisplayName());
                }
            }
            ((JComponent) component).setToolTipText(sj.toString());
            return component;
        }
    }
}
