/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.math.DoubleMath;
import java.io.IOException;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.StreamSupport;
import javax.swing.Icon;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.admin.units.util.Util;
import org.thespheres.betula.services.client.jms.JMSTopicListenerService;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Entry;
import org.thespheres.betula.document.Envelope;
import org.thespheres.betula.document.util.DateTimeUtil;
import org.thespheres.betula.document.util.DocumentUtilities;
import org.thespheres.betula.document.util.TargetAssessmentEntry;
import org.thespheres.betula.document.util.UnitEntry;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.NoProviderException;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.ProviderRegistry;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.ws.Paths;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.services.WorkingDate;
import org.thespheres.betula.services.jms.JMSTopic;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.util.ContainerBuilder;

/**
 *
 * @author boris.heithecker
 */
public class ServiceTargetAssessmentDocumentFactory extends CacheLoader<DocumentId, ServiceRemoteTargetAssessmentDocument> {
    
    private final static Map<String, ServiceTargetAssessmentDocumentFactory> INSTANCES = new HashMap<>();
    
    private final LoadingCache<DocumentId, ServiceRemoteTargetAssessmentDocument> cache = CacheBuilder.newBuilder()
            .weakValues()
            .concurrencyLevel(Util.RP_THROUGHPUT)
            .initialCapacity(6000)
            .recordStats()
            .build(this);
    private final WebServiceProvider service;
    
    private ServiceTargetAssessmentDocumentFactory(String provider) {
        service = WebProvider.find(provider, WebServiceProvider.class);
    }
    
    public static ServiceTargetAssessmentDocumentFactory get(final String provider) {
        synchronized (INSTANCES) {
            return INSTANCES.computeIfAbsent(provider, ServiceTargetAssessmentDocumentFactory::new);
        }
    }
    
    public ServiceRemoteTargetAssessmentDocument getTargetAssessmentDocument(final DocumentId document) throws IOException {
        final ServiceRemoteTargetAssessmentDocument get;
        try {
//            get = cache.get(document, () -> {
//                try {
//                    return create(document);
//                } catch (Exception e) {
//                    throw new ExecutionException(e);
//                }
//            });
            get = cache.get(document);
        } catch (ExecutionException ex) {
            if (ex.getCause() instanceof IOException) {
                throw (IOException) ex.getCause();
            } else {
                throw new IOException(ex);
            }
        }
        final double nanos = cache.stats().averageLoadPenalty();
        final int round = DoubleMath.roundToInt(nanos / 1000000, RoundingMode.UP);
        PlatformUtil.logPerformance("Average load penalty", round);
        return get;
    }
    
    Map<DocumentId, ServiceRemoteTargetAssessmentDocument> getAll(final Set<DocumentId> document) throws IOException {
        final Map<DocumentId, ServiceRemoteTargetAssessmentDocument> get;
        try {
            get = cache.getAll(document);
        } catch (ExecutionException | InvalidCacheLoadException ex) {
            if (ex.getCause() instanceof IOException) {
                throw (IOException) ex.getCause();
            } else {
                throw new IOException(ex);
            }
        }
        final double nanos = cache.stats().averageLoadPenalty();
        final int round = DoubleMath.roundToInt(nanos / 1000000, RoundingMode.UP);
        PlatformUtil.logPerformance("Average load penalty", round);
        return get;
    }
    
    public void removeTargetAssessmentDocument(final DocumentId document) {
        cache.invalidate(document);
    }
    
    static NamingResolver findNamingResolver(final String provider) {
        final String nr = LocalProperties.find(provider).getProperty("naming.providerURL", provider);
        try {
            return NamingResolver.find(nr);
        } catch (NoProviderException e) {
            final ProviderInfo info = ProviderRegistry.getDefault().get(provider);
            Util.notify(null, info);
            return null;
        }
    }
    
    static JMSTopicListenerService findJMSTopicListenerService(final String provider) {
        final String np = LocalProperties.find(provider).getProperty("jms.providerURL", provider);
        try {
            return JMSTopicListenerService.find(np, JMSTopic.DOCUMENTS_TOPIC.getJmsResource());
        } catch (NoProviderException ex) {
            notifyNoJMS(provider);
            return null;
        }
    }
    
