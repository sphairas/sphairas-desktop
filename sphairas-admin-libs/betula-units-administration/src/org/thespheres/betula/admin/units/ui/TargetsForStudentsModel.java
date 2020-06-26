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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.swing.table.AbstractTableModel;
import org.apache.commons.lang3.StringUtils;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.admin.units.AbstractUnitOpenSupport;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocumentName;
import org.thespheres.betula.admin.units.RemoteUnitsModel;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GradeTermTargetAssessment;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.services.scheme.Terms;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.ui.util.ExportToCSVOption;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
public class TargetsForStudentsModel extends AbstractTableModel implements ItemListener, ExportToCSVOption, TargetsElementModel {

    public static final String TABLE_PROP_CURRENT_TARGETTYPE = "current.target.type";
    private final WeakReference<TargetsForStudentsElement> component;
    private final Map<String, List<RemoteTargetAssessmentDocument>> targets = new HashMap<>();
    private final Map<DocumentId, Listener> listeners = new HashMap<>();
    private final ArrayList<RemoteStudent> students = new ArrayList<>();
    private final HashSet<String> targetTypes = new HashSet<>();
    private Term currentTerm;
    private String currentTargetType;
    private final PropertyChangeSupport pSupport = new PropertyChangeSupport(this);
    private final SortedSet<Term> terms = new TreeSet<>((t1, t2) -> t1.getBeginDate().compareTo(t2.getBeginDate()));
    private final AbstractUnitOpenSupport support;
    private final RemoteUnitsModel remoteModel;
    private AbstractNode node;
    final HideRTADColumns hidden = new HideRTADColumns(this);

    @SuppressWarnings("LeakingThisInConstructor")
    TargetsForStudentsModel(AbstractUnitOpenSupport uos, RemoteUnitsModel model, TargetsForStudentsElement cmp) {
        this.support = uos;
        this.component = new WeakReference(cmp);
        this.remoteModel = model;
        init();
    }

    public AbstractUnitOpenSupport getSupport() {
        return support;
    }

    public RemoteUnitsModel getRemoteUnitsModel() {
        return remoteModel;
    }

    public Node getNodeDelegate() {
        if (node == null) {
            node = new AbstractNode(Children.LEAF, Lookups.singleton(this));
        }
        return node;
    }

    Optional<TargetsForStudentsElement> getComponent() {
        return Optional.of(component.get());
    }

    private void init() {
        final Map<String, List<RemoteTargetAssessmentDocument>> m = remoteModel.getTargets().stream()
                .collect(Collectors.groupingBy(RemoteTargetAssessmentDocument::getTargetType, Collectors.toList()));
        initCurrentTargets(m);
        initStudents();
        initTerms();
    }

    void initOnEvent() {
        init();
        fireTableStructureChanged();
    }

    private void initTerms() {
        terms.clear();
        remoteModel.getTerms().stream()
                .map(Terms::forTermId)
                .forEach(terms::add);
    }

    void updateTargets(final Map<String, List<RemoteTargetAssessmentDocument>> m) {
        checkIsAWT();
        initCurrentTargets(m);
        initTerms();
//        hidden.beforeTableStructureChanged(); ????
        fireTableStructureChanged();//culprit for too much time in awt
        hidden.updateAfterSetCurrentIdentityTargetType();
    }

    private synchronized void initStudents() {
        students.clear();
        students.addAll(remoteModel.getStudents());
    }

    void initStudentsOnEvent() {
        initStudents();
        fireTableStructureChanged();
    }

    //TODO: undoredo listeners here?
    private synchronized void initCurrentTargets(final Map<String, List<RemoteTargetAssessmentDocument>> m) {
        targetTypes.clear();
        targets.values().stream()
                .flatMap(List::stream)
                .forEach(t -> {
                    final Listener l = listeners.get(t.getDocumentId());
                    if (l != null) {
                        t.removeListener(l);
                        t.getName().removePropertyChangeListener(l);
                    }
                });
        targets.clear();
        m.forEach((k, v) -> {
            targets.put(k, v);
            targetTypes.add(k);
            Collections.sort(v, Comparator.comparing(RemoteTargetAssessmentDocument::getName));
            v.forEach(t -> {
                final Listener l = listeners.computeIfAbsent(t.getDocumentId(), did -> new Listener());
                t.addListener(l);
                t.getName().addPropertyChangeListener(l);
            });
        });
    }

