/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.clients.termreport;

import org.thespheres.betula.document.util.TargetAssessmentEntrySynchronizer;
import com.google.common.eventbus.Subscribe;
import java.awt.EventQueue;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.swing.Action;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.netbeans.api.project.FileOwnerQuery;
import org.openide.awt.StatusDisplayer;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.Unit;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.IdentityTargetAssessment;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.document.util.TargetAssessmentSynchronizer.LogKey;
import org.thespheres.betula.document.util.TargetAssessmentSynchronizer.LogValue;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.document.util.TargetAssessmentEntry;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.NoProviderException;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.scheme.spi.TermNotFoundException;
import org.thespheres.betula.services.ui.util.Targets;
import org.thespheres.betula.services.ws.Paths;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.services.ws.push.DocumentPushEvent;
import org.thespheres.betula.services.ws.push.PushNotificationService;
import org.thespheres.betula.termreport.AssessmentProvider;
import org.thespheres.betula.termreport.AssessmentProviderEnvironment;
import org.thespheres.betula.termreport.TableColumnConfiguration;
import org.thespheres.betula.termreport.TargetAssessmentProvider;
import org.thespheres.betula.termreport.TermReport;
import org.thespheres.betula.termreport.XmlAssessmentProviderDataProvider;
import org.thespheres.betula.tag.State;
import org.thespheres.betula.termreport.AssessmentProviderNode;
import org.thespheres.betula.ui.util.LogLevel;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.util.ContainerBuilder;

/**
 *
 * @author boris.heithecker
 */
public class XmlRemoteTargetAssessmentProvider extends AssessmentProvider<Grade> implements IdentityTargetAssessment<Grade, TermId, IdentityTargetAssessment.Listener<Grade, TermId>>, XmlAssessmentProviderDataProvider<TargetAssessmentProvider> {

    static final int UPDATER_DELAY = 1800;
    final RequestProcessor RP; // = new RequestProcessor(XmlRemoteTargetAssessmentProvider.class);
    private final XmlRemoteTargetAssessmentReference reference;
    final XmlAssessmentProviderEnvironment env;
    public static final State UNAVAILABLE = new Initialization(1, true, "unavailable");
    public static final State MISSING_CONFIGURATION = new Initialization(-1, true, "missing.config");
    private final TargetAssessmentEntrySynchronizer synchronizer = new TargetAssessmentEntrySynchronizer(this::logUpdate);
//    {
//
//        @Override
//        protected void updateOneLocal(TargetAssessmentEntry<TermId> remote, TargetAssessmentEntry<TermId> local, StudentId sid, TermId tid) {
//            Unit unit = getEnvironment().getContextLookup().lookup(Unit.class);
//            Student stud = null;
//            if (unit != null) {
//                stud = unit.findStudent(sid);
//            }
//            String name = stud != null ? stud.getFullName() : Long.toString(sid.getId());
//            String rt = "Remote: ";
//            if (remote != null) {
//                Grade select = remote.select(sid, tid);
//                Timestamp timestamp = remote.timestamp(sid, tid);
//                rt += (select != null ? select.getShortLabel() : "null") + (timestamp != null ? ("(" + timestamp.getDate().toLocaleString() + ")") : "");
//            }
//            String lt = "Local: ";
//            if (local != null) {
//                Grade select = local.select(sid, tid);
//                Timestamp timestamp = local.timestamp(sid, tid);
//                lt = (select != null ? select.getShortLabel() : "null") + (timestamp != null ? ("(" + timestamp.getDate().toLocaleString() + ")") : "");
//            }
//            Logger.getLogger("sync.info").log(Level.INFO, "Student:  " + name + "; Term: " + tid.getId() + "; "+ lt + "; " + rt);
//            super.updateOneLocal(remote, local, sid, tid);
//        }
//
//    };
    private final Map<LogKey, LogValue> logs = new HashMap<>();
    private final RequestProcessor.Task updater;
    private final Integer[] numTrial = new Integer[]{0};
    static final int[] WAIT_TIME = {1500, 4000, 9000};

