/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.table2;

import java.awt.Font;
import java.util.HashSet;
import java.util.Set;
import org.jdesktop.swingx.decorator.FontHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.thespheres.betula.classtest.model.EditableClassroomTest;
import org.thespheres.betula.classtest.model.EditableStudent;
import org.thespheres.betula.classtest.model.LineItem;
import org.thespheres.betula.classtest.model.LineItem.LineType;
import org.thespheres.betula.ui.swingx.FontFaceHighlighter;
import org.thespheres.betula.ui.util.AbstractPluggableTableModel;
import org.thespheres.betula.ui.util.PluggableTableColumn;

/**
 *
 * @author boris.heithecker
 */
public abstract class ClasstestColumn extends PluggableTableColumn<EditableClassroomTest<?, ?, ?>, LineItem> {

    private ClasstestTableModel2 tableModel;
    private int modelIndex;

    protected ClasstestColumn(String id, int position, boolean editable, int width) {
        super(id, position, editable, width);
    }

    static Set<PluggableTableColumn<EditableClassroomTest<?, ?, ?>, LineItem>> createDefaultSet() {
        final HashSet<PluggableTableColumn<EditableClassroomTest<?, ?, ?>, LineItem>> ret = new HashSet<>();
        ret.add(new StudentColumn());
        ret.add(new ProblemColumn());
        ret.add(new ScoreSumColumn());
        ret.add(new GradesColumn());
        ret.add(new NoteColumn());
        return ret;
    }

    @Override
    public boolean isCellEditable(final LineItem il) {
        if (il.getLineType().equals(LineType.STUDENTS)) {
            return super.isCellEditable(il);
        } else {
            return false;
        }
    }

    @Override
    public void initialize(EditableClassroomTest ecal, Lookup context) {
        if (model != null) {
            model.getEventBus().unregister(this);
        }
        super.initialize(ecal, context);
        model.getEventBus().register(this);
    }

    @Override
    public void configureTableColumn(AbstractPluggableTableModel<EditableClassroomTest<?, ?, ?>, LineItem, ?, ?> model, TableColumnExt col) {
        tableModel = (ClasstestTableModel2) model;
        modelIndex = col.getModelIndex();
    }

    protected void cellUpdated(int row) {
        if (tableModel != null) {
            tableModel.updateCellValue(row, modelIndex);
        }
    }

    @NbBundle.Messages({"StudentColumn.displayName=Name"})
    public static class StudentColumn extends PluggableTableColumn<EditableClassroomTest<?, ?, ?>, LineItem> {

        private final FontHighlighter fontHighlighter = new FontFaceHighlighter(Font.BOLD, HighlightPredicate.ALWAYS);
        private final StringValue studentsStringValue = v -> v instanceof EditableStudent ? ((EditableStudent) v).getStudent().getDirectoryName() : "";

        protected StudentColumn() {
            super("students", 100, false, 140);
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(StudentColumn.class, "StudentColumn.displayName");
        }

        @Override
        public Object getColumnValue(LineItem il) {
            return il.getStudent().orElse(null);
        }

        @Override
        public void configureTableColumn(AbstractPluggableTableModel<EditableClassroomTest<?, ?, ?>, LineItem, ?, ?> model, TableColumnExt col) {
            super.configureTableColumn(model, col);
            col.addHighlighter(fontHighlighter);
            col.setCellRenderer(new DefaultTableRenderer(studentsStringValue));
        }

    }

    @NbBundle.Messages({"NoteColumn.displayName=Bemerkung"})
    public static class NoteColumn extends ClasstestColumn {

        NoteColumn() {
            super("notes", 30000, true, 250);
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(NoteColumn.class, "NoteColumn.displayName");
        }

        @Override
        public Object getColumnValue(LineItem il) {
            return il.getStudent()
                    .map(es -> es.getStudentScores().getNote())
                    .orElse(null);
        }

        @Override
        public boolean setColumnValue(LineItem il, Object value) {
            il.getStudent()
                    .ifPresent(es -> es.getStudentScores().setNote((String) value));
            return false;
        }

    }

    public static abstract class Factory extends PluggableTableColumn.Factory<PluggableTableColumn<EditableClassroomTest<?, ?, ?>, LineItem>> {
    }
}
