/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units;

import java.awt.EventQueue;
import org.thespheres.betula.document.model.UnitsModel;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.util.WeakListeners;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.admin.units.util.Config;
import org.thespheres.betula.admin.units.ui.RemoteUnitsProgressUI;
import org.thespheres.betula.admin.units.util.Util;
import org.thespheres.betula.services.jms.AbstractDocumentEvent.DocumentEventType;
import org.thespheres.betula.services.jms.MultiTargetAssessmentEvent;
import org.thespheres.betula.services.jms.MultiTargetAssessmentEvent.Update;
import org.thespheres.betula.services.jms.UnitDocumentEvent;
import org.thespheres.betula.services.client.jms.JMSListener;
import org.thespheres.betula.services.client.jms.JMSTopicListenerService;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.model.DocumentsModel;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.WorkingDate;
import org.thespheres.betula.services.jms.JMSTopic;
import org.thespheres.betula.ui.util.LogLevel;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
@Messages({"RemoteUnitsModel.initialized.view.status.message=Ansicht der Gruppe(n) „{0}“ [{1}] ({3} Listen) in {2} ms aufgebaut.",
    "RemoteUnitsModel.initialized.unit.status.message=Gruppe(n) „{0}“ [{1}] ({3} Listen) in {2} ms geladen.",
    "RemoteUnitsModel.target.status.initError=Liste(n) „{0}“ [{1}] konnte(n) nicht geladen werden.",
    "RemoteUnitsModel.unit.status.initError=Gruppe „{0}“ [{1}] konnte nicht geladen werden.",
    "RemoteUnitsModel.model.status.initError=Gruppe(n) „{0}“ [{1}] konnte(n) nicht geöffnet werden.",
    "RemoteUnitsModel.model.status.retry=Enqueuing {0}. retry to load {1}.",
    "RemoteUnitsModel.status.initError.title=Fehler beim Laden der Listen"})
public abstract class RemoteUnitsModel implements UnitsModel<RemoteStudent, RemoteTargetAssessmentDocument>, Runnable { //implements LookupListener {

    public static final Logger LOGGER = Logger.getLogger(RemoteUnitsModel.class.getCanonicalName());
    static final RequestProcessor RP = new RequestProcessor(RemoteUnitsModel.class.getName(), 64, true);
    protected final INITIALISATION[] initialization = new INITIALISATION[]{INITIALISATION.NO_INITIALISATION};
    private final AtomicReference<INITIALISATION> lastCalled = new AtomicReference<>(null);
    private final AtomicReference<String> priorityTarget = new AtomicReference<>(null);
    private final AtomicReference<TermId> priorityTerm = new AtomicReference<>(null);
    private final AtomicInteger count = new AtomicInteger(0);
    protected final AtomicLong studentsTime = new AtomicLong(0l);
    protected static final int RELOAD_WAIT_TIME = 2000;
    public static final String PROP_INITIALISATION = "initialization";
    public static final String PROP_INITIALIZING = "initializing.process";
    public static final String PROP_STUDENTS = "students";
    public static final String PROP_MARKERS = "markers";
    public static final String PROP_TARGETS = "targets";
    public static final String PROP_TERMS = "terms";
    protected final AbstractUnitOpenSupport support;
    protected final JMSTopicListenerService jmsDocService;
    protected final HashSet<RemoteTargetAssessmentDocument> targets = new HashSet<>();
    protected final HashSet<RemoteTargetAssessmentDocument> targetsLoading = new HashSet<>();
    protected final HashSet<RemoteTargetAssessmentDocument> prioTargetsLoading = new HashSet<>();
    private final Set<StudentId> studentIds = new HashSet<>();
    private final Set<StudentId> studentsRespectedInTargets = new HashSet<>();
    private final List<RemoteStudent> students = new ArrayList<>();
    private final Map<UnitId, Marker[]> markers = new HashMap<>();
    protected final SortedSet<TermId> terms = new TreeSet<>(Comparator.comparingInt(TermId::getId));
    protected final PropertyChangeSupport pSupport = new PropertyChangeSupport(this);
    protected final Set<DocumentId> documents = new HashSet<>();
    protected final NamingResolver namingResolver;
    private final AtomicReference<Reload> reload = new AtomicReference<>(Reload.NO_RELOAD);
    protected final UnitId[] units;
    private final Set<DocumentId> unitDocuments;
    protected final int PRIORITY_BACKGROUND = 3;
    private final RequestProcessor.Task task;
    private final Set<RequestProcessor.Task> targetLoadTasks = new HashSet<>();
    protected final RequestProcessor.Task firePropTargetsTask;
    private final WorkingDateListener wdListener = new WorkingDateListener();
    protected final DocumentsModel documentsModel;
    private final TargetsListener tListener = new TargetsListener();
    private final UnitsListener uListener = new UnitsListener();
    private boolean initJMS;
    protected final String unitsString;
    protected final String unitsResolved;
    protected final String provider;
//    private final VCardStudents vCards;
//    private final RemoteUnitsModel2 testdel;

