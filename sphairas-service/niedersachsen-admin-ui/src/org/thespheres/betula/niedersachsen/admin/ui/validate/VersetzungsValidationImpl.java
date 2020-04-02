/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.validate;

import com.google.common.eventbus.Subscribe;
import org.thespheres.betula.validation.ui.ValidationProgressUI;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.admin.units.AbstractUnitOpenSupport;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.admin.units.RemoteUnitsModel;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GradeTermTargetAssessment;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.niedersachsen.admin.ui.RemoteReportsModel2;
import org.thespheres.betula.niedersachsen.admin.ui.ReportData2;
import org.thespheres.betula.ui.util.JAXBUtil;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.util.CollectionChangeEvent;
import org.thespheres.betula.validation.ValidationResultSet;
import org.thespheres.betula.validation.impl.CareerAwareGradeCondition;
import org.thespheres.betula.validation.impl.PolicyLegalHint;
import org.thespheres.betula.validation.impl.ValidationRunException;
import org.thespheres.betula.validation.impl.VersetzungsValidation;
import org.thespheres.betula.validation.impl.VersetzungsValidationConfiguration;

/**
 *
 * @author boris.heithecker
 */
public class VersetzungsValidationImpl extends VersetzungsValidation<ReportData2, RemoteReportsModel2, VersetzungsResultImpl> {

    //move to engine
    private final static int DELAY = 1000;
    private final static int PRIORITY = Thread.NORM_PRIORITY;
    protected final RequestProcessor RP2 = new RequestProcessor(VersetzungsValidationImpl.class);
    private final Map<DocumentId, DocumentWatch> watches = new HashMap<>();
    private final Listener listener = new Listener();
    final static Map<AbstractUnitOpenSupport, VersetzungsValidationImpl> CACHE = new WeakHashMap<>();
    private final RemoteUnitsModel unitsModel;
    private final RequestProcessor.Task task;

    @SuppressWarnings({"LeakingThisInConstructor"})
    private VersetzungsValidationImpl(RemoteReportsModel2 model, VersetzungsValidationConfiguration config) throws IOException {
        super(model, config);
        unitsModel = model.support.getRemoteUnitsModel();
        unitsModel.addPropertyChangeListener(WeakListeners.propertyChange(listener, unitsModel));
        getModel().getEventBus().register(this);
        final ValidationResultSet.ValidationListener l = ValidationProgressUI.getDefault().createListener(this, () -> getDisplayName(model.support.getNodeDelegate().getDisplayName()));
        addValidationListener(l);
        task = RP2.post(this::initAll, 0, PRIORITY);
    }

    public static VersetzungsValidationImpl create(RemoteReportsModel2 model) {
        final FileObject configFo = ValidationConfigUtilities.findLastConfigFile("/ValidationEngine/Configuration/org-thespheres-betula-niedersachsen-admin-ui-validate-VersetzungsValidation/");
        if (configFo != null) {
            final Class[] cl = JAXBUtil.lookupJAXBTypes("VersetzungsValidationConfiguration", VersetzungsValidationConfiguration.class, CareerAwareGradeCondition.class);
            try {
                final JAXBContext ctx = JAXBContext.newInstance(cl);
                final VersetzungsValidationConfiguration unmarshal = (VersetzungsValidationConfiguration) ctx.createUnmarshaller().unmarshal(configFo.getInputStream());
                return new VersetzungsValidationImpl(model, unmarshal);
            } catch (JAXBException | IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }

    @Override
    protected boolean cancel(ValidationListener<VersetzungsResultImpl> cancelledBy) {
        return task.cancel();
    }

    @Override
    protected VersetzungsResultImpl createResult(ReportData2 report, List<PolicyLegalHint> hints) {
        return new VersetzungsResultImpl(report.getRemoteStudent(), report, hints);
    }

    private void initAll() {
        assert RP2.isRequestProcessorThread();
        if (!unitsModel.getInitialization().satisfies(RemoteUnitsModel.INITIALISATION.MAXIMUM)) {
            return;
        }
        final Set<RemoteTargetAssessmentDocument> targets;
        try {
            targets = model.support.getRemoteUnitsModel().getTargets();
        } catch (IOException ex) {
            PlatformUtil.getCodeNameBaseLogger(VersetzungsValidationImpl.class).log(Level.WARNING, ex.getLocalizedMessage(), ex);
            return;
        }
        synchronized (watches) {
            targets.stream()
                    .forEach(rtad -> watches.computeIfAbsent(rtad.getDocumentId(), d -> createDocumentWatch(rtad)));
            final Set<DocumentId> remove = watches.keySet().stream()
                    .filter(d -> targets.stream().noneMatch(rtad -> rtad.getDocumentId().equals(d)))
                    .collect(Collectors.toSet());
            remove.forEach(watches::remove);
        }
        run();
    }

    private DocumentWatch createDocumentWatch(RemoteTargetAssessmentDocument rtad) {
        final DocumentWatch ret = new DocumentWatch();
        rtad.addListener(WeakListeners.create(GradeTermTargetAssessment.Listener.class, ret, rtad));
        return ret;
    }

    @Subscribe
    public void on(CollectionChangeEvent cce) {
        if (RemoteReportsModel2.COLLECTION_TERMS.equals(cce.getCollectionName())) {
            cce.getItemAs(TermId.class)
                    .ifPresent(t -> postRunOneTerm(t, 0));
        }
    }

    private void postRunOneTerm(final TermId term, final int count) {
        RP2.post(() -> runOneTerm(term, count));
    }

    private void runOneTerm(final TermId term, final int count) {
        model.getReportDocuments(term);
        final List<ReportData2> set = model.getReportDocuments(term).stream()
                .map(model::getReportDocument)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        try {
            fireStart(set.size());
            set.stream()
                    .forEach(this::processOneDocument);
        } catch (ValidationRunException e) {
//            TODO: count
            if (count < 3) {
                postRunOneTerm(term, count + 1);
            } else {
                e.setCount(count);
                throw e;
            }
        } finally {
            fireStop();
        }
    }

    private void postRunOneDocument(TermId d, StudentId stud, final int count) {
        RP2.post(() -> runOneDocument(d, stud, count));
    }

    private void runOneDocument(TermId term, StudentId stud, final int count) {
        try {
            fireStart(1);
            model.getReportDocuments(stud).stream()
                    .map(model::getReportDocument)
                    .filter(r -> r.getTerm().equals(term))
                    .forEach(this::processOneDocument);
        } catch (ValidationRunException e) {
//            TODO: count
            if (count < 3) {
                postRunOneDocument(term, stud, count + 1);
            } else {
                e.setCount(count);
                throw e;
            }
        } finally {
            fireStop();
        }
    }

    private class Listener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final String n = evt.getPropertyName();
            if (RemoteUnitsModel.PROP_STUDENTS.equals(n) || RemoteUnitsModel.PROP_TARGETS.equals(n)) {
                task.schedule(DELAY);
            } else if (RemoteUnitsModel.PROP_INITIALISATION.equals(n)) {
                task.schedule(DELAY);
            }
        }

    }

    private class DocumentWatch implements GradeTermTargetAssessment.Listener {

        @Override
        public void valueForStudentChanged(Object source, StudentId stud, TermId term, Grade old, Grade newGrade, Timestamp timestamp) {
            if (term != null && stud != null) {
                postRunOneDocument(term, stud, 0);
            }
        }
    }
}
