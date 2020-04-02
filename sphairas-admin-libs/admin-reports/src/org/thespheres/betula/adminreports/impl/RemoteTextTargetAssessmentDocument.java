/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminreports.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import javax.swing.event.EventListenerList;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor.Task;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.admin.units.AbstractTargetAssessmentDocument;
import org.thespheres.betula.admin.units.SubmitResult;
import org.thespheres.betula.admin.units.util.Util;
import org.thespheres.betula.services.jms.TextTargetAssessmentEvent;
import org.thespheres.betula.services.client.jms.JMSListener;
import org.thespheres.betula.services.client.jms.JMSTopicListenerService;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Entry;
import org.thespheres.betula.document.Envelope;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.document.Template;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.document.util.DocumentUtilities;
import org.thespheres.betula.document.util.MarkerAdapter;
import org.thespheres.betula.document.util.TextAssessmentEntry;
import org.thespheres.betula.services.ProviderRegistry;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.ws.Paths;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.ui.util.LogLevel;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.util.ContainerBuilder;

/**
 *
 * @author boris.heithecker
 */
public class RemoteTextTargetAssessmentDocument extends AbstractTargetAssessmentDocument {

    private final ConcurrentMap<StudentId, Map<TermId, Map<Marker, String>>> values;
    private final EventListenerList listeners = new EventListenerList();
    private final DocListener listener = new DocListener();
    private final TextAssessmentEntry entry;

    private RemoteTextTargetAssessmentDocument(final DocumentId document, final TextAssessmentEntry e, final ConcurrentMap<StudentId, Map<TermId, Map<Marker, String>>> values, final String provider, final JMSTopicListenerService jmsprovider, final NamingResolver namingResolver) {
        super(document, provider, jmsprovider, namingResolver);
        this.entry = e;
        this.values = values;
        jmsprovider.registerListener(TextTargetAssessmentEvent.class, listener);
    }

    static RemoteTextTargetAssessmentDocument create(DocumentId d, TextAssessmentEntry e, String provider, JMSTopicListenerService jmsprovider, NamingResolver nr) {
        final ConcurrentMap<StudentId, Map<TermId, Map<Marker, String>>> values = new ConcurrentHashMap<>();
        //sections
        e.getChildren().stream()
                .filter(tch -> (tch instanceof Entry && ((Entry<?, ?>) tch).getIdentity() instanceof TermId))
                .forEachOrdered(tch -> {
                    final TermId tid = ((Entry<TermId, ?>) tch).getIdentity();
                    tch.getChildren().stream()
                            .filter(t -> t.getValue() != null && t.getValue() instanceof MarkerAdapter)
                            .map(t -> (Template<MarkerAdapter>) t)
                            .forEachOrdered(ve -> {
                                final Marker section = ve.getValue().getMarker();
                                ve.getChildren().stream()
                                        .filter(s -> (s instanceof Entry && ((Entry<?, ?>) s).getIdentity() instanceof StudentId) && s.getValue() instanceof String)
                                        .map(s -> (Entry<StudentId, String>) s)
                                        .forEachOrdered(se -> {
                                            values.computeIfAbsent(se.getIdentity(), k -> new ConcurrentHashMap<>())
                                                    .computeIfAbsent(tid, k -> new ConcurrentHashMap<>())
                                                    .put(section, se.getValue());
                                        });
                            });
                });
        //no sections
        e.getChildren().stream()
                .filter(tch -> (tch instanceof Entry && ((Entry<?, ?>) tch).getIdentity() instanceof TermId))
                .forEachOrdered(tch -> {
                    final TermId tid = ((Entry<TermId, ?>) tch).getIdentity();
                    tch.getChildren().stream()
                            .filter(s -> (s instanceof Entry && ((Entry<?, ?>) s).getIdentity() instanceof StudentId) && s.getValue() instanceof String)
                            .map(s -> (Entry<StudentId, String>) s)
                            .forEachOrdered(se -> {
                                values.computeIfAbsent(se.getIdentity(), k -> new ConcurrentHashMap<>())
                                        .computeIfAbsent(tid, k -> new ConcurrentHashMap<>())
                                        .put(Marker.NULL, se.getValue());
                            });
                });
        return new RemoteTextTargetAssessmentDocument(d, e, values, provider, jmsprovider, nr);
    }

