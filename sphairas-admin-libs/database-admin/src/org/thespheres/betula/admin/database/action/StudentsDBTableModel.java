/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.database.action;

import java.awt.EventQueue;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.openide.util.Lookup;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.admin.database.action.StudentsDBTableModel.StudentsDBColFactory;
import org.thespheres.betula.services.vcard.VCardStudent;
import org.thespheres.betula.ui.util.AbstractPluggableTableModel;
import org.thespheres.betula.ui.util.AbstractPluggableTableModel.PluggableColumnFactory;
import org.thespheres.betula.ui.util.PluggableTableColumn;
import org.thespheres.betula.util.StudentComparator;
import org.thespheres.betula.services.ui.util.dav.VCardStudents;

/**
 *
 * @author boris.heithecker
 */
public class StudentsDBTableModel extends AbstractPluggableTableModel<VCardStudents, VCardStudent, PluggableTableColumn<VCardStudents, VCardStudent>, StudentsDBColFactory> {

    private final List<StudentId> order = new ArrayList<>();
    static final Collator COLLATOR = Collator.getInstance(Locale.GERMANY);
    private final Listener listener = new Listener();

    private StudentsDBTableModel(Set<? extends PluggableTableColumn<VCardStudents, VCardStudent>> s) {
        super("StudentsDBTableModel", s);
    }

    static StudentsDBTableModel create() {
        Set<PluggableTableColumn<VCardStudents, VCardStudent>> s = StudentsDBTableColumn.createDefaultSet();
        MimeLookup.getLookup(StudentsDBOpenSupport.STUDENTSDB_MIME)
                .lookupAll(StudentsDBTableColumn.Factory.class).stream()
                .map(StudentsDBTableColumn.Factory::createInstance)
                .forEach(s::add);
        return new StudentsDBTableModel(s);
    }

    @Override
    public void initialize(VCardStudents report, Lookup context) {
        if (model != null) {
            model.getLoadTask().removeTaskListener(listener);
        }
        super.initialize(report, context);
        model.getLoadTask().addTaskListener(listener);
        if (model.getLoadTask().isFinished()) {
            listener.taskFinished(null);
        }
    }

    private void updateStudents(final List<StudentId> update) {
        assert EventQueue.isDispatchThread();
        order.clear();
        update.forEach(order::add);
        fireTableStructureChanged();
    }

    @Override
    protected StudentsDBColFactory createColumnFactory() {
        return new StudentsDBColFactory();
    }

    @Override
    protected int getItemSize() {
        return model.getStudents().size();
    }

    @Override
    protected VCardStudent getItemAt(int row) {
        return model.find(order.get(row));
    }

    class Listener implements TaskListener {

        @Override
        public void taskFinished(final Task task) {
            final List<StudentId> update = model.getStudents().stream()
                    .sorted(Comparator.comparing(l -> StudentComparator.sortStringFromDirectoryName(l.getDirectoryName()), COLLATOR))
                    .map(v -> v.getStudentId())
                    .collect(Collectors.toList());
            EventQueue.invokeLater(() -> updateStudents(update));
        }

    }

    class StudentsDBColFactory extends PluggableColumnFactory {
    }
}
