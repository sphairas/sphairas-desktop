/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoableEditSupport;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.admin.units.impl.MarkerDecorationImpl;
import org.thespheres.betula.assess.TargetDocument;
import org.thespheres.betula.services.client.jms.JMSTopicListenerService;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.document.UniqueMarkerSet;
import org.thespheres.betula.document.model.MarkerDecoration;
import org.thespheres.betula.document.model.MultiSubject;
import org.thespheres.betula.document.model.Subject;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.LocalFileProperties;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.ui.ConfigurationPanelLookupHint;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
public abstract class AbstractTargetAssessmentDocument implements TargetDocument, ConfigurationPanelLookupHint {

    public static final String ABSTRACT_TARGET_ASSESSMENT_HINT = "AbstractTargetAssessmentDocument";
    public static final String PROP_MARKERS = "markers";
    public static final String PROP_TARGETTYPE = "targetType";
    protected final DocumentId document;
    protected LocalDate exp;
    protected final Set<Marker> markers = new HashSet<>();
    protected final NamingResolver namingResolver;
    protected NamingResolver.Result namingResolverResult;
    protected final JMSTopicListenerService jmsDocumentsService;
    protected final PropertyChangeSupport pSupport = new PropertyChangeSupport(this);
    protected final UndoableEditSupport undoSupport = new UndoableEditSupport(this);
    private final SubjectHolder subjectHolder;
    private LocalFileProperties properties;
    private MarkerDecoration markerDecoration;
    protected final String provider;

    protected AbstractTargetAssessmentDocument(final DocumentId document, final String provider, final JMSTopicListenerService jms, NamingResolver namingResolver) {
        this.document = document;
        this.provider = provider;
        this.namingResolver = namingResolver;
        this.jmsDocumentsService = jms;
        this.subjectHolder = new SubjectHolder();
    }

    public DocumentId getDocumentId() {
        return document;
    }

    public String getProvider() {
        return provider;
    }

    public abstract Set<StudentId> students();

    public abstract Set<TermId> identities();

    public LocalDate getDateOfExpiry() {
        return exp;
    }

    protected LocalFileProperties getLocalProperties() {
        if (properties == null) {
            properties = LocalFileProperties.find(provider);
        }
        return properties;
    }

    protected MarkerDecoration getMarkerDecoration() {
        if (markerDecoration == null) {
            markerDecoration = createMarkerDecoration();
        }
        return markerDecoration;
    }

    protected MarkerDecoration createMarkerDecoration() {
        return new MarkerDecorationImpl(getLocalProperties());
    }

    public NamingResolver.Result getNamingResolverResult() {
        if (namingResolverResult == null && namingResolver != null) {
            try {
                namingResolverResult = namingResolver.resolveDisplayNameResult(document);
            } catch (IllegalAuthorityException ex) {
                throw new IllegalArgumentException(ex);
            }
        }
        return namingResolverResult;
    }

    @Override
    public Marker[] markers() {
        synchronized (markers) {
            return markers.stream().toArray(Marker[]::new);
        }
    }

    public Optional<Marker> getUniqueMarker(final String convention) {
        return Arrays.stream(markers())
                .filter(m -> m.getConvention().equals(convention))
                .collect(CollectionUtil.singleton());
    }

    @Deprecated
    public Subject getSubject() {
        return (Subject) subjectHolder.getSubject(false);
    }

    public MultiSubject getMultiSubject() {
        return (MultiSubject) subjectHolder.getSubject(true);
    }

    public abstract Timestamp timestamp(StudentId student, TermId gradeId);

    @Override
    public String getContentType() {
        return ABSTRACT_TARGET_ASSESSMENT_HINT;
    }

    @Override
    public String getDisplayName() {
        return document.getId();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pSupport.removePropertyChangeListener(listener);
    }

    public UndoableEditSupport getUndoSupport() {
        return undoSupport;
    }

    public synchronized void addUndoableEditListener(UndoableEditListener l) {
        undoSupport.addUndoableEditListener(l);
    }

