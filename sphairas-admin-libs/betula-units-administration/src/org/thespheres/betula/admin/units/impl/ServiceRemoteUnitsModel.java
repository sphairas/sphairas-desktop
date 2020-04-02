/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.impl;

import com.google.common.collect.Sets;
import com.google.common.math.DoubleMath;
import java.io.IOException;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.admin.units.AbstractUnitOpenSupport;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.admin.units.RemoteStudents;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.admin.units.RemoteUnitsModel;
import static org.thespheres.betula.admin.units.RemoteUnitsModel.PROP_TERMS;
import org.thespheres.betula.admin.units.util.Config;
import org.thespheres.betula.admin.units.util.Util;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Entry;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.util.DocumentUtilities;
import org.thespheres.betula.document.util.TargetAssessmentEntry;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.util.UnitInfo;
import org.thespheres.betula.services.util.Units;
import org.thespheres.betula.services.WorkingDate;
import org.thespheres.betula.services.ws.Paths;
import org.thespheres.betula.ui.util.LogLevel;

/**
 *
 * @author boris.heithecker
 */
public class ServiceRemoteUnitsModel extends RemoteUnitsModel {

    private final AtomicLong docsTime = new AtomicLong(0l);
    private final Set<DocumentId> docsPreparing = new HashSet<>();
    private final Set<DocumentId> prioDocsPreparing = new HashSet<>();

    public ServiceRemoteUnitsModel(String provider, AbstractUnitOpenSupport support, UnitId[] units) throws IOException {
        super(support, units, provider);
    }

    //Synchronized in run2
    @Override
    protected void doInit(final String prioSuffix, final INITIALISATION stage, final Reload rl) throws IOException, InterruptedException {

        int size = 0;
        int prioSize[] = new int[]{0};

        final boolean reloadStudents = rl.equals(Reload.WORKING_DATE) || rl.equals(Reload.UNIT_DOCUMENTS);
        boolean loaddoc;
        boolean loadstud;
        synchronized (initialization) {
            loaddoc = stage.satisfies(INITIALISATION.PRIORITY) && (rl.equals(Reload.TARGET_DOCUMENTS) || !getInitialization().satisfies(INITIALISATION.MAXIMUM));
            loadstud = stage.satisfies(INITIALISATION.STUDENTS) && (reloadStudents || !getInitialization().satisfies(INITIALISATION.STUDENTS));
        }

        long start = System.currentTimeMillis();

        if (loadstud) {
            final Map<UnitId, Set<StudentId>> unitStudents = new HashMap<>();
            final Map<UnitId, Marker[]> unitMarkers = new HashMap<>();

            long nanosLast = 0;
            for (final UnitId unit : units) {

                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }

                nanosLast = loadStudentsAndMarkers(unit, unitStudents, unitMarkers);
            }

            if (Thread.interrupted()) {
                throw new InterruptedException();
            }

            initStudentsLists(unitStudents, unitMarkers, reloadStudents);

            if (nanosLast != 0l) {
                studentsTime.set(nanosLast);
            }

            if (!initialization[0].satisfies(INITIALISATION.STUDENTS)) {
                updateInitialization(INITIALISATION.STUDENTS);
            }
        }

