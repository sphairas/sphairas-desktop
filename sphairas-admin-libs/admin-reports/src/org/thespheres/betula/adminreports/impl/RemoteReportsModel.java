/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminreports.impl;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.swing.text.StyledDocument;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.admin.units.AbstractTargetAssessmentDocument.TargetAssessmentDocumentCreationException;
import org.thespheres.betula.admin.units.RemoteStudents;
import org.thespheres.betula.admin.units.util.Util;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.reports.model.Report;
import org.thespheres.betula.reports.model.Report.ReportCollection;

/**
 *
 * @author boris.heithecker
 */
public class RemoteReportsModel implements Lookup.Provider {

    public static final String PROP_SECTION_CONVENTION = "section.convention";
    public static final String PROP_MODIFIED = "modified";
    private boolean modif;
    private final RequestProcessor RP = new RequestProcessor(RemoteReportsModel.class.getCanonicalName(), 1, true);
    final InstanceContent ic = new InstanceContent(RP);
    private final Lookup lookup;
    private final PropertyChangeSupport pSupport = new PropertyChangeSupport(this);
    private Date lastTime = new Date(0l);
    private final RemoteReportsDescriptor descriptor;
    private final String id;
    private final HashSet<RemoteTextTargetAssessmentDocument> targets = new HashSet<>();
    private final HashSet<RemoteTextTargetAssessmentDocument> targetsLoading = new HashSet<>();
    private final Set<DocumentId> documents = new HashSet<>();
    private final HashSet<TermId> terms = new HashSet<>();
    private final Set<DocumentId> docsPreparing = new HashSet<>();
    private boolean initialized;
    private boolean initializing;
    private ReportCollection collection;

