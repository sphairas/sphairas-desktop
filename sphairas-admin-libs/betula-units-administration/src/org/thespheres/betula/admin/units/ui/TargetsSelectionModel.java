/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.ui;

import java.awt.EventQueue;
import org.thespheres.betula.admin.units.RemoteStudent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.swing.table.AbstractTableModel;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.Unit;
import org.thespheres.betula.admin.units.RemoteStudents;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.admin.units.TargetsSelectionElementEnv2;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GradeTermTargetAssessment;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.ui.util.ExportToCSVOption;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.model.DocumentsModel;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.Terms;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
public class TargetsSelectionModel extends AbstractTableModel implements ItemListener, ExportToCSVOption, TargetsElementModel {

    public static final String TABLE_PROP_CURRENT_TARGETTYPE = "current.target.type";
    private final List<RemoteTargetAssessmentDocument> targets = new ArrayList<>();
    private final Map<DocumentId, Listener> listeners = new HashMap<>();
    private final ArrayList<RemoteStudent> students = new ArrayList<>();
    private Term currentTerm;
    private final PropertyChangeSupport pSupport = new PropertyChangeSupport(this);
    private final SortedSet<Term> terms = new TreeSet<>((t1, t2) -> t1.getBeginDate().compareTo(t2.getBeginDate()));
    private final TargetsSelectionElementEnv2 env;
    private final WeakReference<TargetsSelectionElement> component;
    private String displayName;
    private List<RemoteTargetAssessmentDocument> lastTargets;

    @SuppressWarnings("LeakingThisInConstructor")
    TargetsSelectionModel(final TargetsSelectionElementEnv2 env, TargetsSelectionElement cmp) {
        this.env = env;
        this.component = new WeakReference(cmp);
    }

    List<RemoteTargetAssessmentDocument> getTargets() {
        return targets;
    }

    List<RemoteTargetAssessmentDocument> getLastTargets() {
        return lastTargets != null ? lastTargets : Collections.EMPTY_LIST;
    }

    void init(final List<RemoteTargetAssessmentDocument> d) {
        final List<RemoteStudent> rs = collectStudents(d);
        final Set<Term> t = collectTerms(d);
        EventQueue.invokeLater(() -> initInEDT(d, rs, t));
    }

    private Set<Term> collectTerms(final List<RemoteTargetAssessmentDocument> d) {
        return d.stream()
                .flatMap(r -> r.identities().stream())
                .map(Terms::forTermId)
                .collect(Collectors.toSet());
    }

    private List<RemoteStudent> collectStudents(final List<RemoteTargetAssessmentDocument> d) {
        return d.stream()
                .flatMap(r -> r.students().stream())
                .distinct()
                .map(s -> RemoteStudents.find(env.getProvider(), s))
                .sorted()
                .collect(Collectors.toList());
    }

    synchronized void initInEDT(final List<RemoteTargetAssessmentDocument> d, final List<RemoteStudent> studs, final Set<Term> terms) {

        List<RemoteTargetAssessmentDocument> before = null;
        if (d != null) {
            targets.stream()
                    .forEach(t -> {
                        final TargetsSelectionModel.Listener l = listeners.get(t.getDocumentId());
                        if (l != null) {
                            t.removeListener(l);
                        }
                        getComponent().ifPresent(cmp -> t.removeUndoableEditListener(cmp.getUndoRedo()));
                    });
            before = new CopyOnWriteArrayList<>(targets);
            targets.clear();
            d.stream().forEach(t -> {
                targets.add(t);
                final TargetsSelectionModel.Listener l = listeners.computeIfAbsent(t.getDocumentId(), did -> new TargetsSelectionModel.Listener());
                t.addListener(l);
                getComponent().ifPresent(cmp -> t.addUndoableEditListener(cmp.getUndoRedo()));
            });
        }

        this.students.clear();
        this.terms.clear();
        this.terms.addAll(terms);
        this.students.addAll(studs);
        updateDisplayName();
        this.lastTargets = before;
        fireTableStructureChanged();
    }

    private void updateDisplayName() {
        final LocalProperties lm = LocalProperties.find(env.getProvider());
        final DocumentsModel dm = new DocumentsModel();
        dm.initialize(lm.getProperties());
        final NamingResolver nr = NamingResolver.find(env.getProvider());
        final List<DocumentId> l = targets.stream()
                .map(rtad -> dm.convert(rtad.getDocumentId()))
                .distinct()
                .collect(Collectors.toList());
        displayName = l.stream()
                .map(d -> {
                    try {
                        return nr.resolveDisplayName(d);
                    } catch (IllegalAuthorityException ex) {
                        return d.getId();
                    }
                })
                .collect(Collectors.joining(", "));
    }