    public enum INITIALISATION {

        ERROR(0, true),
        NO_INITIALISATION(0),
        STUDENTS(10),
        //        PRIO_TARGET_TYPE(20),
        PRIORITY(30),
        MAXIMUM(100);
        private final int level;
        private final boolean error;

        private INITIALISATION(int l) {
            this(l, false);
        }

        private INITIALISATION(int l, boolean error) {
            this.level = l;
            this.error = error;
        }

        public int getLevel() {
            return level;
        }

        public boolean satisfies(INITIALISATION stage) {
            return stage != null && stage.getLevel() <= getLevel();
        }

        public boolean isError() {
            return error;
        }

    }

    protected enum Reload {
        NO_RELOAD,
        WORKING_DATE,
        UNIT_DOCUMENTS,
        TARGET_DOCUMENTS
    }

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    protected RemoteUnitsModel(final AbstractUnitOpenSupport support, final UnitId[] units, final String provider) throws IOException {
        this.support = support;
        this.units = units;
        this.provider = provider;
        jmsDocService = this.support.findJMSTopicListenerService(JMSTopic.DOCUMENTS_TOPIC.getJmsResource());
//        vCards = VCardStudents.get(support.findBetulaProjectProperties());
        namingResolver = this.support.findNamingResolver();
        documentsModel = support.findDocumentsModel();
        unitDocuments = Arrays.stream(units)
                .map(documentsModel::convertToUnitDocumentId)
                .collect(Collectors.toSet());
        task = RP.create(this);
        task.setPriority(PRIORITY_BACKGROUND);
        firePropTargetsTask = Util.NOTIFICATIONS.create(() -> pSupport.firePropertyChange(PROP_TARGETS, null, null));
        unitsString = Arrays.stream(units)
                .map(UnitId::getId)
                .collect(Collectors.joining(","));
        final Term currentTerm = support.findTermSchedule().getCurrentTerm();
        unitsResolved = Arrays.stream(units)
                .map(u -> {
                    try {
                        return namingResolver.resolveDisplayNameResult(u).getResolvedName(currentTerm);
                    } catch (IllegalAuthorityException illaex) {
                        return "???";
                    }
                })
                .collect(Collectors.joining(", "));
        final PropertyChangeListener pcl = RemoteUnitsProgressUI.getDefault().createListener(this);
        addPropertyChangeListener(pcl);
        final WorkingDate wd = Lookup.getDefault().lookup(WorkingDate.class);
        wd.addChangeListener(WeakListeners.change(wdListener, wd));
    }

    void initialize(final INITIALISATION stage, final String preferredSuffix, final TermId prioTerm) {
        if (stage != null
                && !getInitialization().satisfies(stage)
                && stage.getLevel() > Optional.ofNullable(lastCalled.getAndUpdate(before -> before == null || stage.satisfies(before) ? stage : before)).map(INITIALISATION::getLevel).orElse(0)) {
            startIntialization(stage, preferredSuffix, prioTerm, 0);
        }
    }