    public synchronized void removeUndoableEditListener(UndoableEditListener l) {
        undoSupport.removeUndoableEditListener(l);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.document);
        return 37 * hash + Objects.hashCode(this.provider);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AbstractTargetAssessmentDocument)) {
            return false;
        }
        final AbstractTargetAssessmentDocument other = (AbstractTargetAssessmentDocument) obj;
        if (!Objects.equals(this.provider, other.provider)) {
            return false;
        }
        return Objects.equals(this.document, other.document);
    }

    class SubjectHolder implements PropertyChangeListener {

        private final Object[] subject = new Object[]{null, null};
        private boolean reset = true;

        private SubjectHolder() {
        }

        Object getSubject(boolean multi) {
            synchronized (subject) {
                if (reset) {
                    try {
                        final Subject s = findSubject();
                        subject[0] = s;
                    } catch (IOException ex) {
                        subject[0] = null;
                    }
                    try {
                        final MultiSubject s = findMultiSubject();
                        subject[1] = s;
                    } catch (IOException ex) {
                        subject[1] = null;
                    }
                    final PropertyChangeListener pcl = WeakListeners.propertyChange(this, AbstractTargetAssessmentDocument.this);
                    addPropertyChangeListener(pcl);
                    reset = false;
                }
            }
            return multi ? (MultiSubject) subject[1] : (Subject) subject[0];
        }

        private Subject findSubject() throws IOException {
            final MarkerDecoration deco = getMarkerDecoration();
            final UniqueMarkerSet sms = deco.getDistinguishingDecoration(getDocumentId(), AbstractTargetAssessmentDocument.this, "subject");
            final UniqueMarkerSet realm = deco.getDistinguishingDecoration(getDocumentId(), AbstractTargetAssessmentDocument.this, "realm");
            final String[] arr = getLocalProperties().getProperty(LocalFileProperties.PROP_UNIQUE_SUBJECT_CONVENTIONS, "").split(",");
            final Marker subjectMarker = sms.getUnique(arr);
            final String[] arr2 = getLocalProperties().getProperty(LocalFileProperties.PROP_REALM_CONVENTIONS, "").split(",");
            final Marker realmMarker = realm.getUnique(arr2);
            return subjectMarker != null ? new Subject(subjectMarker, realmMarker) : null;
        }

        private MultiSubject findMultiSubject() throws IOException {
            final MarkerDecoration deco = getMarkerDecoration();
            final UniqueMarkerSet realm = deco.getDistinguishingDecoration(getDocumentId(), AbstractTargetAssessmentDocument.this, "realm");
            final String[] arr = getLocalProperties().getProperty(LocalFileProperties.PROP_UNIQUE_SUBJECT_CONVENTIONS, "").split(",");
            final Set<Marker> subjectMarkers = Arrays.stream(markers())
                    .filter(m -> Arrays.stream(arr).anyMatch(m.getConvention()::equals))
                    .collect(Collectors.toSet());
            final String[] arr2 = getLocalProperties().getProperty(LocalFileProperties.PROP_REALM_CONVENTIONS, "").split(",");
            final Marker realmMarker = realm.getUnique(arr2);
            return !subjectMarkers.isEmpty() ? new MultiSubject(realmMarker, subjectMarkers) : null;
        }
//        private Subject findSubject() throws IOException {
//            final MarkerDecoration deco = support.findMarkerDecoration();
//            final UniqueMarkerSet sms = deco.getDistinguishingDecoration(getDocumentId(), AbstractTargetAssessmentDocument.this, "subject");
//            final UniqueMarkerSet realm = deco.getDistinguishingDecoration(getDocumentId(), AbstractTargetAssessmentDocument.this, "realm");
//            final String[] arr = support.findBetulaProjectProperties().getProperty(LocalFileProperties.PROP_UNIQUE_SUBJECT_CONVENTIONS, "").split(",");
//            final Marker subjectMarker = sms.getUnique(arr);
//            final String[] arr2 = support.findBetulaProjectProperties().getProperty(LocalFileProperties.PROP_REALM_CONVENTIONS, "").split(",");
//            final Marker realmMarker = realm.getUnique(arr2);
//            return subjectMarker != null ? new Subject(subjectMarker, realmMarker) : null;
//        }
//
//        private MultiSubject findMultiSubject() throws IOException {
//            final MarkerDecoration deco = support.findMarkerDecoration();
//            final UniqueMarkerSet realm = deco.getDistinguishingDecoration(getDocumentId(), AbstractTargetAssessmentDocument.this, "realm");
//            final String[] arr = support.findBetulaProjectProperties().getProperty(LocalFileProperties.PROP_UNIQUE_SUBJECT_CONVENTIONS, "").split(",");
//            final Set<Marker> subjectMarkers = Arrays.stream(markers())
//                    .filter(m -> Arrays.stream(arr).anyMatch(m.getConvention()::equals))
//                    .collect(Collectors.toSet());
//            final String[] arr2 = support.findBetulaProjectProperties().getProperty(LocalFileProperties.PROP_REALM_CONVENTIONS, "").split(",");
//            final Marker realmMarker = realm.getUnique(arr2);
//            return !subjectMarkers.isEmpty() ? new MultiSubject(realmMarker, subjectMarkers) : null;
//        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (AbstractTargetAssessmentDocument.PROP_MARKERS.equals(evt.getPropertyName())) {
                synchronized (subject) {
                    reset = true;
                }
            }
        }

    }

    @NbBundle.Messages("TargetAssessmentDocumentCreationException.message=Could not create TargetAssessmentDocument {0}, caught {1}.")
    public static class TargetAssessmentDocumentCreationException extends IOException {

        private final DocumentId[] documents;

        public TargetAssessmentDocumentCreationException(final DocumentId document, final Exception e) {
            super(e);
            this.documents = new DocumentId[]{document};
        }

        public TargetAssessmentDocumentCreationException(final DocumentId[] documents, final Exception e) {
            super(e);
            this.documents = documents;
        }

        public DocumentId[] getDocuments() {
            return documents;
        }

        @Override
        public String getMessage() {
            final String c = getCause() != null ? getCause().getClass().getCanonicalName() : null;
            final String d = Arrays.stream(documents)
                    .map(DocumentId::getId)
                    .collect(Collectors.joining(", "));
            return NbBundle.getMessage(TargetAssessmentDocumentCreationException.class, "TargetAssessmentDocumentCreationException.message", d, c);
        }

    }
}
