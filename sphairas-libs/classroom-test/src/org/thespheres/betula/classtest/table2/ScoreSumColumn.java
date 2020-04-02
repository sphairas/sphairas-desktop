/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.table2;

import com.google.common.eventbus.Subscribe;
import java.beans.PropertyChangeEvent;
import javax.swing.SwingConstants;
import org.jdesktop.swingx.decorator.AlignmentHighlighter;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.NbBundle;
import org.thespheres.betula.classtest.model.EditableClassroomTest;
import org.thespheres.betula.classtest.model.EditableProblem;
import org.thespheres.betula.classtest.model.EditableStudent;
import org.thespheres.betula.classtest.model.LineItem;
import org.thespheres.betula.classtest.model.LineItem.LineType;
import org.thespheres.betula.ui.util.AbstractPluggableTableModel;
import org.thespheres.betula.util.CollectionElementPropertyChangeEvent;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages(value = {"ScoreSumColumn.displayName=Summe"})
public class ScoreSumColumn extends ClasstestColumn {

    private final LocalDoubleFormatter ldf = new LocalDoubleFormatter();
    private final Highlighter centerAlignment = new AlignmentHighlighter(SwingConstants.CENTER);

    protected ScoreSumColumn() {
        super("scores", 9000, false, 40);
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(NoteColumn.class, "ScoreSumColumn.displayName");
    }

    @Override
    public Object getColumnValue(LineItem il) {
        if (il.getLineType().equals(LineType.PROBLEM_WEIGHT)) {
            return model.getAllProblemsWeightedMaximumSum();
        }
        return il.getStudent().map((es) -> es.getStudentScores().sum()).orElse(null);
    }

    @Override
    public void configureTableColumn(AbstractPluggableTableModel<EditableClassroomTest<?, ?, ?>, LineItem, ?, ?> model, TableColumnExt col) {
        super.configureTableColumn(model, col);
        col.addHighlighter(centerAlignment);
        col.setCellRenderer(new DefaultTableRenderer(ldf));
    }

    @Subscribe
    public void onPropertyChange(PropertyChangeEvent evt) {
        final String name = evt.getPropertyName();
        if (evt.getSource() instanceof EditableProblem
                && (name.equals(EditableProblem.PROP_MAXSCORE) || name.equals(EditableProblem.PROP_WEIGHT))) {
            final int size = model.getEditableStudents().size();
            cellUpdated(LineItem.toRowIndex(LineItem.LineType.PROBLEM_WEIGHT, 0, size));
        }
    }

    @Subscribe
    public void studentScoresChange(CollectionElementPropertyChangeEvent event) {
        if (event.getSource() instanceof EditableStudent) {
            EditableStudent es = (EditableStudent) event.getSource();
            final int size = model.getEditableStudents().size();
            cellUpdated(LineItem.toRowIndex(LineType.STUDENTS, es.getIndex(), size));
            cellUpdated(LineItem.toRowIndex(LineType.PROBLEM_MEAN, 0, size));
        }
    }

}
