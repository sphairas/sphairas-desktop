/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units;

import java.awt.EventQueue;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.swing.undo.CompoundEdit;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.thespheres.betula.assess.GradeFactory;
import org.thespheres.betula.Identity;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.admin.units.util.Config;
import org.thespheres.betula.document.model.UnitsModel.UnitsModelDocument;
import org.thespheres.betula.admin.units.ui.RTADName2;
import org.thespheres.betula.admin.units.util.Util;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GradeTermTargetAssessment;
import org.thespheres.betula.assess.TargetAssessment;
import org.thespheres.betula.assess.TargetDocument;
import org.thespheres.betula.services.jms.AbstractDocumentEvent.DocumentEventType;
import org.thespheres.betula.services.jms.MultiTargetAssessmentEvent;
import org.thespheres.betula.services.client.jms.JMSListener;
import org.thespheres.betula.services.client.jms.JMSTopicListenerService;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.document.util.AbstractDocumentValidity;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.assess.IdentityTargetAssessment;
import org.thespheres.betula.services.WorkingDate;
import org.thespheres.betula.ui.util.LogLevel;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
public abstract class RemoteTargetAssessmentDocument extends AbstractTargetAssessmentDocument implements GradeTermTargetAssessment, TargetDocument, UnitsModelDocument {

    public static final String PROP_VALUES = "values";
    public static final String PROP_SIGNEES = "signees";
    public static final String PROP_PREFERRED_CONVENTIUON = "preferred-convention";
    private final Listener listener = new Listener();
    protected final Set<TermId> termsLoaded = new HashSet<>();//TODO: use this to keep track of loaded term. Provide an option to load an empty RTAD in inital phase. 
    protected final Map<StudentId, Map<TermId, RemoteGradeEntry>> values; // = new HashMap<>();
    private final EventListenerList listeners = new EventListenerList();
    private String prefConDN;
    final static int[] UPDATE_DELAY = new int[]{500, 1500, 5000};
    private final RTADName2 name;
    private final WorkingDateListener wdListener = new WorkingDateListener();
    //Do not use this RequestProcessor for loads/updates. Too many RemoteTargetAssessmentDocument in memory may call to may concurrent requests. 
    //Do not use Util.RP for submits. Causes optimistic look exceptions.
    protected final RequestProcessor submitRP;

    @SuppressWarnings("LeakingThisInConstructor")
    protected RemoteTargetAssessmentDocument(DocumentId d, String provider, final Map<StudentId, Map<TermId, RemoteGradeEntry>> values, final JMSTopicListenerService jms, NamingResolver nr) {
        super(d, provider, jms, nr);
        this.submitRP = new RequestProcessor(d.toString());
        this.values = values;
        name = new RTADName2(this);
        final WorkingDate wd = Lookup.getDefault().lookup(WorkingDate.class);
        wd.addChangeListener(WeakListeners.change(wdListener, wd));
    }

    @Override
    public Set<StudentId> students() {
        return values.keySet();
    }

    @Override
    public Set<TermId> identities() {
        return values.values().stream()
                .map(Map::keySet)
                .flatMap(Set::stream)
                .distinct()
                .collect(Collectors.toSet()); //.collect(HashSet::new, (r, s) -> r.addAll(s), HashSet::addAll);
    }

    public boolean isEmptyFor(final TermId term, final Collection<StudentId> students) {
        if (students == null) {
            return values.values().stream()
                    .noneMatch(m -> m.containsKey(term));
        } else {
            return students.stream()
                    .noneMatch(sid -> values.getOrDefault(sid, Collections.EMPTY_MAP).containsKey(term));
        }
    }