    String getDisplayName() {
        return displayName;
    }

    Optional<TargetsSelectionElement> getComponent() {
        return Optional.of(component.get());
    }
//    void updateTargets(final Map<String, List<RemoteTargetAssessmentDocument>> m) {
//        checkIsAWT();
//        initTerms();
////        hidden.beforeTableStructureChanged(); ????
//        fireTableStructureChanged();//culprit for too much time in awt
//    }
//
//    void initStudentsOnEvent() {
//        initStudents();
//        fireTableStructureChanged();
//    }

    boolean restoreCurrentIdentity(final TermId restore) {
        if (restore != null) {
            for (Term t : getTerms()) {
                if (t.getScheduledItemId().equals(restore)) {
                    setCurrentIndentity(t);
                    return true;
                }
            }
        }
        final List<Term> t = getTerms();
        if (!t.isEmpty()) {
            final Term term = t.get(t.size() - 1);
            setCurrentIndentity(term);
        }
        return false;
    }

    @Override
    public Term getCurrentIndentity() {
        return currentTerm;
    }

    private void setCurrentIndentity(Term id) {
        if (Objects.equals(getCurrentIndentity(), id)) {
            return;
        }
        currentTerm = id;
        fireTableStructureChanged();
        getComponent().ifPresent(cmp -> cmp.getTable().putClientProperty(TABLE_PROP_CURRENT_TERMID, getCurrentIndentity().getScheduledItemId()));
    }

    @Override
    public int getRowCount() {
        return students.size();
    }

    @Override
    public synchronized int getColumnCount() {
        return targets.size() + 3;
    }

    @Override
    public synchronized RemoteTargetAssessmentDocument getRemoteTargetAssessmentDocumentForDocumentId(final DocumentId id) {
        return targets.stream()
                .filter(t -> t.getDocumentId().equals(id))
                .collect(CollectionUtil.singleOrNull());
    }

    @Override
    public synchronized RemoteTargetAssessmentDocument getRemoteTargetAssessmentDocumentAtColumnIndex(int i) {
        final int index = i - 2;
        return getRemoteTargetAssessmentDocumentAtListIndex(index);
    }

    @Override
    public RemoteTargetAssessmentDocument getRemoteTargetAssessmentDocumentAtListIndex(int index) {
        try {
            return targets.get(index);
        } catch (IndexOutOfBoundsException e) {
            PlatformUtil.getCodeNameBaseLogger(TargetsSelectionModel.class).log(Level.FINE, e.getLocalizedMessage(), e);
            return null;
        }
    }

    @Override
    public synchronized int getRemoteTargetAssessmentDocumentsSize() {
        return targets.size();
    }

    public RemoteStudent getStudentAt(int row) {
        return students.get(row);
    }

    private void checkIsAWT() throws RuntimeException {
        if (!EventQueue.isDispatchThread()) {
            throw new RuntimeException("TargetsSelectionModel must be called event queue.");
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (currentTerm == null) {
            return null;
        }
        checkIsAWT();
        final RemoteStudent rs = students.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return rs;
            case 1:
                return rs.getClientProperty(RemoteStudents.PROP_PRIMARY_UNIT, Unit.class);
            default:
                final RemoteTargetAssessmentDocument rtad = getRemoteTargetAssessmentDocumentAtColumnIndex(columnIndex);
                return rtad == null ? null : rtad.selectGradeAccess(rs.getStudentId(), currentTerm.getScheduledItemId());
        }
    }

