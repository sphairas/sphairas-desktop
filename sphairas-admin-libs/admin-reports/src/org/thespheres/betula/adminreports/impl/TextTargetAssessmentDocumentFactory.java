/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminreports.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.concurrent.ExecutionException;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.thespheres.betula.Identity;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.admin.units.AbstractTargetAssessmentDocument.TargetAssessmentDocumentCreationException;
import org.thespheres.betula.admin.units.util.Util;
import org.thespheres.betula.services.client.jms.JMSTopicListenerService;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.document.DocumentEntry;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Entry;
import org.thespheres.betula.document.Envelope;
import org.thespheres.betula.document.ExceptionMessage;
import org.thespheres.betula.document.Template;
import org.thespheres.betula.document.util.DateTimeUtil;
import org.thespheres.betula.document.util.DocumentUtilities;
import org.thespheres.betula.document.util.TargetAssessmentEntry;
import org.thespheres.betula.document.util.TextAssessmentEntry;
import org.thespheres.betula.document.util.UnitEntry;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.ws.Paths;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.services.WorkingDate;
import org.thespheres.betula.services.jms.JMSTopic;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.util.ContainerBuilder;

/**
 *
 * @author boris.heithecker
 */
public class TextTargetAssessmentDocumentFactory {

    private final static Map<String, TextTargetAssessmentDocumentFactory> INSTANCES = new HashMap<>();
    private final Cache<DocumentId, RemoteTextTargetAssessmentDocument> CACHE2 = CacheBuilder.newBuilder()
            .weakValues()
            .concurrencyLevel(Util.RP_THROUGHPUT)
            .initialCapacity(6000)
            .build();
    private final WebServiceProvider service;
    private JMSTopicListenerService jms;

    private TextTargetAssessmentDocumentFactory(String provider) {
        service = WebProvider.find(provider, WebServiceProvider.class);
    }

    public static TextTargetAssessmentDocumentFactory get(final String provider) {
        synchronized (INSTANCES) {
            return INSTANCES.computeIfAbsent(provider, TextTargetAssessmentDocumentFactory::new);
        }
    }

    public RemoteTextTargetAssessmentDocument getTargetAssessmentDocument(final DocumentId document) throws TargetAssessmentDocumentCreationException {
        final RemoteTextTargetAssessmentDocument get;
        try {
            get = CACHE2.get(document, () -> {
                try {
                    return create(document);
                } catch (Exception e) {
                    throw new ExecutionException(e);
                }
            });
        } catch (ExecutionException ex) {
            throw new TargetAssessmentDocumentCreationException(document, (Exception) ex.getCause());
        }
        return get;
    }