    public XmlRemoteTargetAssessmentProvider(XmlRemoteTargetAssessmentReference ref, Lookup context) {
        super(ref.getId(), AssessmentProvider.LOADING);
        this.reference = ref;
        this.env = new XmlAssessmentProviderEnvironment(this, context);
        Optional.ofNullable(env.getContextLookup().lookup(DataObject.class))
                .map(dob -> FileOwnerQuery.getOwner(dob.getPrimaryFile()))
                .map(prj -> prj.getLookup().lookup(PushNotificationService.class))
                .ifPresent(pns -> pns.registerSubscriber(this));
        updateDisplayName();
        WebServiceProvider wp = null;
        try {
            wp = WebProvider.find(env.getServiceProvider(), WebServiceProvider.class);
        } catch (NoProviderException npex) {
            PlatformUtil.getCodeNameBaseLogger(XmlRemoteTargetAssessmentProvider.class).log(LogLevel.INFO_WARNING, npex.getLocalizedMessage());
        }
        if (wp != null) {
            RP = wp.getDefaultRequestProcessor();
        } else {
            RP = new RequestProcessor(XmlRemoteTargetAssessmentProvider.class);
        }
        updater = RP.create(() -> onUpdateData());
        RP.post(this::updateData);
    }

    private void updateDisplayName() {
        String dn;
        Term term = null;
        try {
            term = env.getTermSchedule().resolve(reference.getTerm());
        } catch (TermNotFoundException | IllegalAuthorityException ex) {
        }
        final TargetAssessmentEntry<TermId> target = getTarget();
        if (term != null) {
            dn = term.getDisplayName() + (target != null ? " " + target.getTargetType() : "");
        } else {
            dn = getId();
        }
        EventQueue.invokeLater(() -> setDisplayName(dn));
    }

    public static XmlRemoteTargetAssessmentProvider create(DocumentId document, TermId term, Lookup context) {
        String id = org.thespheres.betula.util.Utilities.createId(context.lookup(TermReport.class).getProviders().size());
        return new XmlRemoteTargetAssessmentProvider(new XmlRemoteTargetAssessmentReference(id, document, term), context);
    }

    @Override
    public AssessmentProviderEnvironment getEnvironment() {
        return env;
    }

    private TargetAssessmentEntry<TermId> getTarget() {
        return reference.target;
    }

    private void setTarget(TargetAssessmentEntry<TermId> target, LocalDateTime accessTime) {
        reference.target = synchronizer.synchronizeTargets(reference.target, target);
        reference.targetAccessTime = accessTime;
    }

    private void logUpdate(LogKey<TermId> key, LogValue log) {
        synchronized (logs) {
            logs.put(key, log);
        }
    }

    boolean isLocalOverridesRemotes(StudentId stud) {
        LogKey key = new LogKey(reference.getTerm(), stud);
        LogValue log;
        synchronized (logs) {
            log = logs.get(key);
        }
        return log != null && !log.isLocalOverridden();
    }

    @Override
    protected Node createNodeDelegate() {
        return new RemoteRefNode(this);
    }

    @Override
    protected TableColumnConfiguration createTableColumnConfiguration() {
        return new RemoteTargetTableColumnConfiguration(this);
    }

    @Override
    public boolean isEditable() {
        return getTarget() != null;
    }

    @Subscribe
    public void onDocumentPushEvent(DocumentPushEvent event) {
        if (event.getEventItem().equals(reference.getDocument())) {
            updater.schedule(UPDATER_DELAY);
        }
    }

    private void onUpdateData() {
        synchronized (initialization) {
            State before = getInitialization();
            initialization[0] = LOADING;
            pSupport.firePropertyChange(PROP_STATUS, before, getInitialization());
        }
        updateData();
    }

