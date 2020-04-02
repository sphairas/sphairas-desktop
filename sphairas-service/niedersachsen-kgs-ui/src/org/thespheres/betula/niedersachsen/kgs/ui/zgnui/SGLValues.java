/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.kgs.ui.zgnui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.swing.Icon;
import org.netbeans.spi.project.LookupProvider;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.*;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.admin.units.RemoteUnitsModel;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Entry;
import org.thespheres.betula.document.Envelope;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.Template;
import org.thespheres.betula.document.util.DocumentUtilities;
import org.thespheres.betula.document.util.MarkerAdapter;
import org.thespheres.betula.services.ProviderRegistry;
import org.thespheres.betula.services.ws.Paths;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.sibank.SiBankImportTarget;
import org.thespheres.betula.sibank.SiBankPlus;
import org.thespheres.betula.ui.util.LogLevel;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.util.ContainerBuilder;
import org.thespheres.betula.xmlimport.ImportTargetFactory;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({
    "SGLValues.message.noStudentCareersDocumentId=Keine SGL-Dokument-Eigenschaft definiert für {0}.",
    "SGLValues.message.ioexception.message=Fehler bei der Initialisierung der Schulzweig-Zugehörigkeiten.",
    "SGLValues.status.retry=Enqueuing {0}. retry to load ticket from {1}.",
    "SGLValues.status.initError.title=Fehler beim Initialisierungen der Schulzweig-Zugehörigkeiten.",
    "SGLValues.status.noCareerDocument.message=Keine Schulzweig-Liste für {0} gefunden."})
public class SGLValues {

    protected DocumentId studentCareersDocumentId;
    private final PrimaryUnitOpenSupport support;
    private WebServiceProvider service;
    private final Listener listener = new Listener();
    private RemoteUnitsModel model;
    private final static Set<String> NOTIFIED_PROVIDERS = new HashSet<>();

    private SGLValues(PrimaryUnitOpenSupport support) {
        this.support = support;
    }

    static SGLValues create(final PrimaryUnitOpenSupport support) {
        final SGLValues ret = new SGLValues(support);
        support.getRP().post(ret::initialize);
        return ret;
    }

    private void initialize() {
        final String provider;
        try {
            provider = support.findBetulaProjectProperties().getProperty("providerURL");
        } catch (IOException ioex) {
            final String msg = NbBundle.getMessage(SGLValues.class, "SGLValues.message.ioexception.message");
            notifyError(ioex, msg);
            return;
        }
        try {
            service = support.findWebServiceProvider();
            studentCareersDocumentId = findCareerDocument(provider);
            model = support.getRemoteUnitsModel(RemoteUnitsModel.INITIALISATION.STUDENTS);
        } catch (IOException ioex) {
            final String msg = NbBundle.getMessage(SGLValues.class, "SGLValues.message.ioexception.message");
            notifyError(ioex, msg);
            return;
        } catch (IllegalStateException illex) {
            final String msg = NbBundle.getMessage(SGLValues.class, "SGLValues.message.noStudentCareersDocumentId", ProviderRegistry.getDefault().get(provider).getDisplayName());
            final boolean notified;
            synchronized (NOTIFIED_PROVIDERS) {
                notified = NOTIFIED_PROVIDERS.contains(provider);
                if (!notified) {
                    NOTIFIED_PROVIDERS.add(provider);
                }
            }
            if (!notified) {
                notifyError(illex, msg);
            }
            return;
        }
        if (studentCareersDocumentId
                == null) {
            final String msg = NbBundle.getMessage(SGLValues.class, "SGLValues.status.noCareerDocument.message", support.getNodeDelegate().getDisplayName());
            PlatformUtil.getCodeNameBaseLogger(SGLValues.class).log(Level.INFO, msg);
            return;
        }

        model.addPropertyChangeListener(listener);

        reload();
    }