    public String getCurrentTargetType() {
        return currentTargetType;
    }

    boolean restoreCurrentIdentity(final TermId restore) {
        for (Term t : getTerms()) {
            if (t.getScheduledItemId().equals(restore)) {
                setCurrentIndentity(t);
                return true;
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
        fireStructureChanged();
        getComponent().ifPresent(cmp -> cmp.getTable().putClientProperty(TABLE_PROP_CURRENT_TERMID, getCurrentIndentity().getScheduledItemId()));
    }

    private void setCurrentTargetType(String tt) {
        if (Objects.equals(getCurrentTargetType(), tt)) {
            return;
        }
        currentTargetType = tt.toLowerCase();
        fireStructureChanged();
        //TODO: also in restoreCurrentTargetType???
        getComponent().ifPresent(cmp -> cmp.getTable().putClientProperty(TABLE_PROP_CURRENT_TARGETTYPE, Optional.ofNullable(getCurrentIndentity()).map(ci -> ci.getScheduledItemId()).orElse(null)));
    }

    private void fireStructureChanged() {
        hidden.beforeTableStructureChanged();
        fireTableStructureChanged();
        hidden.updateAfterSetCurrentIdentityTargetType();
    }

    synchronized boolean restoreCurrentTargetType(final String savedTargetType) {
        if (targetTypes.contains(savedTargetType.toLowerCase())) {//List: groß, savedT... klein
            currentTargetType = savedTargetType.toLowerCase();
            return true;
        }
        return false;
    }

    @Override
    public int getRowCount() {
        return students.size();
    }

    @Override
    public synchronized int getColumnCount() {
        if (targets.containsKey(currentTargetType)) {
            return targets.get(currentTargetType).size() + 2;
        }
        return 0;
    }

    @Override
    public synchronized RemoteTargetAssessmentDocument getRemoteTargetAssessmentDocumentForDocumentId(final DocumentId id) {
        if (targets.containsKey(currentTargetType) && id != null) {
            return targets.get(currentTargetType).stream()
                    .filter(t -> t.getDocumentId().equals(id))
                    .collect(CollectionUtil.singleOrNull());
        }
        return null;
    }

    @Override
    public synchronized RemoteTargetAssessmentDocument getRemoteTargetAssessmentDocumentAtColumnIndex(int i) {
        final int index = i - 1;
        return getRemoteTargetAssessmentDocumentAtListIndex(index);
    }

    @Override
    public RemoteTargetAssessmentDocument getRemoteTargetAssessmentDocumentAtListIndex(int index) {
        try {
            return targets.containsKey(currentTargetType) ? targets.get(currentTargetType).get(index) : null;
        } catch (IndexOutOfBoundsException e) {
            PlatformUtil.getCodeNameBaseLogger(TargetsForStudentsModel.class).log(Level.FINE, e.getLocalizedMessage(), e);
            return null;
        }
    }

    @Override
    public synchronized int getRemoteTargetAssessmentDocumentsSize() {
        return targets.containsKey(currentTargetType) ? targets.get(currentTargetType).size() : -1;
    }

    public RemoteStudent getStudentAt(int row) {
        return students.get(row);
    }

    private void checkIsAWT() throws RuntimeException {
        if (!EventQueue.isDispatchThread()) {
            throw new RuntimeException("TargetsForStudentsModel must be called event queue.");
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (currentTerm == null) {
            return null;
        }
        checkIsAWT();
        final RemoteStudent rs = students.get(rowIndex);
        if (columnIndex == 0) {
            return rs;
        }
        if (columnIndex == getColumnCount() - 1) {
            return "";
        } else {
            final RemoteTargetAssessmentDocument rtad = getRemoteTargetAssessmentDocumentAtColumnIndex(columnIndex);
            return rtad == null ? null : rtad.selectGradeAccess(rs.getStudentId(), currentTerm.getScheduledItemId());
        }
    }

    @Override
    public void setValueAt(Object val, int rowIndex, int columnIndex) {
        if (!(val instanceof Grade)) {
            return;
        }
        Grade g = (Grade) val;
        checkIsAWT();
        RemoteStudent rs = students.get(rowIndex);
        RemoteTargetAssessmentDocument rtad = getRemoteTargetAssessmentDocumentAtColumnIndex(columnIndex);
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
        RemoteStudent rs = students.get(rowIndex);
        if (columnIndex != 0 && columnIndex != getColumnCount() - 1 && currentTerm != null) {
            RemoteTargetAssessmentDocument rtad = getRemoteTargetAssessmentDocumentAtColumnIndex(columnIndex);
            return rtad != null && rtad.getPreferredConvention() != null && rtad.select(rs.getStudentId(), currentTerm.getScheduledItemId()) != null;
        }
        return false;
    }

    synchronized String[] getTargetTypes() {
        return targetTypes.stream().toArray(String[]::new);
    }

    List<Term> getTerms() {
        return terms.stream().collect(Collectors.toList());
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getItem() instanceof Term && e.getStateChange() == ItemEvent.SELECTED) {
            Term term = (Term) e.getItem();
            setCurrentIndentity(term);
        } else if (e.getItem() instanceof String && e.getStateChange() == ItemEvent.SELECTED) {
            final String tt = (String) e.getItem();
            EventQueue.invokeLater(() -> setCurrentTargetType(tt));
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        pSupport.removePropertyChangeListener(l);
    }

    @NbBundle.Messages({"TargetsForStudentsModel.export.csv.filehint={0} {1} {2}-{3} ({4,date,d.M.yy HH'h'mm}).csv"})
    @Override
    public String createFileNameHint() throws IOException {
        Term term = getCurrentIndentity();
        if (term == null) {
            throw new IOException("No current Term selected.");
        }
        Node n = support.getNodeDelegate();
        //TODO defer CSV Export
        String jahr = Integer.toString((Integer) term.getParameter("jahr"));
        int hj = (Integer) term.getParameter("halbjahr");
        return NbBundle.getMessage(TargetsForStudentsModel.class, "TargetsForStudentsModel.export.csv.filehint", n.getDisplayName(), StringUtils.capitalize(getCurrentTargetType()), jahr, hj, new Date());
    }

    @NbBundle.Messages({"TargetsForStudentsModel.export.csv.name=Name"})
    @Override
    public byte[] getCSV() throws IOException {
        StringBuilder sb = new StringBuilder();
        StringJoiner header = new StringJoiner(";", "", "\n");
        header.add(NbBundle.getMessage(TargetsForStudentsModel.class, "TargetsForStudentsModel.export.csv.name"));
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

    private final class Listener implements GradeTermTargetAssessment.Listener, PropertyChangeListener {

        int index;

        @Override
        public void valueForStudentChanged(Object source, StudentId student, TermId gradeId, Grade old, Grade newGrade, Timestamp timestamp) {
            for (int i = 0; i < students.size(); i++) {
                if (students.get(i).getStudentId().equals(student)) {
                    TargetsForStudentsModel.this.fireTableRowsUpdated(i, i);
                    final int row = i;
                    Mutex.EVENT.writeAccess(() -> {
                        try {
                            fireTableCellUpdated(row, index + 1);
                        } catch (Exception e) {
                            PlatformUtil.getCodeNameBaseLogger(TargetsForStudentsModel.class).log(Level.FINE, e.getLocalizedMessage(), e);
                        }
                    });
                    final Term ci = getCurrentIndentity();
                    if (ci != null && source instanceof RemoteTargetAssessmentDocument) {
                        final RemoteTargetAssessmentDocument rtad = (RemoteTargetAssessmentDocument) source;
                        final Set<StudentId> studs = HideRTADColumns.isHideIfEmptyForPU() ? getRemoteUnitsModel().getStudentIds() : null;
                        hidden.possiblyUpdateAfterSetGrade(rtad.getDocumentId(), rtad.isEmptyFor(ci.getScheduledItemId(), studs));
                    }

                }
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            switch (evt.getPropertyName()) {
                case RemoteTargetAssessmentDocumentName.PROP_DISPLAYNAME:
//                    col.setHeaderValue("X"); //name.getDisplayName(true));
                    break;
                case RemoteTargetAssessmentDocument.PROP_VALUES:
                    EventQueue.invokeLater(() -> fireStructureChanged());
            }
        }
    }
}