    public RemoteTargetAssessmentDocumentName getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return getName().getDisplayName(null);
    }

    public String getPreferredConventionDisplayName() {
        if (prefConDN == null && getPreferredConvention() != null) {
            AssessmentConvention ac = GradeFactory.findConvention(getPreferredConvention());
            if (ac != null) {
                prefConDN = ac.getDisplayName();
            } else {
                prefConDN = getPreferredConvention();
            }
        }
        return prefConDN;
    }

    public Signee getSignee(String entitlement) {
        return getSignees().get(entitlement);
    }

    protected abstract void updateSigneesAndMarkersAndProperties();

    public Optional<RemoteGradeEntry> selectGradeAccess(StudentId student, TermId term) {
        return Optional.ofNullable(findGradeAccess(student, term));
    }

    @Override
    public Grade select(StudentId student, TermId gradeId) {
        RemoteGradeEntry ga = findGradeAccess(student, gradeId);
        return ga != null ? ga.getGrade() : null;
    }

    private RemoteGradeEntry findGradeAccess(StudentId sid, TermId term) {
        Map<TermId, RemoteGradeEntry> m = values.get(sid);
        return m != null ? m.get(term) : null;
    }

    protected SubmitGradeEdit setGrade(final Grade g, final StudentId sid, final TermId i, final Timestamp ts) {
        boolean fire = false;
        Grade old = null;
        synchronized (values) {
            Map<TermId, RemoteGradeEntry> m = values.get(sid);
            if (g != null) {
                RemoteGradeEntry ga = null;
                if (m == null) {
                    m = new HashMap<>();
                    values.put(sid, m);
                } else {
                    ga = m.get(i);
                }
                if (ga != null) {
                    old = ga.getGrade();
                    fire = ga.update(g, ts);
                } else {
                    ga = new RemoteGradeEntry(g, ts, i, sid);
                    m.put(i, ga);
                    fire = true;
                }
            } else if (m != null) {
                RemoteGradeEntry oga = m.remove(i);
                if (oga != null) {
                    old = oga.getGrade();
                    fire = true;
                }
                if (m.isEmpty()) {
                    values.remove(sid);
                }
            }
        }
        if (fire) {
            fireValueForStudentChanged(sid, i, old, g, ts);
        }
        return new SubmitGradeEdit(this, sid, i, old, g);
    }

    @Override
    public Timestamp timestamp(StudentId student, TermId gradeId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    //use AbstractAssDoc.getUndoSupport to add CompoundEdit...
    public void submitUndoable(final StudentId student, TermId term, final Grade grade, CompoundEdit ce) {
        if (student != null) {
            final SubmitGradeEdit edit = setGrade(grade, student, term, null);
            //dont use Util.RP causes optimistic lock exception
            submitRP.post(() -> doSubmit(student, term, edit.getOverridden(), grade, null), 0, Thread.NORM_PRIORITY + 1);
            if (ce == null) {
                undoSupport.postEdit(edit);
            } else {
                ce.addEdit(edit);
            }
        }
    }

    @Override
    public void submit(final StudentId student, final TermId term, final Grade grade, final Timestamp ts) {
        if (student != null) {
            SubmitGradeEdit old = null;
            if (ts == null) { //unconfirmed user table action
                old = setGrade(grade, student, term, null);
            }
            final Grade before = old != null ? old.getOverridden() : null;
            submitRP.post(() -> doSubmit(student, term, before, grade, ts));
        }
    }

    protected abstract void doSubmit(final StudentId student, final TermId term, final Grade before, final Grade grade, final Timestamp ts);

    protected abstract void updateTerm(final TermId term, final int numTrial);

    public void refresh() {
        Util.RP(provider).post(() -> refresh(0));
    }

    protected abstract void refresh(final int numTrial);

    protected void logMessage(Exception pex, String message, StudentId student, TermId term, Grade grade) {
        final String g = grade != null ? grade.toString() : "";
        final String pexType = pex.getClass().getCanonicalName();
        final String msg = pex.getLocalizedMessage();
        Logger.getLogger(RemoteTargetAssessmentDocument.class.getCanonicalName()).log(Level.WARNING, message, new String[]{document.getId(), Long.toString(student.getId()), Integer.toString(term.getId()), g, pexType, msg, Integer.toString(UPDATE_DELAY.length)});
    }

    @Override
    public void addListener(final GradeTermTargetAssessment.Listener l) {
        synchronized (listeners) {
            if (listeners.getListenerCount() == 0) {
                if (jmsDocumentsService != null) {
                    jmsDocumentsService.registerListener(MultiTargetAssessmentEvent.class, listener);
                }
            }
            listeners.add(TargetAssessment.Listener.class, l);
        }
    }

    @Override
    public void removeListener(final GradeTermTargetAssessment.Listener l) {
        synchronized (listeners) {
            listeners.remove(TargetAssessment.Listener.class, l);
            if (listeners.getListenerCount() == 0) {
                if (jmsDocumentsService != null) {
                    jmsDocumentsService.unregisterListener(listener);
                }
            }
        }

    }

    protected void fireValueForStudentChanged(final StudentId sid, final TermId term, final Grade old, final Grade g, final Timestamp ts) {
        if (!Objects.equals(old, g)) {
            //TODO: really dispach in AWT?
            Mutex.EVENT.writeAccess(() -> {
                synchronized (listeners) {
                    for (final TargetAssessment.Listener l : listeners.getListeners(TargetAssessment.Listener.class)) {
                        if (l instanceof IdentityTargetAssessment.Listener) {
                            try {
                                ((IdentityTargetAssessment.Listener) l).valueForStudentChanged(this, sid, term, old, g, ts);
                            } catch (Exception e) {
                                final String msg = "An exception has ocurred firing fireValueForStudentChanged on " + getName().getColumnLabel();
                                PlatformUtil.getCodeNameBaseLogger(RemoteTargetAssessmentDocument.class).log(Level.WARNING, msg, e);
                            }
                        }
                    }
                }
            });

        }
    }

    @Override
    public boolean isFragment() {
        return false;
    }

    @Override
    public Validity getDocumentValidity() {
        return new AbstractDocumentValidity() {

            @Override
            public ZonedDateTime getExpirationDate() {
                LocalDate ld = getDateOfExpiry();
                return ld.atStartOfDay(ZoneId.systemDefault());
            }

        };
    }

    protected abstract boolean isRemoteException(Exception ex);

    @Override
    public String toString() {
        return getDocumentId().toString() + "[" + getName().getDisplayName(null) + "]";
    }

    private class WorkingDateListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            Util.RP(provider).post(() -> update(0));
        }

        @Messages({"RemoteTargetAssessmentDocument.WorkingDateListener.update.retry=Enqueuing {0}. retry to update {1}."})
        private void update(final int retry) {
            assert Util.RP(provider).isRequestProcessorThread();
            try {
                updateSigneesAndMarkersAndProperties();
            } catch (Exception e) {
                if (isRemoteException(e) && retry < Config.getInstance().getRetryTimes().length) {
                    final int num = retry + 1;
                    final int wait = Config.getInstance().getRetryTimes()[retry];
                    final String message = NbBundle.getMessage(RemoteTargetAssessmentDocument.class, "RemoteTargetAssessmentDocument.WorkingDateListener.update.retry", num, getName().getDisplayName(null));
                    RemoteUnitsModel.LOGGER.log(LogLevel.INFO, message);
                    Util.RP(provider).post(() -> update(num), wait);
                    return;
                }
                throw e;
            } finally {
                WorkingDate wd = Lookup.getDefault().lookup(WorkingDate.class);
                final WorkingDate.Updater wdUpdater = wd.markUpdating();
                EventQueue.invokeLater(wdUpdater::unmarkUpdating);
            }
        }

    }

    private final class Listener implements JMSListener<MultiTargetAssessmentEvent> {

        @Override
        public void addNotify() {
        }

        @Override
        public void removeNotify() {
        }

        @Override
        public void onMessage(MultiTargetAssessmentEvent event) {
            Identity source = event.getSource();
            if (!(source instanceof DocumentId) || !((DocumentId) source).equals(document)) {
                return;
            }
            final MultiTargetAssessmentEvent.Update[] updates = event.getUpdates();
            if (updates != null) {
                final TermId singleTerm = Arrays.stream(updates)
                        .map(u -> u.getGradeId())
                        .filter(TermId.class::isInstance)
                        .map(TermId.class::cast)
                        .collect(CollectionUtil.singleOrNull());
                final boolean runUpdate = NbPreferences.forModule(RemoteTargetAssessmentDocument.class).getBoolean("remote-target-assessment-document.run.single.term.updates", false);
                if (runUpdate && singleTerm != null && updates.length > 3) {
                    Util.RP(provider).post(() -> updateTerm(singleTerm, 0));
                } else {
                    for (final MultiTargetAssessmentEvent.Update u : updates) {
                        final Grade g = u.getValue();
                        final StudentId sid = u.getStudent();
                        final Timestamp ts = u.getTimestamp();
                        final Identity gId = u.getGradeId();
                        if (sid != null && gId instanceof TermId) {
                            final TermId term = (TermId) gId;
                            setGrade(g, sid, term, ts);
                        }
                    }
                }
            } else if (event.getType().equals(DocumentEventType.CHANGE)) {
                Util.RP(provider).post(RemoteTargetAssessmentDocument.this::updateSigneesAndMarkersAndProperties);
            }
        }

    }

}