    @NbBundle.Messages(value = {"ServiceTargetAssessmentDocumentFactory.notifyNoJMS.title=Keine Server-Benachrichtigungen",
        "ServiceTargetAssessmentDocumentFactory.notifyNoJMS.message=Für die Datenstelle {0} konnten keine Server-Benachrichtigungen abonniert werden."})
    private static void notifyNoJMS(final String p) {
        final ProviderInfo provider = ProviderRegistry.getDefault().get(p);
        final Icon ic = ImageUtilities.loadImageIcon("org/thespheres/betula/ui/resources/exclamation-red-frame.png", true);
        final String title = NbBundle.getMessage(ServiceTargetAssessmentDocumentFactory.class, "ServiceTargetAssessmentDocumentFactory.notifyNoJMS.title");
        final String message = NbBundle.getMessage(ServiceTargetAssessmentDocumentFactory.class, "ServiceTargetAssessmentDocumentFactory.notifyNoJMS.message", provider.getDisplayName());
        NotificationDisplayer.getDefault()
                .notify(title, ic, message, null, NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.WARNING);
    }
    
    public static void addWorkingDateProperties(final Entry<?, ?> entry) {
        final WorkingDate wd = Lookup.getDefault().lookup(WorkingDate.class);
        if (wd != null && !wd.isNow()) {
            final Date d = wd.getCurrentWorkingDate();
            final LocalDate date = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            final String df = DateTimeUtil.DATEFORMAT.format(date);
            entry.getHints().put("version.asOf", df);
        }
    }
    
    @Override
    public ServiceRemoteTargetAssessmentDocument load(DocumentId key) throws Exception {
        return create(new DocumentId[]{key}).get(key);
    }
    
    @Override
    public Map<DocumentId, ServiceRemoteTargetAssessmentDocument> loadAll(Iterable<? extends DocumentId> keys) throws Exception {
        return create(StreamSupport.stream(keys.spliterator(), false).toArray(DocumentId[]::new));
    }

    //Loads one target document
    protected Map<DocumentId, ServiceRemoteTargetAssessmentDocument> create(final DocumentId[] dd) throws IOException {
        final ContainerBuilder builder = new ContainerBuilder();
        final String[] path = Paths.UNITS_TARGETS_PATH;
        final Action action = Action.REQUEST_COMPLETION;
        final UnitId unit = null;
        final Map<DocumentId, ServiceRemoteTargetAssessmentDocument> retMap = new HashMap<>();
        for (final DocumentId d : dd) {
            if (DocumentId.isNull(d)) {
                continue;
            }
            final TargetAssessmentEntry<TermId> tae = builder.createTargetAssessmentAction(unit, d, Paths.UNITS_TARGETS_PATH, null, action, true);
            addWorkingDateProperties(tae);
        }
        Container response;
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
        for (final DocumentId d : dd) {
            final TargetAssessmentEntry<TermId> ret;
            try {
                ret = l.stream()
                        //                .flatMap(n -> n.getChildren().stream())
                        .filter(TargetAssessmentEntry.class::isInstance)
                        .map(TargetAssessmentEntry.class::cast)
                        .filter(t -> t.getIdentity().equals(d))
                        .collect(CollectionUtil.requireSingleOrNull());
            } catch (Exception e) {
                throw new IOException(e);
            }
            if (ret == null) {
                PlatformUtil.getCodeNameBaseLogger(ServiceTargetAssessmentDocumentFactory.class).info("Non single result for " + d.toString());
                continue;
            }
            try {
                Util.processException(ret, d);
            } catch (IOException ioex) {
                if (dd.length == 1) {
                    throw ioex;
                } else {
                    continue;
                }
            }
            final ServiceRemoteTargetAssessmentDocument rtad = ServiceRemoteTargetAssessmentDocument.create(d, ret, service.getInfo().getURL());
            retMap.put(d, rtad);
        }
        return retMap;
    }