    @Override
    public Set<StudentId> students() {
        synchronized (values) {
            return values.keySet();
        }
    }

    @Override
    public Set<TermId> identities() {
        synchronized (values) {
            return values.values().stream()
                    .flatMap(m -> m.keySet().stream())
                    .collect(Collectors.toSet());
        }
    }

    public Set<Marker> sections() {
        synchronized (values) {
            return values.values().stream()
                    .flatMap(m -> m.values().stream())
                    .flatMap(m -> m.keySet().stream())
                    .collect(Collectors.toSet());
        }
    }

    public String getText(final StudentId sid, final TermId term, final Marker section) {
        synchronized (values) {
            return values.getOrDefault(sid, (Map<TermId, Map<Marker, String>>) Collections.EMPTY_MAP)
                    .getOrDefault(term, (Map<Marker, String>) Collections.EMPTY_MAP)
                    .get(section);
        }
    }

    private void setText(StudentId sid, final TermId term, final Marker section, final String txt) {
        final String old = getText(sid, term, section);
        synchronized (values) {
            values.computeIfPresent(sid, (s, tm) -> {
                tm.computeIfPresent(term, (t, sm) -> {
                    sm.computeIfPresent(section, (m, tv) -> txt);
                    return sm;
                });
                return tm;
            });
        }
        fireValueForStudentChanged(sid, term, section, old, getText(sid, term, section), null);
    }

    public SubmitTextEdit submit(final StudentId student, final TermId term, final Marker section, final String text) {
        if (student != null && term != null) {
            final SubmitTextEdit ret = new SubmitTextEdit(this, student, term, section, text);
            final Task t = Util.RP(provider).post(() -> {
                try {
                    doSubmit(ret);
                    ret.setResult(SubmitResult.OK);
                } catch (IOException ex) {
                    ret.setResult(SubmitResult.EXCEPTION);
                    Util.notify(getDocumentId(), ProviderRegistry.getDefault().get(provider));
                    PlatformUtil.getCodeNameBaseLogger(RemoteTextTargetAssessmentDocument.class).log(LogLevel.INFO_WARNING, ex.getLocalizedMessage(), ex);
                }
            }, 0, Thread.NORM_PRIORITY);
            ret.task = t;
            return ret;
        }
        return null;
    }

    private void updateText(final StudentId sid, final TermId term, final Marker section) throws IOException {
        final ContainerBuilder builder = new ContainerBuilder();
        final String[] path = Paths.TEXT_UNITS_TARGETS_PATH;
        final TextAssessmentEntry tae = builder.createTextAssessmentAction(null, getDocumentId(), path, null, null, true);
        final Entry<TermId, ?> te = new Entry<>(null, term);
        tae.getChildren().add(te);
        final Template<MarkerAdapter> me = new Template<>(null, new MarkerAdapter(section));
        te.getChildren().add(me);
        final Entry<StudentId, ?> se = new Entry<>(Action.REQUEST_COMPLETION, sid);
        me.getChildren().add(se);
        //        tae.getHints().put("preferred-security-role", "unitadmin");
        Container response;
        try {
            final WebServiceProvider service = WebProvider.find(getProvider(), WebServiceProvider.class);
            response = service.createServicePort().solicit(builder.getContainer());
        } catch (Exception ex) {
            throw new IOException(ex);
        }
        final List<Envelope> l = DocumentUtilities.findEnvelope(response, path);
//        if (term == null) {
        final String ret;
        try {
            ret = l.stream()
                    //                .flatMap(n -> n.getChildren().stream())
                    .filter(TextAssessmentEntry.class::isInstance)
                    .map(TextAssessmentEntry.class::cast)
                    .filter(t -> t.getIdentity().equals(getDocumentId()))
                    .flatMap(t -> t.getChildren().stream())
                    .filter(Entry.class::isInstance)
                    .map(Entry.class::cast)
                    .filter(t -> t.getIdentity().equals(term))
                    .flatMap(t -> t.getChildren().stream())
                    .filter(t -> t.getValue() instanceof MarkerAdapter && ((MarkerAdapter) t.getValue()).getMarker().equals(section))
                    .flatMap(t -> t.getChildren().stream())
                    .filter(Entry.class::isInstance)
                    .map(Entry.class::cast)
                    .filter(t -> t.getIdentity().equals(sid) && t.getValue() instanceof String)
                    .map(t -> (String) t.getValue())
                    .collect(CollectionUtil.requireSingleOrNull());
        } catch (Exception e) {
            throw e;
        }
        setText(sid, term, section, ret);
    }