    private void startIntialization(final INITIALISATION request, final String preferredSuffix, final TermId prioTerm, final int retry) {
        final INITIALISATION stage = lastCalled.updateAndGet(before -> before == null || request.satisfies(before) ? request : before);
        int prio = stage == INITIALISATION.STUDENTS ? PRIORITY_BACKGROUND : Thread.NORM_PRIORITY;
        synchronized (task) {
            if (preferredSuffix != null) {
                priorityTarget.set(preferredSuffix);
            }
            if (prioTerm != null) {
                priorityTerm.set(prioTerm);
            }
            count.set(retry);
            task.setPriority(Math.max(task.getPriority(), prio));
            task.schedule(0);
        }
    }

    public AbstractUnitOpenSupport getUnitOpenSupport() {
        return support;
    }

    public UnitId[] getUnits() {
        return units;
    }

    //Synchronized
    @Override
    public void run() {

        final INITIALISATION stage = lastCalled.get();
        final int numTrial = count.get();

        if (!RP.isRequestProcessorThread()) {
            LOGGER.log(Level.INFO, "RemoteUnitsModel.run must be called from RemoteLookup RequestProcessor thread.");
            return;
        }

        final Reload rl = reload.getAndSet(Reload.NO_RELOAD);

        synchronized (initialization) {
            if (stage == null || (getInitialization().satisfies(stage) && rl.equals(Reload.NO_RELOAD))) {
                return;
            }
            updateInitializing(true);
        }

        final String preferredSuffix;
        if (getInitialization().satisfies(INITIALISATION.MAXIMUM)) {
            preferredSuffix = null;
        } else {
            preferredSuffix = priorityTarget.get();
        }
        final TermId prioTerm;
        if (getInitialization().satisfies(INITIALISATION.MAXIMUM)) {
            prioTerm = null;
        } else {
            prioTerm = priorityTerm.get();
        }

        final String log = "(Re-)Initializing " + unitsString + " to stage " + (stage == null ? "null" : stage.toString());
        LOGGER.log(Level.INFO, log);

        try {
//            Domain.ensureRunning(support.findDomainProviderUrl());

            if (stage.satisfies(INITIALISATION.PRIORITY) && (rl.equals(Reload.TARGET_DOCUMENTS) || !getInitialization().satisfies(INITIALISATION.PRIORITY))) {
                loadDocs(prioTerm);
            }

            if (stage.satisfies(INITIALISATION.PRIORITY)) {
                if (stage == INITIALISATION.PRIORITY && (preferredSuffix == null || prioTerm == null)) {
                    LOGGER.log(Level.INFO, "RemoteUnitsModel can be initialize to explicit stage {0} only if argument \"preferredSuffix\" and \"prioTerm\" are specified.", INITIALISATION.PRIORITY.name());
                    doInit(null, INITIALISATION.MAXIMUM, rl);
                } else {
                    doInit(preferredSuffix, INITIALISATION.PRIORITY, rl);
                }
            } else {
                doInit(null, stage, rl);
            }

            if (jmsDocService != null && !initJMS) {
                jmsDocService.registerListener(MultiTargetAssessmentEvent.class, tListener);
                jmsDocService.registerListener(UnitDocumentEvent.class, uListener);
                initJMS = true;
            }

            //Load non-prio term
            if (prioTerm != null && stage.satisfies(INITIALISATION.MAXIMUM) && (rl.equals(Reload.TARGET_DOCUMENTS) || !getInitialization().satisfies(INITIALISATION.MAXIMUM))) {
                loadDocs(null);
                doInit(null, INITIALISATION.MAXIMUM, rl);
            } else if (rl.equals(Reload.WORKING_DATE)) {
                final StudentId[] arr = getStudentIds().stream()
                        .filter(sid -> !studentsRespectedInTargets.contains(sid))
                        .toArray(StudentId[]::new);
                final DocumentId[] docs = fetchTargetAssessmentDocuments(arr);
                synchronized (documents) {
                    Arrays.stream(docs)
                            .forEach(documents::add);
                }
                doInit(null, INITIALISATION.MAXIMUM, rl);
            }

            studentsRespectedInTargets.addAll(studentIds);

            count.set(0);

        } catch (Exception ex) {
            final int num = numTrial + 1;
            reload.compareAndSet(Reload.NO_RELOAD, rl);
            if (isRemoteException(ex) && numTrial < Config.getInstance().getRetryTimes().length) {
                final int wait = Config.getInstance().getRetryTimes()[numTrial];
                final String message = NbBundle.getMessage(RemoteUnitsModel.class, "RemoteUnitsModel.model.status.retry", num, unitsString);
                LOGGER.log(LogLevel.INFO, message);
//                startIntialization(stage, preferredSuffix, prioTerm, num);
                count.set(num);
                task.schedule(wait);
            } else {
                count.set(0);
                lastCalled.set(null);
                updateInitialization(INITIALISATION.ERROR);
                final String message = NbBundle.getMessage(RemoteUnitsModel.class, "RemoteUnitsModel.model.status.initError", unitsResolved, unitsString);
                notifyError(ex, message);
            }
        } finally {
            synchronized (initialization) {
                updateInitializing(false);
            }
        }
    }

