/*
 * EditableClassroomTest.java
 *
 * Created on 19. Oktober 2007, 15:40
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.model;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import java.beans.PropertyChangeEvent;
import java.util.*;
import javax.swing.undo.UndoableEditSupport;
import org.thespheres.betula.Student;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.classtest.Assessable;
import org.thespheres.betula.classtest.Basket;
import org.thespheres.betula.classtest.ClassTest;
import org.thespheres.betula.classtest.Hierarchical;
import org.thespheres.betula.classtest.StudentScores;
import org.thespheres.betula.util.CollectionChangeEvent;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.util.Grades;

/**
 *
 * @author boris.heithecker
 * @param <P>
 * @param <S>
 * @param <C>
 */
public abstract class EditableClassroomTest<P extends Assessable.Problem & Basket.Ref<?> & Basket<?, ?> & Hierarchical, S extends StudentScores, C extends ClassTest<P, S>> {

    public static final String COLLECTION_STUDENTS = "students";
    public static final String COLLECTION_PROBLEMS = "problems";
    public static final String PROP_PROBLEMSWEIGHTEDMAXIMUM = "problems-weighted-maximum";
    private final C test;
    private final EditableStudentComparator comparator = new EditableStudentComparator();
    private final ArrayList<EditableStudent<S>> students = new ArrayList<>();
    private final ArrayList<EditableBasket<P>> problems = new ArrayList<>();
    private final ProblemsList editableProblems = new ProblemsList();
    private WeightedProblemScores weightedSum;
    private ClasstestScoresSumMean scoresMean;
    private final Grades<Grade> grades = new Grades<>();
    protected final UndoableEditSupport undoSupport = new UndoableEditSupport(this);
    private final EventBus eventBus = new EventBus();
    private final GradesMean gradesMean;

    @SuppressWarnings("LeakingThisInConstructor")
    public EditableClassroomTest(C test) {
//        this.test = (test != null) ? test : new XmlClasstest();
        this.test = test;
        initEditableStudents();
        initEditableProblems();
        gradesMean = new GradesMean(this);
        eventBus.register(this);
    }

    public C getTest() {
        return test;
    }

    private void initEditableStudents() {
        test.getStudentScores().keySet().stream()
                .map(s -> new EditableStudent(s, test.getStudentScores().get(s), this))
                .forEach(students::add);
        Collections.sort(students, comparator);
        for (int i = 0; i < students.size(); i++) {
            final EditableStudent es = students.get(i);
            es.setIndex(i);
            Grade g = es.getStudentScores().getGrade();
            if (g != null) {
                if (g instanceof Grade.Biasable) {
                    g = ((Grade.Biasable) g).getUnbiased();
                }
                grades.inc(g);
            }
        }
    }

    private void initEditableProblems() {
        test.getProblems().values().stream()
                .sorted(Comparator.comparingInt(p -> ((P) p).getIndex()))
                .map(p -> new EditableBasket(this, p))
                .forEach(problems::add);
        problems
                .forEach(EditableBasket::initListeners);
    }

    //scores kann null sein
    public EditableProblem updateProblem(P prob) {
        EditableProblem ret;
        final String key = prob.getId();
        if ((ret = findProblem(key)) == null) {
            final P create = createAssessableProblem(key, prob.getIndex());
            create.setDisplayName(prob.getDisplayName());
            ret = insertProblem(key, create);
        } else {
            //TODO: aus as in ret alles kopieren!!
//            if (!student.getPrimaryUnit().equals(ret.getUnit())) {
//                ret.setUnit(student.getPrimaryUnit());
//            }
//            grades.inc(ret.getGrade().getLinkedGrade());
        }
        return ret;
    }

    protected abstract P createAssessableProblem(String key, int index);

    public EditableStudent updateStudent(Student s) {
        EditableStudent ret;
        if ((ret = findStudent(s)) == null) {
            ret = insertStudent(s, createStudentScores());
        } else {
            //?????
            Grade g = ret.getStudentScores().getGrade();
            if (g instanceof Grade.Biasable) {
                g = ((Grade.Biasable) g).getUnbiased();
            }
            grades.inc(g);
        }
//        if (scores != null) {
//            ret.updateScores(scores);
//        }
        gradesMean.reset();
        return ret;
    }

    protected abstract S createStudentScores();

