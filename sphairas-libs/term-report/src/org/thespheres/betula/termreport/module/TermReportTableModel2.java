/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport.module;

import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.Unit;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.TargetAssessment;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.listprint.builder.TableItem;
import org.thespheres.betula.termreport.AssessmentProvider;
import org.thespheres.betula.termreport.TargetAssessmentProvider;
import org.thespheres.betula.termreport.TermReport;
import org.thespheres.betula.termreport.module.TermReportTableModel2.TermReportColFactory;
import org.thespheres.betula.ui.util.AbstractPluggableTableModel;
import org.thespheres.betula.ui.util.AbstractPluggableTableModel.PluggableColumnFactory;
import org.thespheres.betula.ui.util.PluggableTableColumn;
import org.thespheres.betula.util.CollectionChangeEvent;
import org.thespheres.betula.util.StudentComparator;

/**
 *
 * @author boris.heithecker
 */
public class TermReportTableModel2 extends AbstractPluggableTableModel<TermReport, StudentId, PluggableTableColumn<TermReport, StudentId>, TermReportColFactory> implements TableItem {

    private final List<StudentId> students = new ArrayList<>();
    private final Set<StudentId> hidden = new HashSet<>();
    private final Map<String, Listener> listeners = new HashMap();
    private final StudentComparator sComp = new StudentComparator();

    private TermReportTableModel2(Set<? extends PluggableTableColumn<TermReport, StudentId>> s) {
        super("TermReportTableModel", s);
    }

    static TermReportTableModel2 create() {
        Set<PluggableTableColumn<TermReport, StudentId>> s = TermReportTableColumn.createDefaultSet();
        MimeLookup.getLookup(TermReportDataObject.TERMREPORT_MIME)
                .lookupAll(TermReportTableColumn.Factory.class).stream()
                .map(TermReportTableColumn.Factory::createInstance)
                .forEach(s::add);
        return new TermReportTableModel2(s);
    }

    @Override
    public void initialize(TermReport report, Lookup context) {
        if (model != null) {
            model.getEventBus().unregister(this);
            final Iterator<Map.Entry<String, Listener>> it = listeners.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Listener> e = it.next();
                e.getValue().provider.removePropertyChangeListener(e.getValue());
                it.remove();
            }
        }
        super.initialize(report, context);
        report.getProviders().stream().forEach(this::addListener);
        model.getEventBus().register(this);
    }

    private void addListener(AssessmentProvider p) {
        Listener l;
        if (p instanceof TargetAssessmentProvider) {
            l = new TargetProviderListener((TargetAssessmentProvider) p);
        } else {
            l = new Listener(p);
        }
        listeners.put(p.getId(), l);
    }

    public List<StudentId> getStudents() {
        return students;
    }

    @Override
    protected TermReportColFactory createColumnFactory() {
        return new TermReportColFactory();
    }

    @Override
    protected int getItemSize() {
        return getStudents().size();
    }

    @Override
    protected StudentId getItemAt(int row) {
        return getStudents().get(row);
    }

    @Subscribe
    public void onChange(CollectionChangeEvent event) {
        if (event.getCollectionName().equals(TermReport.PROP_ASSESSMENTS) && event.getSource() instanceof TermReport) {
            event.getItemAs(AssessmentProvider.class).ifPresent(provider -> {
                if (event.getType().equals(CollectionChangeEvent.Type.ADD)) {
                    addListener(provider);
                } else if (event.getType().equals(CollectionChangeEvent.Type.REMOVE)) {
                    Listener removed = listeners.remove(provider.getId());
                    if (removed != null) {
                        removed.provider.removePropertyChangeListener(removed);
                        if (removed instanceof TargetProviderListener) {
                            TargetProviderListener tal = (TargetProviderListener) removed;
                            ((TargetAssessmentProvider) tal.provider).removeListener(tal);
                        }
                    }
                }
                Mutex.EVENT.postWriteRequest(this::fireTableStructureChanged);
            });
        }
    }

