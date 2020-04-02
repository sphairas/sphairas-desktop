package org.thespheres.betula.classtest.module2;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import com.google.common.eventbus.Subscribe;
import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import javax.swing.event.EventListenerList;
import org.netbeans.spi.project.LookupProvider;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.assess.AssessmentContext;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GradeTargetAssessment;
import org.thespheres.betula.assess.TargetAssessment;
import org.thespheres.betula.classtest.model.EditableStudent;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.classtest.model.EditableClassroomTest;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
class ClassroomTestTargetAssessment implements GradeTargetAssessment, LookupListener {

    private final DataObject dob;
    private Lookup.Result<EditableClassroomTest> result;
    private final RequestProcessor RP = new RequestProcessor(ClassroomTestTargetAssessment.class);
    private final InstanceContent ic = new InstanceContent(RP);
    private final Lookup lookup = new AbstractLookup(ic);
    private final EditableClassroomTest[] etest = new EditableClassroomTest[]{null};
    private final transient EventListenerList listeners = new EventListenerList();

    @SuppressWarnings("LeakingThisInConstructor")
    private ClassroomTestTargetAssessment(DataObject dataobj) {
        this.dob = dataobj;
        RP.post(this::initialize);
    }

    private void initialize() {
        this.result = this.dob.getLookup().lookupResult(EditableClassroomTest.class);
        this.result.addLookupListener(this);
        resultChanged(null);
    }

    static Lookup createLookup(DataObject dob) {
        ClassroomTestTargetAssessment ret = new ClassroomTestTargetAssessment(dob);
        return ret.lookup;
    }

    private EditableClassroomTest getEditableClassroomTest() {
        return etest[0];
    }

    @Override
    public final void resultChanged(LookupEvent ev) {
        synchronized (etest) {
            EditableClassroomTest old = getEditableClassroomTest();
            etest[0] = result.allInstances().stream()
                    .map(EditableClassroomTest.class::cast)
                    .collect(CollectionUtil.singleOrNull());
            final EditableClassroomTest e = getEditableClassroomTest();
            if (!Objects.equals(old, e)) {
                if (old != null) {
                    old.getEventBus().unregister(this);
//                    old.removeClassroomTestListener(this);
//                    old.getEditableStudents()
//                            .forEach(es -> es.removePropertyChangeListener(this));
                }
                if (e != null) {
                    e.getEventBus().register(this);
//                    e.addClassroomTestListener(this);
//                    e.getEditableStudents()
//                            .forEach(es -> es.addPropertyChangeListener(this));
                    ic.add(this);
                } else {
                    ic.remove(this);
                }
            }
        }
    }

    private void checkInitialized() {
        if (getEditableClassroomTest() == null) {
            throw new IllegalStateException("ClassroomTest is not set.");
        }
    }

    @Override
    public void submit(StudentId student, Grade grade, Timestamp timestamp) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Grade select(StudentId student) {
        checkInitialized();
        EditableStudent s = getEditableClassroomTest().findStudent(student);
        return s != null ? s.getStudentScores().getGrade() : null;
    }

    @Override
    public Timestamp timestamp(StudentId student) {
        checkInitialized();
        return null;
    }

    @Override
    public Set<StudentId> students() {
        return getEditableClassroomTest().studentIdSet();
    }

    @Override
    public String getPreferredConvention() { //TODO l
        AssessmentContext ac = dob.getLookup().lookup(AssessmentContext.class);
        if (ac != null) {
            return ac.getName();
        }
        return null;
    }

    @Override
    public void addListener(TargetAssessment.Listener<Grade> listener) {
        listeners.add(TargetAssessment.Listener.class, listener);
    }

    @Override
    public void removeListener(Listener listener) {
        listeners.remove(Listener.class, listener);
    }

    @Subscribe
    public void onPropertyChange(PropertyChangeEvent evt) {
        final String name = evt.getPropertyName();
        if (evt.getSource() instanceof EditableStudent) {
            final EditableStudent s = (EditableStudent) evt.getSource();
            if (EditableStudent.PROP_GRADE.equals(name)) {
                final Grade ov = (Grade) evt.getOldValue();
                Arrays.stream(listeners.getListeners(Listener.class))
                        .forEach(l -> l.valueForStudentChanged(getEditableClassroomTest(), s.getStudentId(), ov, s.getStudentScores().getGrade(), null));
            } else if (EditableStudent.PROP_AUTODISTRIBUTING.equals(name)) {
            }
        }
    }

    @ServiceProvider(service = LookupProvider.class, path = "Loaders/text/betula-classtest-file+xml/Lookup")
    public static class Registration implements LookupProvider {

        @Override
        public Lookup createAdditionalLookup(Lookup baseContext) {
            ClasstestDataObject dob = baseContext.lookup(ClasstestDataObject.class);
            if (dob != null) {
                return ClassroomTestTargetAssessment.createLookup(dob);
            }
            return Lookup.EMPTY;
        }
    }
}
