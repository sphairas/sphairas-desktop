/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.database.action;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.NbBundle;
import org.thespheres.betula.services.vcard.VCardStudent;
import org.thespheres.betula.ui.util.AbstractPluggableTableModel;
import org.thespheres.betula.ui.util.PluggableTableColumn;
import org.thespheres.betula.util.StudentComparator;
import org.thespheres.betula.services.ui.util.dav.VCardStudents;

/**
 *
 * @author boris.heithecker
 */
public abstract class StudentsDBTableColumn extends PluggableTableColumn<VCardStudents, VCardStudent> {

    protected StudentsDBTableColumn(String id, int position, boolean editable, int width) {
        super(id, position, editable, width);
    }

    static Set<PluggableTableColumn<VCardStudents, VCardStudent>> createDefaultSet() {
        final HashSet<PluggableTableColumn<VCardStudents, VCardStudent>> ret = new HashSet<>();
        ret.add(new StudentColumn());
        ret.add(new IDColumn());
        return ret;
    }

    @NbBundle.Messages({"StudentColumn.displayName=Name"})
    public static class StudentColumn extends StudentsDBTableColumn {

        protected StudentColumn() {
            super("student", 100, false, 250);
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(StudentColumn.class, "StudentColumn.displayName");
        }

        @Override
        public Object getColumnValue(VCardStudent il) {
            return il.getDirectoryName();
        }

        @Override
        public void configureTableColumn(AbstractPluggableTableModel<VCardStudents, VCardStudent, ?, ?> model, TableColumnExt col) {
            super.configureTableColumn(model, col);
            col.setComparator(Comparator.comparing(l -> StudentComparator.sortStringFromDirectoryName(l.toString()), StudentsDBTableModel.COLLATOR));
        }

    }

    @NbBundle.Messages({"IDColumn.displayName=ID"})
    public static class IDColumn extends StudentsDBTableColumn {

        IDColumn() {
            super("id", 200, false, 250);
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(IDColumn.class, "IDColumn.displayName");
        }

        @Override
        public Object getColumnValue(VCardStudent il) {
            return Long.toString(il.getStudentId().getId());
        }

    }

    public static abstract class Factory extends PluggableTableColumn.Factory<PluggableTableColumn< VCardStudents, VCardStudent>> {
    }
}