    @SuppressWarnings("LeakingThisInConstructor")
    public RemoteReportsModel(RemoteReportsDescriptor descriptor, String uniqueName, Lookup baseLookup) throws IOException {
        this.descriptor = descriptor;
        this.id = uniqueName;
        lookup = new ProxyLookup(new Lookup[]{baseLookup, new AbstractLookup(ic)});
        ic.add(this);
        ic.add(RemoteStudents.get(descriptor.getProvider()));
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    public RemoteReportsDescriptor getDescriptor() {
        return descriptor;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return descriptor.getDisplayName();
    }

    public boolean isInitialized() {
        return initialized;
    }

    void initialize() {
        synchronized (this) {
            if (initialized || initializing) {
                return;
            } else {
                initializing = true;
            }
        }
        doInit();
        synchronized (this) {
            initializing = false;
        }
    }

    public Set<RemoteTextTargetAssessmentDocument> getTargets() {
        return targets;
    }

    public HashSet<TermId> getTerms() {
        return terms;
    }

    private void doInit() {
//        final TextTargetAssessmentDocumentBean docBean = getRemoteLookup().lookup(TextTargetAssessmentDocumentBean.class);
//        if (docBean == null) {
//            return;
//        }
        Arrays.stream(descriptor.getTargetSet()).forEach(documents::add);
        terms.clear();
        long start = System.currentTimeMillis();
        int size = documents.size();

        final class Init implements Runnable {

            private final DocumentId d;

            private Init(DocumentId d) {
                this.d = d;
            }

            @Override
            public void run() {
                try {
                    final RemoteTextTargetAssessmentDocument rtad = TextTargetAssessmentDocumentFactory.get(descriptor.getProvider()).getTargetAssessmentDocument(d);
                    if (rtad != null) {
                        targetsLoading.add(rtad);
                        terms.addAll(rtad.identities());
                        synchronized (docsPreparing) {
                            docsPreparing.remove(d);
                            docsPreparing.notify();
                        }
                    }
                } catch (TargetAssessmentDocumentCreationException ex) {
                    Exceptions.printStackTrace(ex);
//                    Util.RP(getRemoteLookup()).post(this, 0, Thread.NORM_PRIORITY);
                }
            }

        }

        docsPreparing.clear();
        docsPreparing.addAll(documents);
        documents.stream().forEach(d -> Util.RP(descriptor.getProvider()).post(new Init(d)));

        synchronized (docsPreparing) {
            while (!docsPreparing.isEmpty()) {
                try {
                    docsPreparing.wait();
                } catch (InterruptedException ex) {
                }
            }
        }

        long dur = System.currentTimeMillis() - start;
        synchronized (targets) {
            targets.clear();
            targets.addAll(targetsLoading);
            targetsLoading.clear();
        }
//        final StringJoiner unames = new StringJoiner(",");
//        Arrays.stream(units).forEach(uid -> unames.add(uid.getId()));
//        Logger.getLogger(RemoteUnitsModel.class.getCanonicalName()).log(Level.INFO, "Initialized view of unit(s) {0} ({2} documents) in {1} ms.", new Object[]{unames.toString(), Long.toString(dur), Integer.toString(size)});
        synchronized (this) {
            lastTime = new Date();
            initialized = true;
        }
//        cSupport.fireChange();
        createReportCollection();
    }

    public ReportCollection getReportsCollection() {
        return collection;
    }

    private void createReportCollection() {
        final ReportCollection ret = new ReportCollection();
        class Index {

            final RemoteTextTargetAssessmentDocument d;
            final TermId t;
            final StudentId s;
            final Marker sec;

            private Index(RemoteTextTargetAssessmentDocument d, StudentId s, TermId term, Marker sec) {
                this.d = d;
                this.t = term;
                this.s = s;
                this.sec = sec;
            }

            boolean notEmpty() {
                return d.getText(s, t, sec) != null;
            }
        }
        final List<Index> l = new ArrayList<>();
        synchronized (targets) {
            targets.stream().forEach(d -> {
                d.identities().stream()
                        .forEach(term -> {
                            d.sections().stream()
                                    .forEach(sec -> d.students().stream()
                                    .map(s -> new Index(d, s, term, sec))
                                    .filter(Index::notEmpty)
                                    .forEach(l::add));
//                            .forEach(s -> {
//                                Index i = new Index(d, term, s);
//                                Report r = ret.addReport(d.getDocumentId().getId());
//                                r.setDocument(d.getDocumentId());
//                                r.setStudent(s);
//                                r.setTerm(term);
//                                r.setText(d.getText(s, term));
//                            }););
                        });
            });
        }
        l.stream()
                .sorted(Comparator.comparingLong(i -> i.s.getId()))
                .sorted(Comparator.comparingInt(i -> i.t.getId()))
                //                .sorted(Comparator.comparing(i -> i.d.markers().)
                .forEach(i -> {
                    final Report r = ret.addReport(i.d.getDocumentId().getId());
                    r.setDocument(i.d.getDocumentId());
                    r.setStudent(i.s);
                    r.setTerm(i.t);
                    if (!Marker.isNull(i.sec)) {
                        r.setProperty(PROP_SECTION_CONVENTION, i.sec.getConvention());
                        r.getMarkers().add(i.sec);
                    }
                    r.setText(i.d.getText(i.s, i.t, i.sec));
                });
        this.collection = ret;
    }

    public RemoteEditableReportCollection createEditableReportCollection(StyledDocument document) {
        return new RemoteEditableReportCollection(document, getLookup());
    }

    public void setModified(boolean b) {
        boolean before = this.modif;
        synchronized (this) {
            modif = b;
        }
        pSupport.firePropertyChange(PROP_MODIFIED, before, this.modif);
    }

    public boolean isModified() {
        synchronized (this) {
            return modif;
        }
    }

    public Date getTime() {
        return lastTime;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pSupport.removePropertyChangeListener(listener);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        return 79 * hash + Objects.hashCode(this.id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RemoteReportsModel other = (RemoteReportsModel) obj;
        return Objects.equals(this.id, other.id);
    }

}