    protected abstract DocumentId[] fetchTargetAssessmentDocuments(final StudentId[] arr) throws IOException;

    //Synchronized in run2
    protected abstract void doInit(final String prioSuffix, final INITIALISATION stage, final Reload rl) throws IOException, InterruptedException;

    protected abstract void loadDocs(final TermId term) throws IOException, InterruptedException;

    protected void postUtilRPTask(final RequestProcessor.Task lt, final int prio, final int delay) {
        lt.addTaskListener(t -> {
            synchronized (targetLoadTasks) {
                targetLoadTasks.remove(lt);
            }
        });
        synchronized (targetLoadTasks) {
            targetLoadTasks.add(lt);
        }
        lt.setPriority(prio);
        lt.schedule(delay);
    }

    protected void notifyError(Exception ex, String message) {
        LOGGER.log(LogLevel.INFO_WARNING, ex.getMessage(), ex);
        final Icon ic = ImageUtilities.loadImageIcon("org/thespheres/betula/ui/resources/exclamation-red-frame.png", true);
        final String title = NbBundle.getMessage(RemoteUnitsModel.class, "RemoteUnitsModel.status.initError.title");
        NotificationDisplayer.getDefault()
                .notify(title, ic, message, null, NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.WARNING);
    }

    protected void postForceReload(final Reload reason) {
        //Check initializing
        reload.set(reason);
//                count.set(0);
        task.schedule(RELOAD_WAIT_TIME);
//                Util.RP(remote).post(() -> reloadDocs(source));
    }

    protected abstract long loadStudentsAndMarkers(final UnitId unit, final Map<UnitId, Set<StudentId>> students, final Map<UnitId, Marker[]> markers) throws IOException;

    protected void updateInitializing(boolean v) {
//        boolean before = this.initializing2;
//        this.initializing2 = v;
        pSupport.firePropertyChange(PROP_INITIALIZING, !v, v);
    }

    protected synchronized void updateInitialization(final INITIALISATION stage) {
        final INITIALISATION before;
        synchronized (initialization) {
            before = initialization[0];
            initialization[0] = stage;
        }
        pSupport.firePropertyChange(PROP_INITIALISATION, before, stage);
    }