//    private void updateStudents(TargetAssessment<?> target) {
//        boolean changed = false;
//        synchronized (students) {
//            for (StudentId s : target.students()) {
//                if (!students.contains(s)) {
//                    students.add(s);
//                    changed = true;
//                }
//            }
//        }
//        Unit unit = context.lookup(Unit.class);
//        if (changed && unit != null) {
//            synchronized (students) {
//                Collections.sort(students, Comparator.comparing(unit::findStudent, sComp));
//            }
//            EventQueue.invokeLater(this::fireTableStructureChanged);
//        }
//    }
    private boolean showStudent(final StudentId stud) {
        return Optional.ofNullable(context.lookup(Unit.class))
                .map(u -> u.findStudent(stud))
                .map(Objects::nonNull)
                .orElse(false);
    }

    private void updateStudents() {
        assert EventQueue.isDispatchThread();
        boolean changed;
        final Set<StudentId> all;
        try {
            all = listeners.values().stream()
                    .filter(li -> li instanceof TargetProviderListener)
                    .map(li -> (TargetProviderListener) li)
                    .filter(li -> li.provider.getInitialization().satisfies(AssessmentProvider.READY))
                    .flatMap(tpl -> tpl.provider.students().stream())
                    .filter(this::showStudent)
                    .collect(Collectors.toSet());
        } catch (ConcurrentModificationException e) {
            //This may happen if students are fetched from external entity
            EventQueue.invokeLater(TermReportTableModel2.this::updateStudents);
            return;
        }
        synchronized (students) {
            final Sets.SetView<StudentId> toRemove = Sets.difference(Sets.newHashSet(students), all);
            final Sets.SetView<StudentId> toAdd = Sets.difference(all, Sets.newHashSet(students));

            students.addAll(toAdd);
            students.removeAll(toRemove);
            changed = !toRemove.isEmpty() || !toAdd.isEmpty();
        }
        final Unit unit = context.lookup(Unit.class);
        if (changed && unit != null) {
            synchronized (students) {
                Collections.sort(students, Comparator.comparing(unit::findStudent, sComp));
            }
            fireTableStructureChanged();
        }
    }

    private class TargetProviderListener extends Listener implements TargetAssessment.Listener<Grade> {

        @SuppressWarnings("LeakingThisInConstructor")
        private TargetProviderListener(TargetAssessmentProvider p) {
            super(p);
            init();
            provider.addPropertyChangeListener(this);
        }

        private void init() {
            if (provider.getInitialization().satisfies(AssessmentProvider.READY)) {
//                updateStudents((TargetAssessmentProvider) provider);
                ((TargetAssessmentProvider) provider).addListener(this);
                EventQueue.invokeLater(TermReportTableModel2.this::updateStudents);
            } else if (provider.getInitialization().isError()) {
                ((TargetAssessmentProvider) provider).removeListener(this);
                EventQueue.invokeLater(TermReportTableModel2.this::fireTableDataChanged);
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            super.propertyChange(evt);
            String prop = evt.getPropertyName();
            if (prop.equals("displayLong")) {
                TermReportTableModel2.this.fireTableDataChanged();
            } else if (prop.equals(AssessmentProvider.PROP_STATUS)) {
                init();
            }
        }

        @Override
        public void valueForStudentChanged(Object source, StudentId s, Grade old, Grade newGrade, Timestamp timestamp) {
            if (s != null && (old == null || newGrade == null)) {
                //Studentadded or removed
//                updateStudents((TargetAssessmentProvider) provider);
                EventQueue.invokeLater(TermReportTableModel2.this::updateStudents);
//                EventQueue.invokeLater(TermReportTableModel2.this::fireTableStructureChanged);
            } else {
                EventQueue.invokeLater(TermReportTableModel2.this::fireTableDataChanged);
            }
        }

    }

    private class Listener implements PropertyChangeListener {

        protected final AssessmentProvider<?> provider;

        @SuppressWarnings("LeakingThisInConstructor")
        private Listener(AssessmentProvider p) {
            provider = p;
            provider.addPropertyChangeListener(this);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String prop = evt.getPropertyName();
            if (prop.equals("displayName") || prop.equals("position")) {
                TermReportTableModel2.this.fireTableStructureChanged();
            } else {
                Mutex.EVENT.writeAccess(TermReportTableModel2.this::fireTableDataChanged);
            }
        }
    }

    public class TermReportColFactory extends PluggableColumnFactory {
    }
}