    public void reload() {
        if (!model.getInitialization().satisfies(RemoteUnitsModel.INITIALISATION.STUDENTS)) {
            return;
        }
        final List<RemoteStudent> students = model.getStudents();
        final ContainerBuilder builder = new ContainerBuilder();
        final Template t = builder.createTemplate(null, studentCareersDocumentId, null, Paths.STUDENTS_MARKERS_PATH, null, null);
//            t.getHints().put(support, support); date-as-of
        students.stream()
                .map(RemoteStudent::getStudentId)
                .map(s -> new Entry(Action.REQUEST_COMPLETION, s))
                .forEach(ch -> t.getChildren().add(ch));
        Container response = null;
        try {
            response = NetworkSettings.suppressAuthenticationDialog(() -> service.createServicePort().solicit(builder.getContainer()));
        } catch (Exception ex) {
            if (ex instanceof IOException) {
//                lastException = (IOException) ex;
            } else {
//                lastException = new IOException(ex);
            }
//            state = UIExceptions.handleServiceException(Targets.class.getName(), providerUrl, ex);
        }
        if (response == null) {
            return;
        }
        final List<Envelope> l = DocumentUtilities.findEnvelope(response, Paths.STUDENTS_MARKERS_PATH);
        final Map<StudentId, Marker> m = l.stream()
                .filter(node -> node instanceof Entry && ((Entry) node).getIdentity() instanceof DocumentId && ((DocumentId) ((Entry) node).getIdentity()).equals(studentCareersDocumentId))
                .flatMap(node -> node.getChildren().stream())
                .filter(node -> node instanceof Entry && ((Entry) node).getIdentity() instanceof StudentId)
                .map(node -> (Entry<StudentId, ?>) node)
                .collect(Collectors.toMap(Entry::getIdentity, e -> e.getValue() instanceof MarkerAdapter ? ((MarkerAdapter) e.getValue()).getMarker() : Marker.NULL));
        students.stream()
                .filter(rs -> m.containsKey(rs.getStudentId()))
                .forEach(rs -> rs.putClientProperty("sgl", m.get(rs.getStudentId())));
    }

    private static DocumentId findCareerDocument(final String url) throws IOException {
        if (url != null) {
            final SiBankImportTarget fac = ImportTargetFactory.find(url, SiBankImportTarget.class,
                    SiBankPlus.getProduct());
            if (fac != null) {
                return fac.getStudentCareersDocumentId();
            }
        }
        return null;
    }

    static void notifyError(Exception ex, String message) {
        PlatformUtil.getCodeNameBaseLogger(SGLValues.class
        ).log(LogLevel.INFO_WARNING, ex.getMessage(), ex);
        final Icon ic = ImageUtilities.loadImageIcon("org/thespheres/betula/ui/resources/exclamation-red-frame.png", true);
        final String title = NbBundle.getMessage(SGLValues.class,
                "SGLValues.status.initError.title");
        NotificationDisplayer.getDefault().notify(title, ic, message, null, NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.WARNING);

    }

    private class Listener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final String prop = evt.getPropertyName();
            if (prop.equals(RemoteUnitsModel.PROP_INITIALISATION)) {
                RemoteUnitsModel.INITIALISATION before = (RemoteUnitsModel.INITIALISATION) evt.getOldValue();
                if (!before.satisfies(RemoteUnitsModel.INITIALISATION.STUDENTS)) {
                    support.getRP().post(SGLValues.this::reload);
                }
            } else if (prop.equals(RemoteUnitsModel.PROP_STUDENTS)) {
                support.getRP().post(SGLValues.this::reload);
            }
        }

    }

    @ServiceProvider(service = LookupProvider.class, path = "Loaders/application/betula-unit-data/Lookup")
    public static final class ReportsModelRegistration implements LookupProvider {

        @Override
        public Lookup createAdditionalLookup(Lookup base) {
            PrimaryUnitOpenSupport puos = base.lookup(PrimaryUnitOpenSupport.class);
            if (puos != null) {
                return Lookups.singleton(SGLValues.create(puos));
            }
            return Lookup.EMPTY;
        }
    }

}
