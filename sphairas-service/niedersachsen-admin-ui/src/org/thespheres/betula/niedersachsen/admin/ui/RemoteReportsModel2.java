/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui;

import com.google.common.eventbus.EventBus;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.undo.UndoableEditSupport;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.admin.units.RemoteStudents;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.model.History;
import org.thespheres.betula.niedersachsen.zeugnis.NdsReportBuilderFactory;
import org.thespheres.betula.niedersachsen.zeugnis.TermReportNoteSetTemplate;
import org.thespheres.betula.niedersachsen.zeugnis.TermReportNoteSetTemplate.MarkerItem;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.ui.ConfigurationException;
import org.thespheres.betula.services.ui.util.dav.URLs;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.util.StudentComparator;

/**
 *
 * @author boris.heithecker
 */
public abstract class RemoteReportsModel2 implements History<RemoteStudent, ReportData2> {

    public static final String COLLECTION_TERMS = "terms";
    public final PrimaryUnitOpenSupport support;
    protected final Map<TermId, Map<StudentId, List<ReportData2>>> cache = new HashMap<>();
    protected final EventBus eventBus = new EventBus();
    public final static RequestProcessor RP = new RequestProcessor("RemoteReportsModel2", 16);
    private RemoteStudents remoteStudents;
    private final UndoableEditSupport undoSupport = new UndoableEditSupport(this);
    final StudentComparator sc = new StudentComparator();
    private final String[] reportNoteSetUrl = new String[]{null};
    private LocalProperties properties;

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    protected RemoteReportsModel2(final PrimaryUnitOpenSupport uos) throws IOException {
        this.support = uos;
//        this.reportNoteSetUrl = reportNoteSetTemplateUrl();
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public PrimaryUnitOpenSupport getPrimaryUnitOpenSupport() {
        return support;
    }

    RemoteStudents getRemoteStudents() {
        synchronized (this) {
            if (remoteStudents == null) {
                try {
                    remoteStudents = RemoteStudents.get(support.findWebServiceProvider().getInfo().getURL());
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
            }
            return remoteStudents;
        }
    }

    public UndoableEditSupport getUndoSupport() {
        return undoSupport;
    }

    @Override
    public List<RemoteStudent> getStudents() {
        final Set<StudentId> s;
        synchronized (cache) {
            s = cache.values().stream()
                    .flatMap(m -> m.keySet().stream())
                    .collect(Collectors.toSet());
        }
        return s.stream()
                .map(getRemoteStudents()::find)
                .collect(Collectors.toList());
    }

    @Override
    public Set<DocumentId> getReportDocuments(StudentId student) {
        synchronized (cache) {
            return cache.values().stream()
                    .map(m -> m.get(student))
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .map(ReportData2::getDocumentId)
                    .collect(Collectors.toSet());
        }
    }

    public List<ReportData2> getReports(final TermId term) {
        synchronized (cache) {
            return cache.getOrDefault(term, (Map<StudentId, List<ReportData2>>) Collections.EMPTY_MAP).values().stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
        }
    }

    public Set<DocumentId> getReportDocuments(final TermId term) {
        synchronized (cache) {
            return cache.get(term).values().stream()
                    .flatMap(List::stream)
                    .map(ReportData2::getDocumentId)
                    .collect(Collectors.toSet());
        }
    }

    @Override
    public ReportData2 getReportDocument(DocumentId did) {
        synchronized (cache) {
            return cache.values().stream()
                    .flatMap(m -> m.values().stream())
                    .flatMap(List::stream)
                    .filter(rd -> rd.getDocumentId().equals(did))
                    .collect(CollectionUtil.requireSingleOrNull());
        }
    }

    public List<ReportData2> getStudentsForTerm(final TermId term) {

        synchronized (cache) {
            if (cache.containsKey(term)) {
                return cache.get(term).values().stream()
                        .flatMap(List::stream)
                        .sorted(Comparator.comparing(ReportData2::getRemoteStudent, sc))
                        .collect(Collectors.toList());
            }
        }
        return Collections.EMPTY_LIST;
    }

    public LocalProperties getLocalFileProperties() {
        if (properties == null) {
            try {
                properties = support.findBetulaProjectProperties();
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return properties;
    }

    public synchronized TermReportNoteSetTemplate getTermReportNoteSetTemplate() {
        try {
            return findReportNotesTemplate();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public int positionOf(final Marker item) {
        return getTermReportNoteSetTemplate().getElements().stream()
                .filter(el -> el.getMarkers().stream().map(MarkerItem::getMarker).anyMatch(item::equals))
                .map(getTermReportNoteSetTemplate().getElements()::indexOf)
                .filter(Objects::nonNull)
                .collect(CollectionUtil.singleton())
                .orElse(Integer.MAX_VALUE);
    }

    protected String reportNoteSetTemplateUrl() {
        synchronized (reportNoteSetUrl) {
            if (reportNoteSetUrl[0] == null) {
                final String base;
                try {
                    base = URLs.adminResourcesDavBase(support.findBetulaProjectProperties());
//                base = support.findBetulaProjectProperties().getProperty("resources.dav.base");
                } catch (IOException | ConfigurationException ex) {
                    throw new IllegalStateException(ex);
                }
                final String file = NdsReportBuilderFactory.SIGNEE_BEMERKUNGEN_FILE;
                reportNoteSetUrl[0] = base + file;
            }
        }
        return reportNoteSetUrl[0];
    }

    protected abstract TermReportNoteSetTemplate findReportNotesTemplate() throws IOException;

}
