/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.validate;

import com.google.common.eventbus.Subscribe;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.admin.units.RemoteUnitsModel;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GradeTermTargetAssessment;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.niedersachsen.admin.ui.RemoteReportsModel2;
import org.thespheres.betula.util.CollectionChangeEvent;
import org.thespheres.betula.validation.ValidationResult;
import org.thespheres.betula.validation.ValidationResultSet;
import org.thespheres.betula.validation.impl.AbstractValidationSet;
import org.thespheres.betula.validation.impl.ValidationRunException;
import org.thespheres.betula.validation.ui.ValidationProgressUI;

/**
 *
 * @author boris.heithecker
 */
class ReportsValidationDelegate<R extends ValidationResult, I extends AbstractValidationSet<RemoteReportsModel2, R, ?, ?> & RunReport> {

    private final static int DELAY = 1000;
    private final static int PRIORITY = Thread.NORM_PRIORITY;
    protected final RequestProcessor RP2 = new RequestProcessor(ReportsValidationDelegate.class);
    private final I delegate;
    private final Listener listener = new Listener();
    private final Map<DocumentId, DocumentWatch> watches = new HashMap<>();
    private final RemoteUnitsModel model;
    final RequestProcessor.Task task;

    @SuppressWarnings({"LeakingThisInConstructor"})
    ReportsValidationDelegate(final I delegate, final Node n) throws IOException {
        this.delegate = delegate;
        model = delegate.getModel().support.getRemoteUnitsModel();
        model.addPropertyChangeListener(WeakListeners.propertyChange(listener, model));
        delegate.getModel().getEventBus().register(this);
        final ValidationResultSet.ValidationListener l = ValidationProgressUI.getDefault().createListener(delegate, () -> delegate.getDisplayName(n.getDisplayName()));
        delegate.addValidationListener(l);
        task = RP2.post(this::initAll, 0, PRIORITY);
    }

    void reschedule() {
        task.schedule(DELAY);
    }

    private void initAll() {
        assert RP2.isRequestProcessorThread();
        if (!model.getInitialization().satisfies(RemoteUnitsModel.INITIALISATION.MAXIMUM)) {
            return;
        }
        synchronized (watches) {
            try {
                watches.clear();
                delegate.getModel().support.getRemoteUnitsModel().getTargets().stream()
                        .forEach(rtad -> watches.computeIfAbsent(rtad.getDocumentId(), d -> createDocumentWatch(rtad)));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return;
            }
        }
        delegate.run();
    }

    private DocumentWatch createDocumentWatch(RemoteTargetAssessmentDocument rtad) {
        final DocumentWatch ret = new DocumentWatch();
        rtad.addListener(WeakListeners.create(GradeTermTargetAssessment.Listener.class, ret, rtad));
        return ret;
    }

    private void postRunOneDocument(final TermId d, final StudentId stud, final int count) {
        if (d != null) {
            RP2.post(() -> {
                try {
                    delegate.runOneDocument(d, stud);
                } catch (ValidationRunException e) {
//            TODO: count
                    if (count < 3) {
                        postRunOneDocument(d, stud, count + 1);
                    } else {
                        e.setCount(count);
                        throw e;
                    }
                }
            });
        }
    }

    @Subscribe
    public void on(CollectionChangeEvent cce) {
        if (RemoteReportsModel2.COLLECTION_TERMS.equals(cce.getCollectionName())) {
            cce.getItemAs(TermId.class)
                    .ifPresent(t -> postRunOneTerm(t, 0));
        }
    }

    private void postRunOneTerm(final TermId d, final int count) {
        if (d != null) {
            RP2.post(() -> {
                try {
                    delegate.runOneTerm(d);
                } catch (ValidationRunException e) {
//            TODO: count
                    if (count < 3) {
                        postRunOneTerm(d, count + 1);
                    } else {
                        e.setCount(count);
                        throw e;
                    }
                }
            });
        }
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
        public void valueForStudentChanged(Object source, StudentId stud, TermId term, Grade old, Grade newGrade, Timestamp timestamp) {
//            RemoteTargetAssessmentDocument rtad = (RemoteTargetAssessmentDocument) source;
            postRunOneDocument(term, stud, 0);
        }
    }
}