    @Messages({"XmlRemoteTargetAssessmentProvider.updateData.message.success=Liste {0} in {1} wurde erfolgreich synchronisiert.",
        "XmlRemoteTargetAssessmentProvider.updateData.log.retry=Enquing {0}. retry to load {1}"})
    //Invoke outside AWT
    public void updateData() {
        final String provider = env.getServiceProvider();
        if (provider != null) {
            Unit u = env.getProject().getLookup().lookup(Unit.class);
            if (u != null) {
                Targets.TargetAssessmentEntryResult f;
                try {
                    f = Targets.get(provider).fetchTargetAssessment(u.getUnitId(), reference.getDocument());
                } catch (IOException ex) {
                    PlatformUtil.getCodeNameBaseLogger(XmlRemoteTargetAssessmentProvider.class).log(Level.INFO, ex.getLocalizedMessage(), ex);
                    synchronized (initialization) {
                        State before = getInitialization();
                        initialization[0] = UNAVAILABLE;
                        pSupport.firePropertyChange(PROP_STATUS, before, getInitialization());
                    }
                    final boolean retry;
                    final Integer trial = numTrial[0];
                    synchronized (numTrial) {
                        retry = numTrial[0]++ < 3;
                    }
                    if (retry) {
                        final String msg = NbBundle.getMessage(XmlRemoteTargetAssessmentProvider.class, "XmlRemoteTargetAssessmentProvider.updateData.log.retry", numTrial[0], reference.getDocument().getId());
                        PlatformUtil.getCodeNameBaseLogger(XmlRemoteTargetAssessmentProvider.class).log(Level.INFO, msg);
                        RP.schedule(updater, WAIT_TIME[trial], TimeUnit.MILLISECONDS);
                    }
                    return;
                }
                final TargetAssessmentEntry<TermId> entry;
                final LocalDateTime time;
                if (f != null) {
                    entry = f.getTargetAssessmentEntry();
                    final ZonedDateTime et = entry.getTime();
                    time = et == null ? null : et.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
                } else {
                    time = null;
                    entry = null;
                }
                synchronized (numTrial) {
                    numTrial[0] = 0;
                }
                synchronized (initialization) {
                    setTarget(entry, time);
                    State before = getInitialization();
                    initialization[0] = READY;
                    pSupport.firePropertyChange(PROP_STATUS, before, getInitialization());
                }
                updateDisplayName();
                final String msg = NbBundle.getMessage(XmlRemoteTargetAssessmentProvider.class, "XmlRemoteTargetAssessmentProvider.updateData.message.success", getDisplayName(), env.getDataObject().getNodeDelegate().getDisplayName());
                StatusDisplayer.getDefault().setStatusText(msg, StatusDisplayer.IMPORTANCE_ANNOTATION);
            }
        } else {
            synchronized (initialization) {
                State before = getInitialization();
                initialization[0] = MISSING_CONFIGURATION;
                pSupport.firePropertyChange(PROP_STATUS, before, getInitialization());
            }
        }
    }

    Container createContainer() {
        final ContainerBuilder builder = new ContainerBuilder();
        final String provider = env.getServiceProvider();
        if (provider != null) {
            final Unit u = env.getProject().getLookup().lookup(Unit.class);
            if (u != null) {
                final TargetAssessmentEntry<TermId> tae = builder.createTargetAssessmentAction(u.getUnitId(), reference.getDocument(), Paths.UNITS_TARGETS_PATH, null, org.thespheres.betula.document.Action.FILE, true);
//                tae.getHints().putAll(td.getProcessorHints());
                synchronized (this) {
                    students().stream().forEach(sid -> {
                        final Grade g = select(sid);
                        if (g != null && Objects.equals(g.getConvention(), reference.target.getPreferredConvention())) {
                            tae.submit(sid, reference.getTerm(), g, timestamp(sid));
                        }
                    });
                }
            }
        }
        return builder.getContainer();
    }

    protected void checkInitialization() {
        if (!getInitialization().satisfies(READY)) {
            throw new IllegalStateException("TargetAssessment is not ready.");
        }
    }

    @Override
    public Grade select(StudentId student, TermId term) {
        try {
            checkInitialization();
        } catch (IllegalStateException e) {
            return null;
        }
        return getTarget().select(student, term);
    }

