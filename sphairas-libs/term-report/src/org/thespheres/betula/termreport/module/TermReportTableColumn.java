/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport.module;

import java.awt.Font;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.decorator.FontHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.NbBundle;
import org.thespheres.betula.Student;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.Unit;
import org.thespheres.betula.termreport.TermReport;
import org.thespheres.betula.ui.swingx.FontFaceHighlighter;
import org.thespheres.betula.ui.util.AbstractPluggableTableModel;
import org.thespheres.betula.ui.util.PluggableTableColumn;

/**
 *
 * @author boris.heithecker
 */
public abstract class TermReportTableColumn extends PluggableTableColumn<TermReport, StudentId> {

    protected TermReportTableColumn(String id, int position, boolean editable, int width) {
        super(id, position, editable, width);
    }

    static Set<PluggableTableColumn<TermReport, StudentId>> createDefaultSet() {
        HashSet<PluggableTableColumn<TermReport, StudentId>> ret = new HashSet<>();
        ret.add(new StudentColumn());
        ret.add(new AssessmentProviderColumn());
        ret.add(new NoteColumn());
        return ret;
    }

    @NbBundle.Messages({"StudentColumn.displayName=Name"})
    public static class StudentColumn extends TermReportTableColumn implements StringValue {

        private final FontHighlighter fontHighlighter = new FontFaceHighlighter(Font.BOLD, HighlightPredicate.ALWAYS);

        protected StudentColumn() {
            super("students", 100, false, 140);
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(StudentColumn.class, "StudentColumn.displayName");
        }

        @Override
        public Object getColumnValue(StudentId il) {
            return il;
        }

        @Override
        public void configureTableColumn(AbstractPluggableTableModel<TermReport, StudentId, ?, ?> model, TableColumnExt col) {
            super.configureTableColumn(model, col);
            col.addHighlighter(fontHighlighter);
            col.setCellRenderer(new DefaultTableRenderer(this));
        }

        @Override
        public String getString(Object value) {
            if (value instanceof StudentId) {
                final StudentId stud = (StudentId) value;
                Student s;
                Unit unit = context.lookup(Unit.class);
                if (unit != null && (s = unit.findStudent(stud)) != null) {
                    return s.getDirectoryName();
                }
                return Long.toString(stud.getId());
            }
            return "";
        }

    }

    @NbBundle.Messages({"NoteColumn.displayName=Bemerkung"})
    public static class NoteColumn extends TermReportTableColumn {

        NoteColumn() {
            super("notes", 30000, true, 250);
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(NoteColumn.class, "NoteColumn.displayName");
        }

        @Override
        public Object getColumnValue(StudentId il) {
            List<TermReport.Note> l = model.getNotes().get(il);
            if (l != null && !l.isEmpty()) {
                StringJoiner sj = new StringJoiner(", ");
                l.stream()
                        .map(TermReport.Note::getText)
                        .forEach(sj::add);
                return sj.toString();
            }
            return null;
        }

        @Override
        public boolean setColumnValue(StudentId il, Object value) {
            final String[] arr = StringUtils.trimToEmpty((String) value)
                    .split(",");
            final List<String> l = Arrays.stream(arr)
                    .map(StringUtils::trimToNull)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (!l.isEmpty()) {
                l.forEach(text -> model.addNote(il, text));
            } else {
                model.removeNotes(il);
            }
            return false;
        }

    }

    public static abstract class Factory extends PluggableTableColumn.Factory<PluggableTableColumn< TermReport, StudentId>> {
    }
}
