/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.openide.util.NbBundle;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.admin.units.RemoteGradeEntry;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.admin.units.RemoteUnitsModel;
import org.thespheres.betula.admin.units.util.Config;
import org.thespheres.betula.admin.units.util.Util;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.services.client.jms.JMSTopicListenerService;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Entry;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.document.util.DocumentUtilities;
import org.thespheres.betula.document.util.TargetAssessmentEntry;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.ws.Paths;
import org.thespheres.betula.services.ws.ServiceException;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.ui.util.LogLevel;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.util.ContainerBuilder;
import org.thespheres.betula.util.GradeAdapter;

/**
 *
 * @author boris.heithecker
 */
public class ServiceRemoteTargetAssessmentDocument extends RemoteTargetAssessmentDocument {

    private final TargetAssessmentEntry<TermId> entry;
    private final Map<String, Signee> signees = new HashMap<>();

    private ServiceRemoteTargetAssessmentDocument(DocumentId d, TargetAssessmentEntry<TermId> e, final Map<StudentId, Map<TermId, RemoteGradeEntry>> values, String provider, JMSTopicListenerService jmsprovider, NamingResolver nr) {
        super(d, provider, values, jmsprovider, nr);
        this.entry = e;
        final Set<Marker> m = entry.getValue().getMarkerSet();
        synchronized (markers) {
            markers.addAll(m);
        }
        final Map<String, Signee> sm = entry.getValue().getSigneeInfos().entrySet().stream()
                .collect(Collectors.toMap(en -> en.getKey(), en -> en.getValue().getSignee()));
        signees.putAll(sm);
    }

    static ServiceRemoteTargetAssessmentDocument create(final DocumentId d, final TargetAssessmentEntry<TermId> e, final String provider) {
        final Map<StudentId, Map<TermId, RemoteGradeEntry>> values = new HashMap<>();
        readEntry(e, values);
        final NamingResolver nr = ServiceTargetAssessmentDocumentFactory.findNamingResolver(provider);
        final JMSTopicListenerService jms = ServiceTargetAssessmentDocumentFactory.findJMSTopicListenerService(provider);
        return new ServiceRemoteTargetAssessmentDocument(d, e, values, provider, jms, nr);
    }

    static void readEntry(final TargetAssessmentEntry<TermId> e, final Map<StudentId, Map<TermId, RemoteGradeEntry>> values) {
        e.getChildren().stream()
                .filter(tch -> (tch instanceof org.thespheres.betula.document.Entry && ((org.thespheres.betula.document.Entry<?, ?>) tch).getIdentity() instanceof TermId)).forEachOrdered(tch -> {
            final TermId tid = ((org.thespheres.betula.document.Entry<TermId, ?>) tch).getIdentity();
            tch.getChildren().stream()
                    .filter(sch -> (sch instanceof org.thespheres.betula.document.Entry && ((org.thespheres.betula.document.Entry<?, ?>) sch).getIdentity() instanceof StudentId && ((org.thespheres.betula.document.Entry<?, ?>) sch).getValue() instanceof GradeAdapter)).map((sch) -> (org.thespheres.betula.document.Entry<StudentId, GradeAdapter>) sch).forEachOrdered(ve -> {
                final RemoteGradeEntry rge = new RemoteGradeEntry(ve.getValue().getConvention(), ve.getValue().getId(), ve.getTimestamp().getDate().getTime(), tid, ve.getIdentity());
                values.computeIfAbsent(ve.getIdentity(), k -> new HashMap<>()).put(tid, rge);
            });
        });
    }

    @Override
    public String getTargetType() {
        return Optional.ofNullable(entry.getTargetType()).map(String::toLowerCase).orElse(null);
    }

    @Override
    public String getPreferredConvention() {
        return entry.getPreferredConvention();
    }

    @Override
    public Map<String, Signee> getSignees() {
//        return entry.getValue().getSigneeInfos().entrySet().stream()
//                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getSignee()));
        return Collections.unmodifiableMap(signees);
    }

    @Override
    public SigneeInfo getCreationInfo() {
        return entry.getValue().getCreationInfo();
    }