    public void removeTargetAssessmentDocument(final DocumentId document) {
        CACHE2.invalidate(document);
    }

//    private NamingResolver findNamingResolver() throws IOException {
//        if (namingResolver == null) {
//            final String provider = service.getInfo().getURL();
//            String np = LocalProperties.find(provider).getProperty("naming.providerURL", provider);
//            if (np != null) {
//                for (NamingResolver.Provider p : Lookup.getDefault().lookupAll(NamingResolver.Provider.class)) {
//                    if (p.getInfo().getURL().equals(np)) {
//                        namingResolver = p.getNamingResolver();
//                        break;
//                    }
//                }
//            }
//            if (namingResolver == null) {
//                namingResolver = new IOException("No naming resolver found for \"" + provider + "\".");
//            }
//        }
//        if (namingResolver instanceof IOException) {
//            throw (IOException) namingResolver;
//        }
//        return (NamingResolver) namingResolver;
//    }
    private JMSTopicListenerService findJMSTopicListenerService() throws IOException {
        if (jms == null) {
            final String provider = service.getInfo().getURL();
            final String np = LocalProperties.find(provider).getProperty("jms.providerURL", provider);
            jms = JMSTopicListenerService.find(np, JMSTopic.DOCUMENTS_TOPIC.getJmsResource());
        }
        return jms;
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

    protected RemoteTextTargetAssessmentDocument create(final DocumentId d) throws IOException {
        final ContainerBuilder builder = new ContainerBuilder();
        final String[] path = Paths.TEXT_UNITS_TARGETS_PATH;
        final Action action = Action.REQUEST_COMPLETION;
        final TextAssessmentEntry tae = builder.createTextAssessmentAction(null, d, path, null, action, true);
//        if (term == null) {
//                addWorkingDateProperties(tae);
//        } else {
//            final Entry<TermId, ?> te = new Entry<>(action, term);
//            builder.add(te, path);
//            addWorkingDateProperties(tae);
//            te.getChildren().add(ue);
//        }
        //        tae.getHints().put("preferred-security-role", "unitadmin");
        Container response;
        try {
            response = service.createServicePort().solicit(builder.getContainer());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        final List<Envelope> l = DocumentUtilities.findEnvelope(response, path);
//        if (term == null) {
        final TextAssessmentEntry ret;
        try {
            ret = l.stream()
                    //                .flatMap(n -> n.getChildren().stream())
                    .filter(TextAssessmentEntry.class::isInstance)
                    .map(TextAssessmentEntry.class::cast)
                    .filter(t -> t.getIdentity().equals(d))
                    .collect(CollectionUtil.requireSingleOrNull());
        } catch (Exception e) {
            throw e;
        }
//                processException(ret, d);
//        } else {
//            return l.stream()
//                    .filter(Entry.class::isInstance)
//                    .map(Entry.class::cast)
//                    .filter(t -> Arrays.stream(units).anyMatch(t.getIdentity()::equals))
//                    .flatMap(e -> e.getChildren().stream())
//                    .filter(Entry.class::isInstance)
//                    .map(Entry.class::cast)
//                    .filter(e -> term.equals(e.getIdentity()))
//                    .map(Entry::getIdentity)
//                    .map(DocumentId.class::cast)
//                    .toArray(DocumentId[]::new);
//        }
//                return ServiceRemoteTargetAssessmentDocument.create(d, ret, service.getInfo().getURL(), findJMSTopicListenerServiceProvider(), findNamingResolver());
        return RemoteTextTargetAssessmentDocument.create(d, ret, service.getInfo().getURL(), findJMSTopicListenerService(), null);
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

    DocumentEntry<?> fetchUnitDocument(UnitId unit, Properties p) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @NbBundle.Messages({"ServiceTargetAssessmentDocumentFactory.processException.message=Beim Verarbeiten von {0} ist auf dem Server ein Fehler aufgetretn.",
        "ServiceTargetAssessmentDocumentFactory.processException.header====== Server-Fehler =====",
        "ServiceTargetAssessmentDocumentFactory.processException.finisher====== Ende ====="})
    static void processException(Template<?> t, Identity key) throws IOException {
        final ExceptionMessage pre = t.getException();
        if (pre == null) {
            return;
        }
        final StringJoiner sj = new StringJoiner("/n");
        sj.add(NbBundle.getMessage(TextTargetAssessmentDocumentFactory.class, "ServiceTargetAssessmentDocumentFactory.processException.header"));
        sj.add(NbBundle.getMessage(TextTargetAssessmentDocumentFactory.class, "ServiceTargetAssessmentDocumentFactory.processException.message", key.toString()));
        sj.add(pre.getUserMessage());
        sj.add(pre.getLogMessage());
        sj.add(pre.getStackTraceElement());
        sj.add(NbBundle.getMessage(TextTargetAssessmentDocumentFactory.class, "ServiceTargetAssessmentDocumentFactory.processException.finisher"));
//        PlatformUtil.getCodeNameBaseLogger(ServiceTargetAssessmentDocumentFactory.class).log(Level.SEVERE, sj.toString());
        throw new IOException(sj.toString());
    }
}