        boolean targetsChanged = false;
        long prioDur = 0l;
        if (loaddoc) {
//            if (getDocBean() == null) {
//                return;
//            }
            final Set<DocumentId> current;
            synchronized (targets) {
                current = targets.stream()
                        .map(RemoteTargetAssessmentDocument::getDocumentId)
                        .collect(Collectors.toSet());
            }
            docsPreparing.clear();
            prioDocsPreparing.clear();
            synchronized (documents) {
                size = documents.size();
                final Map<Boolean, Set<DocumentId>> m = documents.stream()
                        .filter(d -> !current.contains(d))
                        .collect(Collectors.groupingBy(d -> prioSuffix != null && d.getId().endsWith(prioSuffix), Collectors.toSet()));
                m.entrySet().forEach((Map.Entry<Boolean, Set<DocumentId>> e) -> {
                    final boolean priority = e.getKey();
                    final Set<DocumentId> dd = e.getValue();
                    if (priority) {
                        prioDocsPreparing.addAll(dd);
                    } else {
                        docsPreparing.addAll(dd);
                    }
                    prioSize[0] = prioDocsPreparing.size();
                    final int prio = priority ? Thread.NORM_PRIORITY : PRIORITY_BACKGROUND;
                    final int delay = 0;
                    final int mode = 2;
                    if (mode == 0) {
                        final Init init = new Init(dd, priority, !rl.equals(Reload.NO_RELOAD));
                        final RequestProcessor.Task lt = Util.RP(provider).create(init);
                        postUtilRPTask(lt, prio, delay);
                    } else if (mode == 1) {
                        dd.stream().forEach(d -> {
                            final Init i2 = new Init(Sets.newHashSet(d), priority, !rl.equals(Reload.NO_RELOAD));
                            final RequestProcessor.Task lt2 = Util.RP(provider).create(i2);
                            postUtilRPTask(lt2, prio, delay);
                        });
                    } else if (mode == 2) {
                        double max = 32d; //Must be double!
                        final double div = dd.size() / max;
                        final int ls = DoubleMath.roundToInt(div, RoundingMode.UP);
                        final List<Set<DocumentId>> l = IntStream.range(0, ls)
                                .mapToObj(i -> new HashSet<DocumentId>())
                                .collect(Collectors.toList());
                        dd.stream().forEachOrdered(d -> {
                            l.stream()
                                    .filter(s -> s.size() < max)
                                    .findFirst()
                                    .get()
                                    .add(d);
                        });
                        l.stream().forEach(d -> {
                            final Init i2 = new Init(Sets.newHashSet(d), priority, !rl.equals(Reload.NO_RELOAD));
                            final RequestProcessor.Task lt2 = Util.RP(provider).create(i2);
                            postUtilRPTask(lt2, prio, delay);
                        });
                    }
                });
//                documents.stream()
//                        .filter(d -> !current.contains(d))
//                        .forEach(d -> {
//
//                            final boolean priority = prioSuffix != null && d.getId().endsWith(prioSuffix);
//                            if (priority) {
//                                prioDocsPreparing.add(d);
//                            } else {
//                                docsPreparing.add(d);
//                            }
//                            prioSize[0] = prioDocsPreparing.size();
//                            final int prio = priority ? Thread.NORM_PRIORITY : PRIORITY_BACKGROUND;
//                            final Init init = new Init(d, priority, !rl.equals(Reload.NO_RELOAD));
//                            final int delay = 0;
//                            final RequestProcessor.Task lt = Util.RP(provider).create(init);
//                            postUtilRPTask(lt, prio, delay);
//                        });
            }

            if (!prioDocsPreparing.isEmpty()) {
                synchronized (prioDocsPreparing) {
                    while (!prioDocsPreparing.isEmpty()) {
                        if (System.currentTimeMillis() - start > Config.getDocumentsMaxLoadTime()) {
                            final String missing = prioDocsPreparing.stream()
                                    .map(DocumentId::getId)
                                    .collect(Collectors.joining(","));
                            throw new IOException("Configured maximum loading time for " + unitsString + " has expired. Missing priority documents: " + missing);
                        }
                        prioDocsPreparing.wait(Config.getTargetWaitTime());
                    }
                }

                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }

                synchronized (targets) {
                    targetsChanged = targets.addAll(prioTargetsLoading);
                    prioTargetsLoading.clear();
                }

                if (prioSuffix != null) {
                    prioDur = System.currentTimeMillis() - start;
                    if (!getInitialization().satisfies(INITIALISATION.PRIORITY)) {
                        updateInitialization(INITIALISATION.PRIORITY);
                    }
                }
            }

            synchronized (docsPreparing) {
                while (!docsPreparing.isEmpty()) {
                    if (System.currentTimeMillis() - start > Config.getDocumentsMaxLoadTime()) {
                        final String missing = docsPreparing.stream()
                                .map(DocumentId::getId)
                                .collect(Collectors.joining(","));
                        throw new IOException("Configured maximum loading time for " + unitsString + " has expired. Missing non-priority documents: " + missing);
                    }
                    docsPreparing.wait(Config.getTargetWaitTime());
                }
            }

            if (Thread.interrupted()) {
                throw new InterruptedException();
            }

