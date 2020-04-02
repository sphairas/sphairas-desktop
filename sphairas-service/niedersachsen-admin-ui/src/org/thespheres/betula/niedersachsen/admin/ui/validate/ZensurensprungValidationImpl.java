package org.thespheres.betula.niedersachsen.admin.ui.validate;

import org.thespheres.betula.validation.ui.ValidationProgressUI;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.admin.units.RemoteUnitsModel;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GradeTermTargetAssessment;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.validation.ValidationResultSet;
import org.thespheres.betula.validation.impl.ZensurensprungValidation;

/**
 *
 * @author boris.heithecker
 */
public class ZensurensprungValidationImpl extends ZensurensprungValidation<RemoteStudent, RemoteTargetAssessmentDocument, RemoteUnitsModel, ZensurensprungResultImpl> {

    //move to engine
    private final static int DELAY = 2000;
    private final static int PRIORITY = Thread.NORM_PRIORITY - 1;
    protected final RequestProcessor RP2 = new RequestProcessor(ZensurensprungValidationImpl.class);
    private final Map<DocumentId, DocumentWatch> watches = new HashMap<>();
    private final Listener listener = new Listener();
    private final RequestProcessor.Task task;

    @SuppressWarnings({"LeakingThisInConstructor"})
    ZensurensprungValidationImpl(final RemoteUnitsModel model, final Properties config) {
        super(model, config);
        model.addPropertyChangeListener(WeakListeners.propertyChange(listener, model));
        final ValidationResultSet.ValidationListener l = ValidationProgressUI.getDefault().createListener(this, () -> getDisplayName(model.getUnitOpenSupport().getNodeDelegate().getDisplayName()));
        addValidationListener(l);
        task = RP2.post(this::initAll, 0, PRIORITY);
    }

    @Override
    protected boolean cancel(ValidationListener<ZensurensprungResultImpl> cancelledBy) {
        return task.cancel();
    }

    @Override
    protected ZensurensprungResultImpl createResult(RemoteStudent student, TermId term, RemoteTargetAssessmentDocument doc, Grade before, Grade current) {
        return new ZensurensprungResultImpl(student, term, doc, before, current);
    }

    private void initAll() {
        assert RP2.isRequestProcessorThread();
        if (!model.getInitialization().satisfies(RemoteUnitsModel.INITIALISATION.MAXIMUM)) {
            return;
        }
        synchronized (watches) {
            watches.clear();
            model.getTargets().stream()
                    .forEach(rtad -> watches.computeIfAbsent(rtad.getDocumentId(), d -> createDocumentWatch(rtad)));  //NPE
        }
        //TODO: let engine do run
        run();
    }

    private void postRunOneDocument(RemoteTargetAssessmentDocument d, StudentId stud) {
        if (d != null) {
            RP2.post(() -> runOneDocument(d, stud));
        }
    }

    private DocumentWatch createDocumentWatch(RemoteTargetAssessmentDocument rtad) {
        final DocumentWatch ret = new DocumentWatch();
        rtad.addListener(WeakListeners.create(GradeTermTargetAssessment.Listener.class, ret, rtad));
        return ret;
    }

    private class Listener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final String n = evt.getPropertyName();
            if (RemoteUnitsModel.PROP_STUDENTS.equals(n) || RemoteUnitsModel.PROP_TARGETS.equals(n)) {
                task.schedule(DELAY);
            } //else RTAD Marker set?
            else if (RemoteUnitsModel.PROP_INITIALISATION.equals(evt.getPropertyName())) {
                task.schedule(DELAY);
            }
        }

    }

    private class DocumentWatch implements GradeTermTargetAssessment.Listener {

        @Override
        public void valueForStudentChanged(Object source, StudentId stud, TermId gradeId, Grade old, Grade newGrade, Timestamp timestamp) {
            RemoteTargetAssessmentDocument rtad = (RemoteTargetAssessmentDocument) source;
            postRunOneDocument(rtad, stud);
        }
    }
}
