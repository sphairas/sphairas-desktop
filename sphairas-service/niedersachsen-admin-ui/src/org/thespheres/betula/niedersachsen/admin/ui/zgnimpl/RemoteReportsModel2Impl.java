/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.zgnimpl;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.Icon;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.apache.commons.lang3.StringUtils;
import org.netbeans.spi.project.LookupProvider;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Entry;
import org.thespheres.betula.document.Envelope;
import org.thespheres.betula.document.ExceptionMessage;
import org.thespheres.betula.document.util.DocumentUtilities;
import org.thespheres.betula.document.util.XmlDocumentEntry;
import org.thespheres.betula.niedersachsen.admin.ui.RemoteReportsModel2;
import org.thespheres.betula.niedersachsen.admin.ui.ReportData2;
import org.thespheres.betula.niedersachsen.xml.NdsZeugnisAngaben;
import org.thespheres.betula.niedersachsen.zeugnis.TermReportNoteSetTemplate;
import org.thespheres.betula.services.ws.Paths;
import org.thespheres.betula.services.ws.ServiceException;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.ui.util.LogLevel;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.util.CollectionChangeEvent;
import org.thespheres.betula.util.ContainerBuilder;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"RemoteReportsModel2Impl.status.initError.title=Fehler beim Laden der Zeugnisse"})
public class RemoteReportsModel2Impl extends RemoteReportsModel2 {

    private static JAXBContext jaxb;