    @Override
    public Timestamp timestamp(StudentId student, TermId term) {
        checkInitialization();
        return getTarget().timestamp(student, term);
    }

    @Override
    public Set<StudentId> students() {
        checkInitialization();
        return getTarget().students();
    }

    @Override
    public Set<TermId> identities() {
        checkInitialization();
        return getTarget().identities();
    }

    @Override
    public void submit(StudentId student, TermId term, Grade grade, Timestamp timestamp) {
        if (!isEditable()) {
            throw new UnsupportedOperationException("TargetAssessment is not editable.");
        }
        checkInitialization();
        final Grade old = getTarget().select(student, term);
        getTarget().submit(student, term, grade, timestamp);
        final LogKey key = new LogKey(reference.getTerm(), student);
        final LogValue beforeLogValue;
        synchronized (logs) {
            LogValue log = logs.get(key);
            if (log != null) {
                beforeLogValue = log;
                logs.put(key, new LogValue(grade, beforeLogValue.getRemoteValue(), false));
            } else {
                logs.put(key, new LogValue(grade, old, false));
                beforeLogValue = null;
            }
        }
        class SubmitEdit extends AbstractUndoableEdit {

            @Override
            public String getRedoPresentationName() {
                return super.getRedoPresentationName();
            }

            @Override
            public String getUndoPresentationName() {
                return super.getUndoPresentationName();
            }

            @Override
            public void redo() throws CannotRedoException {
                super.redo();
            }

            @Override
            public void undo() throws CannotUndoException {
                getTarget().submit(student, term, old, timestamp);
                synchronized (logs) {
                    if (beforeLogValue != null) {
                        logs.put(key, beforeLogValue);
                    } else {
                        logs.remove(key);
                    }
                }
                super.undo();
            }

        }
        final SubmitEdit se = new SubmitEdit();
        if (!Objects.equals(old, grade)) {
            undoSupport.postEdit(se);
            env.getDataObject().setModified(true);
        }
    }

    @Override
    public void submit(StudentId student, Grade value, Timestamp ts) {
        submit(student, reference.getTerm(), value, ts);
    }

    @Override
    public String getPreferredConvention() {
        checkInitialization();
        return getTarget().getPreferredConvention();
    }

    @Override
    public void addListener(Listener listener) {
        checkInitialization();
        if (getTarget() != null) {
            getTarget().addListener(listener);
        }
    }

    @Override
    public void removeListener(Listener listener) {
//        checkInitialization();
        if (getTarget() != null) {
            getTarget().removeListener(listener);
        }
    }

    @Override
    public void remove() throws IOException {
        env.remove();
    }

    @Override
    public XmlRemoteTargetAssessmentReference getXmlAssessmentProviderData() {
        return this.reference;
    }

    @Override
    public Grade select(StudentId student) {
        return select(student, reference.getTerm());
    }

    @Override
    public Timestamp timestamp(StudentId student) {
        return timestamp(student, reference.getTerm());
    }

    private final static class RemoteRefNode extends AssessmentProviderNode<XmlRemoteTargetAssessmentProvider> {

        @SuppressWarnings({"OverridableMethodCallInConstructor",
            "LeakingThisInConstructor"})
        private RemoteRefNode(XmlRemoteTargetAssessmentProvider p) {
            super(p);
            setIconBaseWithExtension("org/thespheres/betula/clients/resources/table-import.png");
        }

        @Override
        public String getHtmlDisplayName() {
            XmlRemoteTargetAssessmentProvider p = (XmlRemoteTargetAssessmentProvider) provider;
            if (p.getInitialization().equals(BROKEN_LINK)) {
                return "<html><i><font color=\"FFA500\">" + getDisplayName() + "</font></i></html>";
            }
            return super.getHtmlDisplayName();
        }

        @Override
        public Action[] getActions(boolean context) {
            return Utilities.actionsForPath("Loaders/text/betula-term-report-target-assessment-context/Actions").stream()
                    .map(Action.class::cast)
                    .toArray(Action[]::new);
        }

    }

}