    protected synchronized void initStudentsLists(final Map<UnitId, Set<StudentId>> unitStudents, final Map<UnitId, Marker[]> unitMarkers, final boolean fire) {
        final Set<StudentId> studentsBefore;
        final Map<UnitId, Marker[]> markersBefore;
        synchronized (students) {
            studentsBefore = studentIds.stream().collect(Collectors.toSet());
            markersBefore = markers.entrySet().stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue));
            studentIds.clear();//TODO: what if only one unit has been updated??
            students.clear();
            unitStudents.values().stream()
                    .flatMap(Set::stream)
                    .distinct()
                    .peek(studentIds::add)
                    .map(this::createRemoteStudent)
                    .sorted()
                    .forEach(students::add);

            if (getUnitOpenSupport() instanceof PrimaryUnitOpenSupport) {
                students.forEach(rs -> rs.putClientProperty(AbstractUnitOpenSupport.class
                        .getCanonicalName(), getUnitOpenSupport()));
            }
            if (unitMarkers != null) {
                markers.clear();
                markers.putAll(unitMarkers);
            }
        }
        if (fire) {
            pSupport.firePropertyChange(PROP_STUDENTS, studentsBefore, studentIds);
            pSupport.firePropertyChange(PROP_MARKERS, markersBefore, markers);
        }
    }

    protected abstract RemoteStudent createRemoteStudent(final StudentId sid);

    public INITIALISATION getInitialization() {
        return initialization[0];
    }

    @Messages("RemoteUnitsModel.cannot.cancel.message=Task {0} is already running.")
    public boolean cancelLoading() {
        final boolean res = task.cancel();
        if (!res) {
            final String msg = NbBundle.getMessage(RemoteUnitsModel.class, "RemoteUnitsModel.cannot.cancel.message", unitsString);
            LOGGER.log(Level.INFO, msg);
//            RP.stop();
        }
        final Set<RequestProcessor.Task> snapshot;
        synchronized (targetLoadTasks) {
            snapshot = new HashSet<>(targetLoadTasks);
        }
        snapshot.forEach(t -> {
            final boolean cancelled = t.cancel();//This causes ConcurrentMod exc. if not from snapshot!
            if (!cancelled) {
                final String msg = NbBundle.getMessage(RemoteUnitsModel.class, "RemoteUnitsModel.cannot.cancel.message", t.toString());
                LOGGER.log(Level.INFO, msg);
            }
        });
        return true; //res;
    }

    @Override
    public Set<RemoteTargetAssessmentDocument> getTargets() {
        synchronized (targets) {
            return targets.stream()
                    .collect(Collectors.toSet());
        }
    }

    @Override
    public RemoteTargetAssessmentDocument getTarget(final DocumentId did) {
        synchronized (targets) {
            return targets.stream()
                    .filter(rtad -> rtad.getDocumentId().equals(did))
                    .collect(CollectionUtil.singleOrNull());
        }
    }

    @Override
    public List<RemoteStudent> getStudents() {
        synchronized (students) {
            return students.stream()
                    .collect(Collectors.toList());
        }
    }

    public Set<StudentId> getStudentIds() {
        synchronized (students) {
            return studentIds.stream()
                    .collect(Collectors.toSet());
        }
    }

    public Marker[] getUnitMarkers(final UnitId unit) {
        synchronized (students) {
            return markers.get(unit);
        }
    }

    public Optional<RemoteStudent> findRemoteStudent(StudentId sid) {
        synchronized (students) {
            return students.stream().filter(rs -> rs.getStudentId().equals(sid)).findAny();
        }
    }

    @Override
    public Set<TermId> getTerms() {
        synchronized (terms) {
            return terms.stream()
                    .collect(Collectors.toSet());
        }
    }

    protected abstract boolean isRemoteException(Exception ex);

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pSupport.removePropertyChangeListener(listener);
    }

    private class UnitsListener implements JMSListener<UnitDocumentEvent> {

        @Override
        public void addNotify() {
        }

        @Override
        public void removeNotify() {
        }

        @Override
        public void onMessage(UnitDocumentEvent event) {
            final DocumentId source = event.getSource();
            final DocumentEventType det = event.getType();
            if (det.equals(DocumentEventType.CHANGE) && unitDocuments.contains(source)) {//Chagen
//                final UnitId uid = documentsModel.convertToUnitId(source);
                postForceReload(Reload.UNIT_DOCUMENTS);
//                Util.RP(remote).post(() -> {
//                    reloadStudents(uid, true);
//                });

            }
        }

    }

    private class TargetsListener implements JMSListener<MultiTargetAssessmentEvent> {

        @Override
        public void addNotify() {
        }

        @Override
        public void removeNotify() {
        }

        @Override
        public void onMessage(MultiTargetAssessmentEvent evt) {
            final MultiTargetAssessmentEvent<TermId> event = evt;
            final DocumentId source = event.getSource();
            final DocumentEventType det = event.getType();
            final boolean contained;
            synchronized (documents) {
                contained = documents.contains(source);
            }
            if (det.equals(DocumentEventType.REMOVE) && contained) { //Targets
                final RequestProcessor.Task lt = Util.RP(provider).create(() -> {
                    //Check initializing
//                    task.waitFinished();
                    if (documents.remove(source)) {
                        final boolean changed;
                        synchronized (targets) {
                            changed = targets.stream()
                                    .filter(rtad -> rtad.getDocumentId().equals(source))
                                    .findAny()
                                    .map(targets::remove)
                                    .orElse(false);
                        }
                        if (changed) {
                            firePropTargetsTask.schedule(RELOAD_WAIT_TIME);
                        }
                    }
                });
                postUtilRPTask(lt, 0, Thread.NORM_PRIORITY);
            } else if (det.equals(DocumentEventType.ADD)) {
                postForceReload(Reload.TARGET_DOCUMENTS);
            } else if (det.equals(DocumentEventType.CHANGE)) {
                final Update<TermId>[] updates = event.getUpdates();
                if (!contained) {
                    if (updates != null) {
                        final Set<StudentId> sid = getStudentIds();
                        final boolean reload = Arrays.stream(updates)
                                .filter(u -> u.getValue() != null && u.getOldValue() == null)
                                .map(u -> u.getStudent())
                                .anyMatch(sid::contains);
                        if (reload) {
                            postForceReload(Reload.TARGET_DOCUMENTS);
                        }
                    }
                } else {
                    if (updates != null) {
                        //final Set<TermId> terms = getTerms();
//                    final boolean fire = updates != null && !Arrays.stream(updates)
//                            .map(Update::getGradeId)
//                            .allMatch(terms::contains);
                        final Set<TermId> uTerms = Arrays.stream(updates)
                                .filter(u -> u.getValue() != null)
                                .map(Update::getGradeId)
                                .collect(Collectors.toSet());
                        final boolean fire;
                        synchronized (terms) {
                            fire = terms.addAll(uTerms);
                        }
                        if (fire) {
                            pSupport.firePropertyChange(PROP_TERMS, null, terms);
                        }
                    }
                }
            }
        }

    }

    private class WorkingDateListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            if (getInitialization().satisfies(INITIALISATION.PRIORITY)) {
                final WorkingDate wd = Lookup.getDefault().lookup(WorkingDate.class);
                final WorkingDate.Updater wdUpdater = wd.markUpdating();
//                Util.RP(RemoteUnitsModel.this.remote).post(() -> {
//                reloadStudents(null, false);
                final long current = studentsTime.get();
                class TL implements TaskListener {

                    @Override
                    public void taskFinished(Task task) {
                        if (studentsTime.get() > current) {
                            task.removeTaskListener(this);
                            EventQueue.invokeLater(wdUpdater::unmarkUpdating);
                        }
                    }

                }
                postForceReload(Reload.WORKING_DATE);
                task.addTaskListener(new TL());
            }
        }

    }

    public static interface Factory {

        public String id();

        public RemoteUnitsModel create(String provider, AbstractUnitOpenSupport support, UnitId[] units) throws IOException;
        
        public RemoteTargetAssessmentDocument find(String provider, DocumentId target) throws IOException;
    }

}
