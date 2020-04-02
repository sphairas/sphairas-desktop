/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.table2;

import com.google.common.eventbus.Subscribe;
import java.beans.PropertyChangeEvent;
import org.thespheres.betula.classtest.model.LineItem;
import java.util.Set;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.thespheres.betula.classtest.model.EditableClassroomTest;
import org.thespheres.betula.classtest.model.EditableProblem;
import org.thespheres.betula.classtest.module2.ClasstestDataObject;
import org.thespheres.betula.classtest.table2.ClasstestTableModel2.ClasstestColFactory;
import org.thespheres.betula.listprint.builder.TableItem;
import org.thespheres.betula.ui.util.AbstractPluggableTableModel;
import org.thespheres.betula.ui.util.AbstractPluggableTableModel.PluggableColumnFactory;
import org.thespheres.betula.ui.util.PluggableTableColumn;
import org.thespheres.betula.util.CollectionChangeEvent;

/**
 *
 * @author boris.heithecker
 */
public class ClasstestTableModel2 extends AbstractPluggableTableModel<EditableClassroomTest<?, ?, ?>, LineItem, PluggableTableColumn<EditableClassroomTest<?, ?, ?>, LineItem>, ClasstestColFactory> implements TableItem {

    private ClasstestTableModel2(Set<? extends PluggableTableColumn<EditableClassroomTest<?, ?, ?>, LineItem>> s) {
        super("ClasstestTableModel", s);
    }

    static ClasstestTableModel2 create() {
        Set<PluggableTableColumn<EditableClassroomTest<?, ?, ?>, LineItem>> s = ClasstestColumn.createDefaultSet();
        MimeLookup.getLookup(ClasstestDataObject.CLASSTEST_MIME)
                .lookupAll(ClasstestColumn.Factory.class).stream()
                .map(ClasstestColumn.Factory::createInstance)
                .forEach(s::add);
        return new ClasstestTableModel2(s);
    }

    @Override
    public void initialize(EditableClassroomTest journal, Lookup context) {
        if (model != null) {
            model.getEventBus().unregister(this);
        }
        super.initialize(journal, context);
        model.getEventBus().register(this);
    }

    @Override
    protected ClasstestColFactory createColumnFactory() {
        return new ClasstestColFactory();
    }

    @Override
    protected int getItemSize() {
        final int size = model.getEditableStudents().size();
        return LineItem.rowCount(size);
    }

    @Override
    protected LineItem getItemAt(int row) {
        final int size = model.getEditableStudents().size();
        int student = LineItem.toModelIndex(row, size);
        final LineItem.LineType lt = LineItem.toLineType(row, size);
        if (lt != null) {
            switch (lt) {
                case PROBLEM_MAX:
                    return new LineItem(LineItem.LineType.PROBLEM_MAX);
                case PROBLEM_WEIGHT:
                    return new LineItem(LineItem.LineType.PROBLEM_WEIGHT);
                case PROBLEM_MEAN:
                    return new LineItem(LineItem.LineType.PROBLEM_MEAN);
                case STUDENTS:
                    return new LineItem(getItemsModel().getEditableStudents().get(student));
            }
        }
        return null;
    }

    public EditableProblem findEditableProblem(int tableColIndex) {
        ColumnIndex ci = getColumnsAt(tableColIndex);
        if (ci == null) {//see super
            return null;
        }
        return "problems".equals(ci.getColumn().columnId()) ? getItemsModel().getEditableProblems().get(ci.getIndexWithinColGroup()) : null;
    }

    @Subscribe
    public void onModelChange(CollectionChangeEvent event) {
        Mutex.EVENT.postWriteRequest(this::fireTableStructureChanged);
    }

    @Subscribe
    public void onPropertyChange(PropertyChangeEvent evt) {
        final String name = evt.getPropertyName();
        if (evt.getSource() instanceof EditableProblem) {
            EditableProblem ep = (EditableProblem) evt.getSource();
            if (null != name) {
                switch (name) {
                    case EditableProblem.PROP_DISPLAY_NAME:
                        onModelChange(null);
                        break;
                }
            }
        }
    }

    public class ClasstestColFactory extends PluggableColumnFactory {
    }
}