    //getUnitBean()
    DocumentId[] getTargetAssessmentDocuments(final DocumentId[] units, final TermId term, final boolean linked) throws IOException {
        final ContainerBuilder builder = new ContainerBuilder();
        final String[] path = Paths.UNITS_TARGET_DOCUMENTS_PATH;
        final Action action = Action.REQUEST_COMPLETION;
        for (final DocumentId unit : units) {
            final UnitEntry ue = new UnitEntry(unit, null, term == null ? action : null, true);
            if (linked) {
                ue.getHints().put("linked", Boolean.toString(true));
            }
            builder.add(ue, path);
            if (term != null) {
                final Entry<TermId, ?> te = new Entry<>(action, term);
                addWorkingDateProperties(te);
                ue.getChildren().add(te);
            }
            //        tae.getHints().put("preferred-security-role", "unitadmin");
        }
        Container response;
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
        if (term == null) {
            return l.stream()
                    .filter(UnitEntry.class::isInstance)
                    .map(UnitEntry.class::cast)
                    .filter(t -> Arrays.stream(units).anyMatch(t.getIdentity()::equals))
                    .flatMap(t -> t.getChildren().stream())
                    .filter(Entry.class::isInstance)
                    .map(Entry.class::cast)
                    .map(Entry::getIdentity)
                    .filter(DocumentId.class::isInstance)
                    .map(DocumentId.class::cast)
                    .toArray(DocumentId[]::new);
        } else {
            return l.stream()
                    .filter(UnitEntry.class::isInstance)
                    .map(UnitEntry.class::cast)
                    .filter(t -> Arrays.stream(units).anyMatch(t.getIdentity()::equals))
                    .flatMap(e -> e.getChildren().stream())
                    .filter(Entry.class::isInstance)
                    .map(Entry.class::cast)
                    .filter(e -> term.equals(e.getIdentity()))
                    .flatMap(t -> t.getChildren().stream())
                    .filter(Entry.class::isInstance)
                    .map(Entry.class::cast)
                    .map(Entry::getIdentity)
                    .filter(DocumentId.class::isInstance)
                    .map(DocumentId.class::cast)
                    .toArray(DocumentId[]::new);
        }
    }

    //getUnitBean()
    DocumentId[] getTargetAssessmentDocuments(final StudentId[] arr, final TermId term, final boolean linked) throws IOException {
        final ContainerBuilder builder = new ContainerBuilder();
        final String[] path = Paths.UNITS_TARGET_DOCUMENTS_PATH;
        final Action action = Action.REQUEST_COMPLETION;
        final UnitEntry ue = new UnitEntry(DocumentId.NULL, null, term == null ? action : null, true);
        builder.add(ue, path);
        if (linked) {
            ue.getHints().put("linked", Boolean.toString(true));
        }
        final Entry<?, ?> sarr;
        if (term != null) {
            final Entry<TermId, ?> te = new Entry<>(action, term);
            builder.add(te, path);
            addWorkingDateProperties(te);
            ue.getChildren().add(te);
            sarr = te;
        } else {
            sarr = ue;
        }
        for (final StudentId sid : arr) {
            final Entry<StudentId, ?> sentry = new Entry<>(null, sid);
            sarr.getChildren().add(sentry);
        }
        //        tae.getHints().put("preferred-security-role", "unitadmin");
        Container response;
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
        if (term == null) {
            return l.stream()
                    .filter(Entry.class::isInstance)
                    .map(Entry.class::cast)
                    .filter(t -> DocumentId.NULL.equals(t.getIdentity()))
                    .map(Entry::getIdentity)
                    .map(DocumentId.class::cast)
                    .toArray(DocumentId[]::new);
        } else {
            return l.stream()
                    .filter(Entry.class::isInstance)
                    .map(Entry.class::cast)
                    .filter(t -> DocumentId.NULL.equals(t.getIdentity()))
                    .flatMap(e -> e.getChildren().stream())
                    .filter(Entry.class::isInstance)
                    .map(Entry.class::cast)
                    .filter(e -> term.equals(e.getIdentity()))
                    .map(Entry::getIdentity)
                    .map(DocumentId.class::cast)
                    .toArray(DocumentId[]::new);
        }
    }
    
    TargetAssessmentEntry<TermId> fetchTargetAssessmentDocument(final DocumentId document) throws IOException {
        final ContainerBuilder builder = new ContainerBuilder();
        
        final String[] path = Paths.UNITS_TARGETS_PATH;
        final Action action = Action.REQUEST_COMPLETION;
        final TargetAssessmentEntry<?> tae = builder.createTargetAssessmentAction(null, document, path, null, action, true);
        //        tae.getHints().put("preferred-security-role", "unitadmin");
        addWorkingDateProperties(tae);
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
        return l.stream()
                .filter(TargetAssessmentEntry.class::isInstance)
                .map(TargetAssessmentEntry.class::cast)
                .filter(t -> t.getIdentity().equals(document))
                .collect(CollectionUtil.singleOrNull());
    }
    
}