    @Override
    protected void doSubmit(final StudentId student, final TermId term, final Grade before, final Grade grade, final Timestamp ts) {
        final ContainerBuilder builder = new ContainerBuilder();
        final String[] path = Paths.UNITS_TARGETS_PATH;
        final TargetAssessmentEntry<TermId> tae = builder.createTargetAssessmentAction(null, getDocumentId(), path, null, null, true);
        tae.submit(student, term, grade, ts);
        //        tae.getHints().put("preferred-security-role", "unitadmin");
        final TargetAssessmentEntry<TermId> ret;
        try {
            final WebServiceProvider service = WebProvider.find(getProvider(), WebServiceProvider.class);
            final Container response = service.createServicePort().solicit(builder.getContainer());
            ret = DocumentUtilities.findEnvelope(response, path).stream()
                    .filter(TargetAssessmentEntry.class::isInstance)
                    .map(TargetAssessmentEntry.class::cast)
                    .filter(t -> t.getIdentity().equals(getDocumentId()))
                    .collect(CollectionUtil.requireSingleOrNull());
            Util.processException(ret, getDocumentId());
        } catch (Exception e) {
            Logger.getLogger(ServiceRemoteTargetAssessmentDocument.class
                    .getCanonicalName()).log(Level.WARNING, e.getLocalizedMessage());
            setGrade(before, student, term, ts);
            return;
        }
        if (term != null) {
            final Entry<StudentId, GradeAdapter> re = ret.getChildren().stream()
                    .filter(Entry.class::isInstance)
                    .map(Entry.class::cast)
                    .filter(e -> term.equals(e.getIdentity()))
                    .flatMap(e -> e.getChildren().stream())
                    .filter(Entry.class::isInstance)
                    .map(Entry.class::cast)
                    .filter(e -> student.equals(e.getIdentity()))
                    .collect(CollectionUtil.singleOrNull());
            if (re != null && Action.CONFIRM.equals(re.getAction())) {
                final Timestamp serverTime = re.getTimestamp();
                final Grade rg = re.getValue() != null ? re.getValue().getGrade() : null;
                setGrade(rg, re.getIdentity(), term, serverTime);
            }
        }
    }

    @Override
    protected void updateSigneesAndMarkers() {
        final TargetAssessmentEntry<TermId> ret;
        try {
            ret = fetchEntry(true);
        } catch (Exception ex) {
            Logger.getLogger(ServiceRemoteTargetAssessmentDocument.class
                    .getCanonicalName()).log(Level.WARNING, "An error occurred updating signees and markers of " + document.toString() + ".", ex);
            return;
        }
        updateSignees(ret);
        updateMarkers(ret);
    }

    private void updateMarkers(final TargetAssessmentEntry<TermId> ret) {
        final Marker[] mm = ret.getValue().markers();
        final Set<Marker> mUpdate = Arrays.stream(mm).collect(Collectors.toSet());
        boolean fireMarkerChange = false;
        synchronized (markers) {
            if (!mUpdate.equals(markers)) {
                markers.clear();
                markers.addAll(mUpdate);
                fireMarkerChange = true;
            }
        }
        if (fireMarkerChange) {
            pSupport.firePropertyChange(PROP_MARKERS, null, signees);
        }
    }

    private void updateSignees(final TargetAssessmentEntry<TermId> ret) {
        final Map<String, Signee> sm = ret.getValue().getSigneeInfos().entrySet().stream()
                .collect(Collectors.toMap(en -> en.getKey(), en -> en.getValue().getSignee()));
        boolean fireSigneesChange = false;
        synchronized (signees) {
            if (!signees.equals(sm)) {
                signees.clear();
                signees.putAll(sm);
                fireSigneesChange = true;
            }
        }
        if (fireSigneesChange) {
            pSupport.firePropertyChange(PROP_SIGNEES, null, signees);
        }
    }