    @Override
    public void setValueAt(Object val, int rowIndex, int columnIndex) {
        if (!(val instanceof Grade)) {
            return;
        }
        final Grade g = (Grade) val;
        checkIsAWT();
        final RemoteStudent rs = students.get(rowIndex);
        final RemoteTargetAssessmentDocument rtad = getRemoteTargetAssessmentDocumentAtColumnIndex(columnIndex);
        if (rtad != null && currentTerm != null) {
            rtad.submitUndoable(rs.getStudentId(), currentTerm.getScheduledItemId(), g, null);
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (currentTerm == null) {
            return false;
        }
        checkIsAWT();
        final RemoteStudent rs = students.get(rowIndex);
        if (columnIndex != 0 && columnIndex != 1 && columnIndex != getColumnCount() - 1 && currentTerm != null) {
            RemoteTargetAssessmentDocument rtad = getRemoteTargetAssessmentDocumentAtColumnIndex(columnIndex);
            return rtad != null && rtad.getPreferredConvention() != null && rtad.select(rs.getStudentId(), currentTerm.getScheduledItemId()) != null;
        }
        return false;
    }

    List<Term> getTerms() {
        return terms.stream().collect(Collectors.toList());
    }

    @Override
    public void itemStateChanged(final ItemEvent e) {
        if (e.getItem() instanceof Term && e.getStateChange() == ItemEvent.SELECTED) {
            Term term = (Term) e.getItem();
            setCurrentIndentity(term);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        pSupport.removePropertyChangeListener(l);
    }

    @NbBundle.Messages({"TargetsSelectionModel.export.csv.filehint={0}.csv",
        "TargetsSelectionModel.export.csv.default.filehint={0} Listen.csv"})
    @Override
    public String createFileNameHint() throws IOException {
        final Term term = getCurrentIndentity();
        if (term == null) {
            throw new IOException("No current Term selected.");
        }
        return getComponent()
                .map(TargetsSelectionElement::getNode)
                .map(Node::getDisplayName)
                .map(n -> NbBundle.getMessage(TargetsForStudentsModel.class, "TargetsSelectionModel.export.csv.filehint", n))
                .orElse(NbBundle.getMessage(TargetsForStudentsModel.class, "TargetsSelectionModel.export.csv.default.filehint", targets.size()));
    }

    @NbBundle.Messages({"TargetsSelectionModel.export.csv.name=Name"})
    @Override
    public byte[] getCSV() throws IOException {
        StringBuilder sb = new StringBuilder();
        StringJoiner header = new StringJoiner(";", "", "\n");
        header.add(NbBundle.getMessage(TargetsForStudentsModel.class, "TargetsSelectionModel.export.csv.name"));
        for (int j = 0; j < getRemoteTargetAssessmentDocumentsSize() - 1; j++) {
            String val = Optional.ofNullable(getRemoteTargetAssessmentDocumentAtListIndex(j))
                    .map(rtad -> rtad.getName().getDisplayName(currentTerm))
                    .orElse("");
            header.add(val);
        }
        sb.append(header.toString());
        for (int i = 0; i < getRowCount(); i++) {
            StringJoiner sj = new StringJoiner(";", "", "\n");
            RemoteStudent rs = students.get(i);
            sj.add(rs.getDirectoryName());
            for (int j = 0; j < getRemoteTargetAssessmentDocumentsSize() - 1; j++) {
                String val = Optional.ofNullable(getRemoteTargetAssessmentDocumentAtListIndex(j))
                        .flatMap(rtad -> rtad.selectGradeAccess(rs.getStudentId(), currentTerm.getScheduledItemId()))
                        .filter(ga -> !ga.isUnconfirmed())
                        .map(ga -> ga.getGrade().getShortLabel())
                        .orElse("");
                sj.add(val);
            }
            sb.append(sj.toString());
        }
        return sb.toString().getBytes("utf-8");
    }

    private final class Listener implements GradeTermTargetAssessment.Listener { //, PropertyChangeListener {

//        int index;
        @Override
        public void valueForStudentChanged(Object source, StudentId student, TermId gradeId, Grade old, Grade newGrade, Timestamp timestamp) {
            for (int i = 0; i < students.size(); i++) {
                if (students.get(i).getStudentId().equals(student)) {
                    TargetsSelectionModel.this.fireTableRowsUpdated(i, i);
                    return;
//                    final int row = i;
//                    Mutex.EVENT.writeAccess(() -> {
//                        try {
//                            fireTableCellUpdated(row, index + 1);
//                        } catch (Exception e) {
//                            PlatformUtil.getCodeNameBaseLogger(TargetsForStudentsModel.class).log(Level.FINE, e.getLocalizedMessage(), e);
//                        }
//                    });
//                    final Term ci = getCurrentIndentity();
//                    if (ci != null && source instanceof RemoteTargetAssessmentDocument) {
//                        final RemoteTargetAssessmentDocument rtad = (RemoteTargetAssessmentDocument) source;
////                        final Set<StudentId> studs = HideRTADColumns.isHideIfEmptyForPU() ? getRemoteUnitsModel().getStudentIds() : null;
////                        hidden.possiblyUpdateAfterSetGrade(rtad.getDocumentId(), rtad.isEmptyFor(ci.getScheduledItemId(), studs));
//                    }
                }
            }
            //Not found
            final List<RemoteStudent> rs = collectStudents(targets);
            final Set<Term> t = collectTerms(targets);
            EventQueue.invokeLater(() -> initInEDT(null, rs, t));
        }

//        @Override
//        public void propertyChange(PropertyChangeEvent evt) {
//            switch (evt.getPropertyName()) {
//                case RemoteTargetAssessmentDocumentName.PROP_DISPLAYNAME:
////                    col.setHeaderValue("X"); //name.getDisplayName(true));
//                    break;
//            }
//        }
    }
}
