/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.openide.util.NetworkSettings;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.TermId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.TargetDocument;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Entry;
import org.thespheres.betula.document.Envelope;
import org.thespheres.betula.document.util.DocumentUtilities;
import org.thespheres.betula.services.NoProviderException;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.ws.Paths;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.util.ContainerBuilder;
import org.thespheres.betula.assess.IdentityTargetAssessment;
import org.thespheres.betula.document.util.TargetAssessmentEntry;
import org.thespheres.betula.ui.util.UIExceptions;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
public class Targets {

    private final static LoadingCache<String, Targets> CACHE = CacheBuilder.newBuilder()
            .maximumSize(10)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build(new Loader());

    private IOException lastException;
    private UIExceptions.ServiceFailureState state;
    private final Map<DocumentId, List<UnitId>> map = new HashMap<>();
    private final String providerUrl;
    private WebServiceProvider service;
    private RequestProcessor.Task task;

    private Targets(String providerUrl) {
        this.providerUrl = providerUrl;
    }

    public String getProviderUrl() {
        return providerUrl;
    }

    public WebServiceProvider getWebServiceProvider() {
        return service;
    }

    public Map<DocumentId, List<UnitId>> getTargetDocuments() throws IOException {
        if (task != null) {
            if (EventQueue.isDispatchThread()) {
                Logger.getLogger(Targets.class.getName()).log(Level.WARNING, "Targets.getTargetDocuments should not be called from AWT while signees are still being loaded.");
            }
            task.waitFinished();
        }
        if (lastException != null) {
            throw lastException;
        }
        return Collections.unmodifiableMap(map);
    }

    public static Targets get(final String providerUrl) {
        final Targets ret = CACHE.getUnchecked(providerUrl);
        if (ret.state != null && !ret.state.equals(UIExceptions.ServiceFailureState.ABANDON)) {
            CACHE.refresh(providerUrl);
            return get(providerUrl);
        }
        return ret;
    }

    public void forceReload() throws IOException {
        if (state != null && state.equals(UIExceptions.ServiceFailureState.ABANDON)) {
            throw lastException;
        }
        CACHE.refresh(providerUrl);
    }

    public TargetAssessmentEntryResult fetchTargetAssessment(UnitId unit, DocumentId doc) throws IOException {
        if (EventQueue.isDispatchThread()) {
            Logger.getLogger(Targets.class.getName()).log(Level.WARNING, "Targets.fetchTargetAssessment should not be called from AWT.");
        }

        ContainerBuilder builder = new ContainerBuilder();
        //signees
        String[] path = Paths.UNITS_TARGETS_PATH;
        Action action = Action.REQUEST_COMPLETION;
        builder.createTargetAssessmentAction(unit, doc, path, null, action, true);

//        t.getHints().put("preferred-security-role", "unitadmin");
        Container response = null;
        try {
            response = service.createServicePort().solicit(builder.getContainer());
        } catch (Exception ex) {
            if (ex instanceof IOException) {
                throw (IOException) ex;
            } else {
                throw new IOException(ex);
            }
        }
        final List<Envelope> l = DocumentUtilities.findEnvelope(response, path);
        TargetAssessmentEntry<?> tae = l.stream()
                //                .flatMap(n -> n.getChildren().stream())
                .filter(Entry.class::isInstance)
                .map(Entry.class::cast)
                .filter(e -> e.getIdentity() instanceof UnitId)
                .filter(e -> ((UnitId) e.getIdentity()).equals(unit))
                .flatMap(e -> e.getChildren().stream())
                .filter(TargetAssessmentEntry.class::isInstance)
                .map(TargetAssessmentEntry.class::cast)
                .filter(t -> t.getIdentity().equals(doc))
                .collect(CollectionUtil.singleOrNull());
        if (tae != null) {
            try {
                return new FetchTargetAssessmentImpl((TargetAssessmentEntry<TermId>) tae);
            } catch (ClassCastException e) {
            }
        }
        return null;
    }

    private void load() {
        final ContainerBuilder builder = new ContainerBuilder();
        //signees
        final String[] path = Paths.TARGETS_PATH;
        builder.createTemplate(path, null, Action.REQUEST_COMPLETION);
//        t.getHints().put("preferred-security-role", "unitadmin");
        try {
            final Container response = NetworkSettings.suppressAuthenticationDialog(() -> service.createServicePort().solicit(builder.getContainer()));
            //units
            final List<Envelope> l = DocumentUtilities.findEnvelope(response, path);
            l.stream()
                    .filter(node -> (node.getAction() != null && node.getAction().equals(Action.RETURN_COMPLETION)))
                    .map(Targets::extractMap)
                    .forEach(m -> map.putAll(m));
        } catch (Exception ex) {
            if (ex instanceof IOException) {
                lastException = (IOException) ex;
            } else {
                lastException = new IOException(ex);
            }
            state = UIExceptions.handleServiceException(Targets.class.getName(), providerUrl, ex);
        }
    }

    private static Map<DocumentId, List<UnitId>> extractMap(final Envelope node) {
        return node.getChildren().stream()
                .filter(e -> e instanceof Entry && ((Entry) e).getIdentity() instanceof DocumentId)
                .collect(Collectors.toMap(e -> (DocumentId) ((Entry) e).getIdentity(),
                        e -> e.getChildren().stream()
                                .filter(t -> t instanceof Entry && ((Entry) t).getIdentity() instanceof UnitId)
                                .map(t -> (UnitId) ((Entry) t).getIdentity())
                                .collect(Collectors.toList())));
    }

    private final static class Loader extends CacheLoader<String, Targets> {

        @Override
        public Targets load(String providerUrl) {
            Targets ret = new Targets(providerUrl);
            WebServiceProvider wsp;
            try {
                wsp = WebProvider.find(providerUrl, WebServiceProvider.class);
            } catch (NoProviderException e) {
                ret.lastException = new IOException(e);
                ret.state = UIExceptions.ServiceFailureState.ABANDON;
                return ret;
            }

            final RequestProcessor proc = wsp.getDefaultRequestProcessor();
            ret.service = wsp;
            ret.task = proc.post(ret::load, 0, Thread.NORM_PRIORITY);
            return ret;
        }

    }

    public static interface TargetAssessmentEntryResult extends TargetDocument, IdentityTargetAssessment<Grade, TermId, IdentityTargetAssessment.Listener<Grade, TermId>> {

        public TargetAssessmentEntry<TermId> getTargetAssessmentEntry();
    }
}