    private TargetAssessmentEntry<TermId> fetchEntry(final boolean noChildren) throws IOException, ServiceException {
        final ContainerBuilder builder = new ContainerBuilder();
        final String[] path = Paths.UNITS_TARGETS_PATH;
        final TargetAssessmentEntry<TermId> tae = builder.createTargetAssessmentAction(null, getDocumentId(), path, null, Action.REQUEST_COMPLETION, true);
        if (noChildren) {
            tae.getHints().put("request-completion.no-children", "true");
        }
        final TargetAssessmentEntry<TermId> ret;
        final WebServiceProvider service = WebProvider.find(getProvider(), WebServiceProvider.class);
        final Container response = service.createServicePort().solicit(builder.getContainer());
        ret = DocumentUtilities.findEnvelope(response, path).stream()
                .filter(TargetAssessmentEntry.class::isInstance)
                .map(TargetAssessmentEntry.class::cast)
                .filter(t -> t.getIdentity().equals(getDocumentId()))
                .collect(CollectionUtil.requireSingleOrNull());
        Util.processException(ret, getDocumentId());
        return ret;
    }

    @NbBundle.Messages({"ServiceRemoteTargetAssessmentDocument.listener.tryFetchEntry.retry=Enqueuing {0}. retry to update {1}."})
    private TargetAssessmentEntry<TermId> tryFetchEntry(final int numTrial, final Runnable update) throws IOException {
        final TargetAssessmentEntry<TermId> ret;
        try {
            return fetchEntry(false);
        } catch (Exception ex) {
            if (Util.isServiceException(ex) && numTrial < Config.getInstance().getRetryTimes().length) {
                final int wait = Config.getInstance().getRetryTimes()[numTrial];
                final int num = numTrial + 1;
                final String message = NbBundle.getMessage(ServiceRemoteTargetAssessmentDocument.class, "ServiceRemoteTargetAssessmentDocument.listener.tryFetchEntry.retry", num, getName().getDisplayName(null));
                RemoteUnitsModel.LOGGER.log(LogLevel.INFO, message);
                Util.RP(provider).post(update, wait);
                throw new IOException();
            }
            Logger.getLogger(ServiceRemoteTargetAssessmentDocument.class
                    .getCanonicalName()).log(Level.SEVERE, "An error occurred updating document " + document.toString() + ".", ex);
            throw new IOException();
        }
    }

    @Override
    protected void updateTerm(final TermId term, final int numTrial) {
        final TargetAssessmentEntry<TermId> ret;
        try {
            ret = tryFetchEntry(numTrial, () -> updateTerm(term, numTrial));
        } catch (final IOException ex) {
            return;
        }
        final Map<StudentId, Entry<StudentId, GradeAdapter>> nv = ret.getChildren().stream()
                .filter(Entry.class::isInstance)
                .map(Entry.class::cast)
                .filter(t -> t.getIdentity().equals(term))
                .flatMap(e -> e.getChildren().stream())
                .filter(Entry.class::isInstance)
                .map(Entry.class::cast)
                .filter(t -> t.getIdentity() instanceof StudentId && t.getValue() instanceof GradeAdapter)
                .map(t -> (Entry<StudentId, GradeAdapter>) t)
                .collect(Collectors.toMap(e -> e.getIdentity(), Function.identity()));
        students().stream().forEachOrdered(s -> {
            if (nv.containsKey(s)) {
                setGrade(nv.get(s).getValue().getGrade(), s, term, nv.get(s).getTimestamp());
            } else if (selectGradeAccess(s, term).isPresent()) {
                setGrade(null, s, term, null);
            }
        });
        updateSignees(ret);
    }

    @Override
    protected void refresh(final int numTrial) {
        final TargetAssessmentEntry<TermId> ret;
        try {
            ret = tryFetchEntry(numTrial, () -> refresh(numTrial));
        } catch (final IOException ex) {
            return;
        }
        synchronized (values) {
            values.clear();
            readEntry(ret, values);
        }
        pSupport.firePropertyChange(PROP_VALUES, null, values);
        updateSignees(ret);
        updateMarkers(ret);
    }

    @Override
    protected boolean isRemoteException(Exception ex) {
        return Util.isServiceException(ex);
    }
}
