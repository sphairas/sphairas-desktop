/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.table2;

import com.google.common.eventbus.Subscribe;
import java.beans.PropertyChangeEvent;
import javax.swing.DefaultCellEditor;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.classtest.model.ClassroomTestEditor2;
import org.thespheres.betula.classtest.model.EditableClassroomTest;
import org.thespheres.betula.classtest.model.EditableStudent;
import org.thespheres.betula.classtest.model.LineItem;
import org.thespheres.betula.classtest.model.LineItem.LineType;
import org.thespheres.betula.classtest.module2.ClasstestConfiguration;
import org.thespheres.betula.ui.GradeComboBoxModel;
import org.thespheres.betula.ui.util.AbstractPluggableTableModel;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"GradesColumn.displayName=Zensur"})
public class GradesColumn extends ClasstestColumn implements LookupListener {

    private final GradeOrNumberStringValue gradeColFormat = new GradeOrNumberStringValue();
    private JXComboBox gradesBox;
    private final GradeComboBoxModel gradeBoxModel;
    private Lookup.Result<AssessmentConvention> result;

    GradesColumn() {
        super("grades", 10000, true, 85);
        gradeBoxModel = new GradeComboBoxModel();
        gradeBoxModel.setUseLongLabel(ClasstestConfiguration.useLongLabel());
        //don't create outside EDT before GUI ready, may couse laf exception in nimbus
//        gradesBox = new JXComboBox();
    }

    @Override
    public void initialize(EditableClassroomTest ecal, Lookup context) {
        if (result != null) {
            result.removeLookupListener(this);
        }
        super.initialize(ecal, context);
//        ClassroomTestEditor2 editor = context.lookup(ClassroomTestEditor2.class);
//        if (editor != null) {
//            result = editor.getContext().lookupResult(AssessmentConvention.class);            
        result = context.lookupResult(AssessmentConvention.class);
        result.addLookupListener(this);
//        }
        resultChanged(null);
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(NoteColumn.class, "GradesColumn.displayName");
    }

    @Override
    public Object getColumnValue(LineItem il) {
        return il.getStudent()
                .map(es -> es.getStudentScores().getGrade())
                .orElse(null);
    }

    @Override
    public boolean setColumnValue(LineItem il, Object value) {
        ClassroomTestEditor2 editor = context.lookup(ClassroomTestEditor2.class);
        il.getStudent()
                .ifPresent(es -> editor.setGradeAt(es, (Grade) value));
        return false;
    }

    @Override
    public void configureTableColumn(AbstractPluggableTableModel<EditableClassroomTest<?, ?, ?>, LineItem, ?, ?> model, TableColumnExt col) {
        super.configureTableColumn(model, col);
        //do not initialize gradesBox in constructor, needs to be initialized in EDT
        gradesBox = new JXComboBox();
        gradesBox.setModel(gradeBoxModel);
        gradesBox.setEditable(false);
        gradesBox.setRenderer(new DefaultListRenderer(gradeBoxModel));
        gradeBoxModel.initialize(gradesBox);
//        col.setHeaderValue(NbBundle.getMessage(ClasstestColumnFactory.class, "classtest.table.grade.col"));
        col.setCellRenderer(new DefaultTableRenderer(gradeColFormat));
        col.setCellEditor(new DefaultCellEditor(gradesBox));
        resultChanged(null);
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        ClassroomTestEditor2 editor = context.lookup(ClassroomTestEditor2.class);
        if (editor != null) {
            final AssessmentConvention con = editor.getAssessmentConvention();
            Mutex.EVENT.postWriteRequest(() -> {
                editable = con != null;
                gradeBoxModel.setPreferredConvention(con == null ? null : con.getName());
            });
        }
    }

    @Subscribe
    public void onPropertyChange(PropertyChangeEvent evt) {
        final String name = evt.getPropertyName();
        if (evt.getSource() instanceof EditableStudent) {
            EditableStudent es = (EditableStudent) evt.getSource();
            if (EditableStudent.PROP_GRADE.equals(name) || EditableStudent.PROP_AUTODISTRIBUTING.equals(name)) {
                cellUpdated(LineItem.toRowIndex(LineType.STUDENTS, es.getIndex(), model.getEditableStudents().size()));
            }
        }
    }
}