    private EditableStudent findStudent(Student s) {
        for (EditableStudent es : getEditableStudents()) {
            if (es.getStudent().equals(s)) {
                return es;
            }
        }
        return null;
    }

    public EditableStudent findStudent(StudentId id) {
        for (EditableStudent es : getEditableStudents()) {
            if (es.getStudent().getStudentId().equals(id)) {
                return es;
            }
        }
        return null;
    }

    public Set<StudentId> studentIdSet() {
        class SSet extends AbstractSet<StudentId> {

            @Override
            public Iterator<StudentId> iterator() {
                final Iterator<EditableStudent<S>> orig = getEditableStudents().iterator();
                Iterator<StudentId> ret = new Iterator<StudentId>() {
                    @Override
                    public boolean hasNext() {
                        return orig.hasNext();
                    }

                    @Override
                    public StudentId next() {
                        return orig.next().getStudent().getStudentId();
                    }

                    @Override
                    public void remove() {
                        orig.remove();
                    }
                };
                return ret;
            }

            @Override
            public int size() {
                return getEditableStudents().size();
            }
        }
        return new SSet();
    }

    public List<EditableStudent<S>> getEditableStudents() {
        return Collections.unmodifiableList(students);
    }

    public List<EditableBasket<P>> getEditableProblems() {
        return editableProblems;
    }

    public double getAllProblemsWeightedMaximumSum() {
        if (weightedSum == null) {
            weightedSum = new WeightedProblemScores(this);
        }
        return weightedSum.getWeightedSum();
    }

    public double getAllStudentsScoresMean() {
        if (scoresMean == null) {
            scoresMean = new ClasstestScoresSumMean(this);
        }
        return scoresMean.getMean();
    }

    public Double getGradesMean() {
        return gradesMean.getMean();
    }

    public Grades<Grade> getGrades() { //enthätl nur linkedGrades!!
        return grades;
    }

    private EditableStudent insertStudent(Student student, S sc) {

        for (String p : test.getProblems().keySet()) {
            sc.put(p, 0d);
        }
//        Grade g = GradeFactory.find("de.notensystem", "2");
//        if (getTest().getContext() != null) {
        sc.setGrade(null); //g);
//        }
        test.getStudentScores().put(student, sc);
        final EditableStudent stud = new EditableStudent(student, sc, this);
        //
        students.add(stud); // return boolean ignored
        Collections.sort(students, comparator);
        //TODO: nur wenn oben sc.setGrade gesetzt !!!
//        if (stud.getGrade() != null) {
//        grades.inc(stud.getGrade().getLinkedGrade());
//        }
//        stud.addPropertyChangeListener(this);
        studentsIndices();
        //
//        getAllStudentsScoresMean().add(stud.getIndex(), stud.getScores().getSum());
        //
        for (int i = 0; i < editableProblems.size(); i++) {
            if (editableProblems.get(i).getMean() != 0d) {
//                editableProblems.get(i).getMean().add(stud.getIndex(), stud.getScores().get(i));
            }
        }
        //
        final CollectionChangeEvent pce = new CollectionChangeEvent(this, EditableClassroomTest.COLLECTION_STUDENTS, stud, CollectionChangeEvent.Type.ADD);
        eventBus.post(pce);
        return stud;
    }

    public EditableStudent removeStudent(final StudentId stud) {
        final EditableStudent rem;
        synchronized (students) {
            try {
                rem = students.stream()
                        .filter(es -> es.getStudentId().equals(stud))
                        .collect(CollectionUtil.singleOrNull());
            } catch (IndexOutOfBoundsException e) {
                return null;
            }
            if (rem != null) {
                Grade g = rem.getStudentScores().getGrade();
                if (g instanceof Grade.Biasable) {
                    g = ((Grade.Biasable) g).getUnbiased();
                }
                students.remove(rem.getIndex());
                test.getStudentScores().remove(rem.getStudent());
                studentsIndices();
                grades.dec(g);
                gradesMean.reset();
            }
        }
        if (rem != null) {
            final CollectionChangeEvent pce = new CollectionChangeEvent(this, EditableClassroomTest.COLLECTION_STUDENTS, rem, CollectionChangeEvent.Type.REMOVE);
            eventBus.post(pce);
        }
        return rem;
        //nICHT vergessen die event Referenzen im EditableStudent !!! zu löschen !!! mehtode initScores!!
    }