    private void doSubmit(final SubmitTextEdit submit) throws IOException {
        final ContainerBuilder builder = new ContainerBuilder();
        final String[] path = Paths.TEXT_UNITS_TARGETS_PATH;
        final TextAssessmentEntry tae = builder.createTextAssessmentAction(null, getDocumentId(), path, null, Action.FILE, true);
        final Entry<TermId, ?> te = new Entry<>(null, submit.getTerm());
        tae.getChildren().add(te);
        final String text = submit.getText();
        final Entry<StudentId, String> se;
        if (text != null) {
            se = new Entry<>(Action.FILE, submit.getStudent(), text);
        } else {
            se = new Entry<>(Action.ANNUL, submit.getStudent(), null);
        }
        final Marker section = submit.getSection();
        if (!Marker.isNull(section)) {
            final Template<MarkerAdapter> me = new Template<>(null, new MarkerAdapter(section));
            te.getChildren().add(me);
            me.getChildren().add(se);
        } else {
            te.getChildren().add(se);
        }
        //        tae.getHints().put("preferred-security-role", "unitadmin");
        Container response;
        try {
            final WebServiceProvider service = WebProvider.find(getProvider(), WebServiceProvider.class);
            response = service.createServicePort().solicit(builder.getContainer());
        } catch (Exception ex) {
            throw new IOException(ex);
        }
        final List<Envelope> l = DocumentUtilities.findEnvelope(response, path);
        final TextAssessmentEntry ret;
        try {
            ret = l.stream()
                    .filter(TextAssessmentEntry.class::isInstance)
                    .map(TextAssessmentEntry.class::cast)
                    .filter(t -> t.getIdentity().equals(getDocumentId()))
                    .collect(CollectionUtil.requireSingleOrNull());
        } catch (Exception e) {
            throw new IOException(e);
        }
        Util.processException(te, getDocumentId());
    }

    @Override
    public Timestamp timestamp(StudentId student, TermId gradeId) {
//        Arrays.stream(entries).filter(e -> e.getStudent().equals(student)).findAny().map(Entry::getTimestamp).orElse(null);
        return null;
    }

    public void addListener(Listener l) {
        synchronized (listeners) {
            listeners.add(Listener.class, l);
        }
    }

    public void removeListener(Listener l) {
        synchronized (listeners) {
            listeners.remove(Listener.class, l);
        }
    }

    private void fireValueForStudentChanged(StudentId sid, TermId term, Marker section, String old, String v, Timestamp ts) {
        if (!Objects.equals(old, v)) {
            Mutex.EVENT.writeAccess(() -> {
                synchronized (listeners) {
                    for (Listener l : listeners.getListeners(Listener.class)) {
                        if (l instanceof Listener) {
                            ((Listener) l).valueForStudentChanged(sid, term, section, old, v, ts);
                        }
                    }
                }
            });
        }
    }

    @Override
    public String getPreferredConvention() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getTargetType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<String, Signee> getSignees() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isFragment() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Validity getDocumentValidity() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SigneeInfo getCreationInfo() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private class DocListener implements JMSListener<TextTargetAssessmentEvent> {

        @Override
        public void addNotify() {
        }

        @Override
        public void removeNotify() {
        }

        @Override
        public void onMessage(TextTargetAssessmentEvent event) {
//            event.
        }

    }

    public interface Listener extends EventListener {

        public void valueForStudentChanged(StudentId source, TermId gradeId, Marker section, String old, String newValue, Timestamp timestamp);

    }
}
