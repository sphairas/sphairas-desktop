/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.noten.ui;

import com.google.common.eventbus.Subscribe;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;
import org.openide.util.WeakListeners;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.assess.AssessmentContext;
import org.thespheres.betula.classtest.model.EditableClassroomTest;
import org.thespheres.betula.classtest.model.EditableStudent;
import org.thespheres.betula.util.CollectionChangeEvent;
import org.thespheres.betula.util.CollectionElementPropertyChangeEvent;
import org.thespheres.betula.util.Int2;
import org.thespheres.betula.assess.AssessorMapEntry;
import org.thespheres.betula.classtest.model.ClassroomTestEditor2;
import org.thespheres.betula.classtest.model.EditableProblem;

/**
 *
 * @author boris.heithecker
 */
public class SyncContext {

    private EditableClassroomTest<?, ?, ?> etest;
    private final WeakReference<AssessmentContext> context;
    private final WeakHashMap<EditableStudent, ValueChangeListener> entryMap = new WeakHashMap<>();

    @SuppressWarnings({"LeakingThisInConstructor"})
    private SyncContext(final EditableClassroomTest et, final AssessmentContext ctx) {
        this.etest = et;
        this.context = new WeakReference<>(ctx);
        etest.getEventBus().register(this);
    }

    private AssessmentContext getAssessmentContext() {
        return context.get();
    }

    public static void attach(final AssessmentContext ctx, final ClassroomTestEditor2 editor) {
        final EditableClassroomTest etest = editor.getEditableClassroomTest();
        final double ws = etest.getAllProblemsWeightedMaximumSum();
        ctx.setRangeMaximum(new Int2(ws)); //Hier liegt das Problem
        final SyncContext sync = new SyncContext(etest, ctx);
        sync.initListeners();
        final PropertyChangeListener pcl = WeakListeners.propertyChange(e -> editor.getDataObject().setModified(true), ctx);
        ctx.addPropertyChangeListener(pcl);
    }

    private void die() {
        etest.getEventBus().unregister(this);
        etest = null;
        entryMap.clear();
    }

    private void initListeners() {
        final AssessmentContext ac = getAssessmentContext();
        if (ac != null) {
            etest.getEditableStudents().stream()
                    .forEach(es -> initSync(es, ac));
        } else {
            die();
        }
    }

    AssessorMapEntry initSync(EditableStudent<?> es, AssessmentContext<StudentId, Int2> ctx) {
        final Double s = es.getStudentScores().sum();
        final AssessorMapEntry entry = ctx.getAssessorList().createAndAdd(es.getStudentId(), new Int2(s));
        final ValueChangeListener listener = new ValueChangeListener(es, entry);
        entryMap.put(es, listener);
        entry.addPropertyChangeListener(listener);
        return entry;
    }

    @Subscribe
    public void contextRelatedCollection(PropertyChangeEvent evt) {
        final String name = evt.getPropertyName();
        if (EditableClassroomTest.PROP_PROBLEMSWEIGHTEDMAXIMUM.equals(name)) {
            updateRangeMax();
        }
    }

    private void updateRangeMax() {
        final AssessmentContext ac = getAssessmentContext();
        if (ac != null) {
            final double d = etest.getAllProblemsWeightedMaximumSum();
            final Int2 v = new Int2(d);
            ac.setRangeMaximum(v);
        } else {
            die();
        }
    }

    @Subscribe
    public void studentProperty(CollectionElementPropertyChangeEvent evt) {
        if (getAssessmentContext() == null) {
            die();
        } else if (EditableStudent.PROP_SCORES.equals(evt.getPropertyName()) || evt.getSource() instanceof EditableStudent) {
            EditableStudent es = (EditableStudent) evt.getSource();
            ValueChangeListener e = entryMap.get(es);
            if (e != null) {
                Int2 v = new Int2(es.getStudentScores().sum());
                e.entry.setValue(v);
            }
        }
    }

    @Subscribe
    public void onModelChange(CollectionChangeEvent event) {
        final AssessmentContext ac = getAssessmentContext();
        if (ac == null) {
            die();
        } else if (EditableClassroomTest.COLLECTION_STUDENTS.equals(event.getCollectionName())) {
            event.getItemAs(EditableStudent.class).ifPresent(student -> {
                if (event.getType().equals(CollectionChangeEvent.Type.ADD)) {
                    final AssessorMapEntry entry = initSync(student, ac);
                    student.getStudentScores().setGrade(entry.getGrade());
                } else if (event.getType().equals(CollectionChangeEvent.Type.REMOVE)) {
                    ac.getAssessorList().remove(student.getStudentId());
                    entryMap.remove(student);
                }
            });
            event.getItemAs(EditableProblem.class).ifPresent(p -> updateRangeMax());
        }
    }

    class ValueChangeListener implements PropertyChangeListener {

        private final WeakReference<EditableStudent> student;
        private final AssessorMapEntry entry;

        ValueChangeListener(EditableStudent student, AssessorMapEntry entry) {
            this.student = new WeakReference<>(student);
            this.entry = entry;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (getAssessmentContext() == null) {
                die();
            } else if (evt.getPropertyName().equals(AssessorMapEntry.PROP_GRADE)) {
                EditableStudent es = student.get();
                if (es != null && es.getStudentScores().isAutoDistributing()) {
                    es.getStudentScores().setGrade(entry.getGrade());
                }
            }
        }
    }
}