            synchronized (targets) {
                targetsChanged = targetsChanged | targets.addAll(targetsLoading);
                targetsLoading.clear();
            }
        }
        if (targetsChanged) {
            firePropTargetsTask.schedule(RELOAD_WAIT_TIME);
        }

        long dur = System.currentTimeMillis() - start;
        updateInitialization(stage);

        switch (stage) {
            case MAXIMUM:
                LOGGER.log(Level.INFO, "Initialized view of unit(s) {0} ({2} documents) in {1} ms; {3} documents with priority suffix ready after {4} ms.", new Object[]{unitsString, Long.toString(dur), Integer.toString(size), Integer.toString(prioSize[0]), Long.toString(prioDur)});
                final String status = NbBundle.getMessage(RemoteUnitsModel.class, "RemoteUnitsModel.initialized.view.status.message", new Object[]{unitsResolved, unitsString, Long.toString(dur), Integer.toString(size)});
                StatusDisplayer.getDefault().setStatusText(status);
                break;
            case PRIORITY:
                LOGGER.log(Level.INFO, "Initialized partial view of unit(s) {0} ({2} documents) in {1} ms; {3} documents with priority suffix ready after {4} ms.", new Object[]{unitsString, Long.toString(dur), Integer.toString(size), Integer.toString(prioSize[0]), Long.toString(prioDur)});
                break;
            case STUDENTS:
                LOGGER.log(Level.INFO, "Initialized students set(s) of unit(s) {0} in {1} ms.", new Object[]{unitsString, Long.toString(dur)});
                final String status2 = NbBundle.getMessage(RemoteUnitsModel.class, "RemoteUnitsModel.initialized.unit.status.message", new Object[]{unitsResolved, unitsString, Long.toString(dur), Integer.toString(size)});
                StatusDisplayer.getDefault().setStatusText(status2);
                break;
        }
    }

    @Override
    protected void loadDocs(final TermId term) throws IOException, InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        LOGGER.log(Level.FINE, "loadDocs: {0} , Term: {1}", new Object[]{this.unitsString, term == null ? "" : term.getId().toString()});
        final boolean linked = Boolean.parseBoolean(support.getLoadingProperties().getProperty("linked"));
        final boolean bps = Boolean.parseBoolean(support.getLoadingProperties().getProperty("by-primary-suffix"));
        final DocumentId[] docs;
        if (bps) {
            docs = Arrays.stream(units)
                    .map(this::createDocumentId)
                    .toArray(DocumentId[]::new);
        } else {
            final DocumentId[] udocs = Arrays.stream(units)
                    .map(documentsModel::convertToUnitDocumentId)
                    .toArray(DocumentId[]::new);
            docs = ServiceTargetAssessmentDocumentFactory.get(provider).getTargetAssessmentDocuments(udocs, term, linked);
        }
        final long nanos = System.nanoTime();
        synchronized (documents) {
            Arrays.stream(docs)
                    .forEach(documents::add);
        }
        if (term == null) {
            docsTime.set(nanos);
        }
    }

    private DocumentId createDocumentId(final UnitId uid) {
        String id = uid.getId();
        final String suffix = "-" + documentsModel.getModelPrimarySuffix();
        if (!id.endsWith(suffix)) {
            id += suffix;
        }
        return new DocumentId(uid.getAuthority(), id, DocumentId.Version.LATEST);
    }

    @Override
    protected long loadStudentsAndMarkers(final UnitId unit, final Map<UnitId, Set<StudentId>> students, final Map<UnitId, Marker[]> markers) throws IOException {
        LocalDateTime asOf = null;
        final WorkingDate wd = Lookup.getDefault().lookup(WorkingDate.class);
        if (wd != null && !wd.isNow()) {
            asOf = wd.getCurrentWorkingLocalDate().atStartOfDay();
        }
        final UnitInfo ui = Units.get(provider).get()
                .fetchParticipants(unit, asOf, UnitInfo::new, Util.RP(provider));
        final Set<StudentId> studs = Arrays.stream(ui.getStudents())
                .collect(Collectors.toSet());
        final long nanos = System.nanoTime();
        students.put(unit, studs);
        if (markers != null) {
            markers.put(unit, ui.getResponseUnitEntry().getValue().markers());
        }
        return nanos;
    }

    @Override
    protected DocumentId[] fetchTargetAssessmentDocuments(final StudentId[] arr) throws IOException {
        final boolean linked = Boolean.parseBoolean(support.getLoadingProperties().getProperty("linked"));
        return ServiceTargetAssessmentDocumentFactory.get(provider).getTargetAssessmentDocuments(arr, null, linked);
    }

    @Override
    protected RemoteStudent createRemoteStudent(StudentId sid) {
        return RemoteStudents.find(provider, sid);
    }

    @Override
    protected boolean isRemoteException(Exception ex) {
        return Util.isServiceException(ex);
    }

    protected void onRespone(final Container response) {
        final String[] path = Paths.UNITS_TARGETS_PATH;
        final List<TargetAssessmentEntry> l = DocumentUtilities.findEnvelope(response, path).stream()
                .filter(TargetAssessmentEntry.class::isInstance)
                .map(TargetAssessmentEntry.class::cast)
                .collect(Collectors.toList());
        final Set<StudentId> studs = getStudentIds();
        for (final TargetAssessmentEntry tae : l) {
            final DocumentId d = tae.getIdentity();
            final boolean contained;
            synchronized (documents) {
                contained = documents.contains(d);
            }
            if (!contained) {
                boolean affected = tae.getChildren().stream()
                        .filter(Entry.class::isInstance)
                        .map(Entry.class::cast)
                        .filter(e -> TermId.class.isInstance(e.getIdentity()))
                        .flatMap(e -> e.getChildren().stream())
                        .filter(Entry.class::isInstance)
                        .map(Entry.class::cast)
                        .map(Entry::getIdentity)
                        .filter(StudentId.class::isInstance)
                        .map(StudentId.class::cast)
                        .anyMatch(studs::contains);
                if (affected) {
                    postForceReload(Reload.TARGET_DOCUMENTS);
                }
            } else {
                //TODO pass to RTAD
                final Set<TermId> terms = tae.getChildren().stream()
                        .filter(Entry.class::isInstance)
                        .map(Entry.class::cast)
                        .map(Entry::getIdentity)
                        .filter(TermId.class::isInstance)
                        .map(TermId.class::cast)
                        .collect(Collectors.toSet());
                final boolean fire;
                synchronized (terms) {
                    fire = terms.addAll(terms);
                }
                if (fire) {
                    pSupport.firePropertyChange(PROP_TERMS, null, terms);
                }
            }
        }
        //TODO Check units students, markers
    }

    private final class Init implements Runnable {

        private final Set<DocumentId> docs;
        private final boolean isPrio;
        private final boolean fireChanges;
        private Map<DocumentId, ServiceRemoteTargetAssessmentDocument> rtad;
        private int numTrial = 0;

        private Init(final Set<DocumentId> d, final boolean isPriorityDoc, final boolean fire) {
            this.fireChanges = fire;
            this.docs = d;
            this.isPrio = isPriorityDoc;
        }

        @Override
        public void run() {
//            remote.login();
            try {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                rtad = ServiceTargetAssessmentDocumentFactory.get(provider).getAll(docs);
                if (isPrio) {
                    prioTargetsLoading.addAll(rtad.values());
                } else {
                    targetsLoading.addAll(rtad.values());
                }
                removeLoading();
                final Set<TermId> tid = rtad.values().stream()
                        .flatMap(r -> r.identities().stream())
                        .collect(Collectors.toSet());
                synchronized (terms) {
                    final boolean changed = terms.addAll(tid);
                    if (changed && fireChanges) {
                        Util.NOTIFICATIONS.post(() -> pSupport.firePropertyChange(PROP_TERMS, null, terms));
                    }
                }
            } catch (IOException ex) {
                final int num = numTrial;
                final String idtext = docs.stream()
                        .map(DocumentId::getId)
                        .collect(Collectors.joining(","));
                if (isRemoteException(ex) && numTrial++ < Config.getInstance().getRetryTimes().length) {
                    docs.forEach(ServiceTargetAssessmentDocumentFactory.get(provider)::removeTargetAssessmentDocument);
                    final int wait = Config.getInstance().getRetryTimes()[num];
                    String message = NbBundle.getMessage(RemoteUnitsModel.class, "RemoteUnitsModel.model.status.retry", numTrial, idtext);
                    LOGGER.log(LogLevel.INFO, message);
                    int priority = isPrio ? Thread.NORM_PRIORITY : PRIORITY_BACKGROUND;
                    final RequestProcessor.Task lt = Util.RP(provider).create(this);
                    postUtilRPTask(lt, wait, priority);
                } else {
                    removeLoading();
                    final String name = docs.stream()
                            .map(d -> {
                                try {
                                    return namingResolver.resolveDisplayName(d);
                                } catch (IllegalAuthorityException illaex) {
                                    return d.getId();
                                }
                            })
                            .collect(Collectors.joining(","));
                    final String message = NbBundle.getMessage(RemoteUnitsModel.class, "RemoteUnitsModel.target.status.initError", name, idtext);
                    notifyError(ex, message);
                }
            } catch (InterruptedException ex) {
                removeLoading();
            }
        }

        private void removeLoading() {
            if (isPrio) {
                synchronized (prioDocsPreparing) {
                    prioDocsPreparing.removeAll(docs);
                    prioDocsPreparing.notify();
                }
            } else {
                synchronized (docsPreparing) {
                    docsPreparing.removeAll(docs);
                    docsPreparing.notify();
                }
            }
        }

        @Override
        public int hashCode() {
            int hash = 5;
            return 59 * hash + Objects.hashCode(this.docs);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Init other = (Init) obj;
            return Objects.equals(this.docs, other.docs);
        }

    }

    @ServiceProvider(service = RemoteUnitsModel.Factory.class)
    public static class Factory implements RemoteUnitsModel.Factory {

        @Override
        public String id() {
            return "service";
        }

        @Override
        public RemoteUnitsModel create(String provider, AbstractUnitOpenSupport support, UnitId[] units) throws IOException {
            return new ServiceRemoteUnitsModel(provider, support, units);
        }

        @Override
        public RemoteTargetAssessmentDocument find(String provider, DocumentId target) throws IOException {
            return ServiceTargetAssessmentDocumentFactory.get(provider).getTargetAssessmentDocument(target);
        }

    }
}