    public static synchronized JAXBContext getZeungnisAngabenJAXB() {
        if (jaxb == null) {
            try {
                jaxb = JAXBContext.newInstance(NdsZeugnisAngaben.class);
            } catch (JAXBException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return jaxb;
    }

    public static void notifyError(final Exception ex, final String message) {
        PlatformUtil.getCodeNameBaseLogger(RemoteReportsModel2Impl.class).log(LogLevel.INFO_WARNING, ex.getMessage(), ex);
        final Icon ic = ImageUtilities.loadImageIcon("org/thespheres/betula/ui/resources/exclamation-red-frame.png", true);
        final String title = NbBundle.getMessage(RemoteReportsModel2Impl.class, "RemoteReportsModel2Impl.status.initError.title");
        NotificationDisplayer.getDefault().notify(title, ic, message, null, NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.WARNING);
    }

    public static void notifyError(final ExceptionMessage em) {
        PlatformUtil.getCodeNameBaseLogger(RemoteReportsModel2Impl.class).log(LogLevel.INFO_WARNING, "Remote log message: {0}", em.getLogMessage());
        PlatformUtil.getCodeNameBaseLogger(RemoteReportsModel2Impl.class).log(LogLevel.INFO_WARNING, "Begin remote stack strace: {0}", em.getStackTraceElement());
        final Icon ic = ImageUtilities.loadImageIcon("org/thespheres/betula/ui/resources/exclamation-red-frame.png", true);
        final String title = NbBundle.getMessage(RemoteReportsModel2Impl.class, "RemoteReportsModel2Impl.status.initError.title");
        NotificationDisplayer.getDefault().notify(title, ic, StringUtils.defaultIfBlank(em.getUserMessage(), ""), null, NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.WARNING);
    }

    private final Set<TermId> loading = new HashSet<>();

    RemoteReportsModel2Impl(PrimaryUnitOpenSupport uos) throws IOException {
        super(uos);
    }

    WebServiceProvider getService() {
        try {
            return support.findWebServiceProvider();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    void loadTerm(final TermId term, final LocalDate forceAsOf) {
        synchronized (cache) {
            if (loading.contains(term)) {
                return;
            }
            if (forceAsOf != null || !cache.containsKey(term)) {
                loading.add(term);
                executor().post(() -> {
                    try {
                        loadTermImpl(term, forceAsOf);
                    } catch (IOException ex) {
                        notifyError(ex, ex.getLocalizedMessage());
                        synchronized (cache) {
                            loading.remove(term);
                        }
                    }
                });
            }
        }
    }

    protected RequestProcessor executor() {
        final boolean useDefault = NbPreferences.forModule(RemoteReportsModel2Impl.class).getBoolean("RemoteReportsModel2Impl.executor.serialize", false);
        return useDefault ? getService().getDefaultRequestProcessor() : RP;
    }

    private boolean loadTermImpl(final TermId term, final LocalDate asOf) throws IOException {
        final ContainerBuilder builder = new ContainerBuilder();
        final UnitId pu = support.getUnitId();
        final Entry<UnitId, ?> ue = new Entry<>(null, pu);
        if (asOf != null) {
            ue.getHints().put("asOf", asOf.toString());
        }
        if (term != null) {
            final Entry<TermId, ?> te = new Entry<>(Action.REQUEST_COMPLETION, term);
            ue.getChildren().add(te);
        } else {
            ue.getHints().put("term.reports.no.document", "false");
        }
        builder.add(ue, Paths.UNITS_REPORTS_PATH);
        final Container ret;
        try {
            ret = getService().createServicePort().solicit(builder.getContainer());
        } catch (ServiceException ex) {
            notifyError(ex, ex.getLocalizedMessage());
            return false;
        } catch (RuntimeException other) {
            throw new IOException(other);
        }
        final List<Envelope> l = DocumentUtilities.findEnvelope(ret, Paths.UNITS_REPORTS_PATH).stream()
                .peek(e -> {
                    final ExceptionMessage em;
                    if ((em = e.getException()) != null) {
                        notifyError(em);
                    }
                })
                .filter(e -> e.getException() == null)
                .collect(Collectors.toList());
        final Map<StudentId, List<ReportData2>> entries = createReports(l, pu, term);

        synchronized (cache) {
            cache.put(term, entries);
            loading.remove(term);
        }

        final CollectionChangeEvent cce = new CollectionChangeEvent(this, COLLECTION_TERMS, term, CollectionChangeEvent.Type.ADD);
        eventBus.post(cce);
        return true;
    }

    Map<StudentId, List<ReportData2>> createReports(final List<Envelope> l, final UnitId pu, final TermId term) throws IOException {
        return l.stream()
                .filter(Entry.class::isInstance)
                .map(Entry.class::cast)
                .filter(e -> e.getIdentity().equals(pu))
                .flatMap(e -> e.getChildren().stream())
                .filter(Entry.class::isInstance)
                .map(Entry.class::cast)
                .filter(e -> e.getIdentity().equals(term))
                .flatMap(e -> e.getChildren().stream())
                .filter(Entry.class::isInstance)
                .map(Entry.class::cast)
                .filter(e -> e.getIdentity() instanceof StudentId)
                .collect(Collectors.toMap(e -> (StudentId) e.getIdentity(), e -> toReportData(e, term)));
    }

    private List<ReportData2> toReportData(Entry<StudentId, ?> studentEntry, final TermId term) {
        final StudentId student = studentEntry.getIdentity();
        return studentEntry.getChildren().stream()
                .map(t -> {
                    if (t instanceof Entry && ((Entry) t).getIdentity() instanceof DocumentId) {
                        final Entry<DocumentId, ?> re = (Entry<DocumentId, ?>) t;
                        final ReportData2Impl ret = new ReportData2Impl(re.getIdentity(), term, student, this);
                        if (re instanceof XmlDocumentEntry) {
                            org.w3c.dom.Element xml = ((XmlDocumentEntry) re).getReportDataElement();
                            if (xml != null) {
                                NdsZeugnisAngaben angaben;
                                try {
                                    angaben = (NdsZeugnisAngaben) getZeungnisAngabenJAXB().createUnmarshaller().unmarshal(xml);
                                } catch (JAXBException | ClassCastException ex) {
                                    throw new IllegalStateException(ex);
                                }
                                if (angaben != null) {
                                    ret.initializeData(angaben);
                                }
                            }
                        }
                        return ret;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    protected TermReportNoteSetTemplate findReportNotesTemplate() throws IOException {
        return ReportNotesTemplateAccess.find(reportNoteSetTemplateUrl(), this);
    }

    @ServiceProvider(service = LookupProvider.class, path = "Loaders/application/betula-unit-data/Lookup")
    public static final class ReportsModelRegistration implements LookupProvider {

        @Override
        public Lookup createAdditionalLookup(Lookup base) {
            final PrimaryUnitOpenSupport puos = base.lookup(PrimaryUnitOpenSupport.class);
            if (puos != null) {
                try {
                    return Lookups.singleton(new RemoteReportsModel2Impl(puos));
                } catch (IllegalStateException | IOException e) {
                    Logger.getLogger(getClass().getCanonicalName()).log(Level.SEVERE, "Could not initialize RemoteReportsModel.", e);
                }
            }
            return Lookup.EMPTY;
        }
    }

}