    private EditableProblem insertProblem(String key, P problem) {
        if (getTest().getProblems().containsKey(key)) {
            return null;
        }//TODO really necessary?

        test.getProblems().put(key, problem);
        test.getStudentScores().keySet().stream()
                .forEach(s -> test.getStudentScores().get(s).put(key, 0d));

        final int index = problem.getIndex();
        final EditableBasket<P> eprob = new EditableBasket<>(this, problem);

        synchronized (problems) {
            problems.add(index, eprob);
            problemsIndices();
        }

        getEditableStudents().stream()
                .forEach(es -> es.getStudentScores().put(eprob.getId(), 0d));

        if (eprob.isBasket()) {
            ((EditableBasket) eprob).initListeners();
        }

        CollectionChangeEvent pce = new CollectionChangeEvent(this, EditableClassroomTest.COLLECTION_PROBLEMS, eprob, CollectionChangeEvent.Type.ADD);
        eventBus.post(pce);

//        Double w = eprob.isBasket() ? 0d : eprob.getWeight();
//        int ms = eprob.isBasket() ? 0 : eprob.getMaxScore();
//        getAllProblemsWeightedMaximumSum().add(eprob.getIndex(), ms, w);
        return eprob;
    }

    void removeProblem(EditableBasket<P> p) {
        test.getProblems().remove(p.getId());
        test.getStudentScores().keySet().stream()
                .forEach(s -> test.getStudentScores().get(s).remove(p.getId()));

        synchronized (problems) {
            problems.remove(p);
            problemsIndices();
        }

        if (p.isBasket()) {
            p.initListeners();
        }
        CollectionChangeEvent pce = new CollectionChangeEvent(this, EditableClassroomTest.COLLECTION_PROBLEMS, p, CollectionChangeEvent.Type.REMOVE);
        eventBus.post(pce);

    }

    public EditableProblem findProblem(final String id) {
        final EditableProblem ret;
        synchronized (editableProblems) {
            ret = editableProblems.stream()
                    .filter(ep -> ep.getId().equals(id))
                    .collect(CollectionUtil.singleOrNull());
        }
        return ret;
    }

    public EditableProblem[] findChildren(final String parentId) {
        synchronized (editableProblems) {
            return editableProblems.stream()
                    .filter(ep -> ep.getParent() != null && ep.getParent().getId().equals(parentId))
                    .toArray(EditableProblem[]::new);
        }
    }

    private void problemsIndices() {
        for (int i = 0; i < problems.size(); i++) {
            EditableBasket<P> ep = problems.get(i);
            if (ep.getIndex() != i) {
                ep.setIndex(i);
            }
        }
    }

    private void studentsIndices() {
        for (int i = 0; i < students.size(); i++) {
            EditableStudent s = students.get(i);
            if (s.getIndex() != i) {
                s.setIndex(i);
            }
        }
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    @Subscribe
    public void onPropertyChange(PropertyChangeEvent evt) {
        final String name = evt.getPropertyName();
        if (evt.getSource() instanceof EditableStudent) {
            if (EditableStudent.PROP_GRADE.equals(name)) {
                Grade old = (Grade) evt.getOldValue();
                Grade g = (Grade) evt.getNewValue();
                if (old != null) {
                    if (old instanceof Grade.Biasable) {
                        old = ((Grade.Biasable) old).getUnbiased();
                    }
                    grades.dec(old);
                }
                if (g != null) {
                    if (g instanceof Grade.Biasable) {
                        g = ((Grade.Biasable) g).getUnbiased();
                    }
                    grades.inc(g);
                }
                gradesMean.reset();
            } else if (EditableStudent.PROP_AUTODISTRIBUTING.equals(name)) {
            }
        }
    }

    private class ProblemsList extends AbstractList<EditableBasket<P>> {

        @Override
        public int size() {
            return problems.size();
        }

        @Override
        public EditableBasket<P> get(int index) {
            return problems.get(index);
        }

        @Override
        public EditableBasket<P> set(int index, EditableBasket<P> element) {
            final EditableBasket<P> p = (EditableBasket<P>) element;
            EditableBasket<P> ret = problems.set(index, p);
            p.setIndex(index);
            return ret;
        }
    }
}
