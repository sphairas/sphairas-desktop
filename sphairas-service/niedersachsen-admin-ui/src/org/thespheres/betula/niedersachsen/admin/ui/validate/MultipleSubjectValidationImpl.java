/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.validate;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.admin.units.RemoteUnitsModel;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GradeTermTargetAssessment;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.document.model.MultiSubject;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.scheme.spi.TermNotFoundException;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.validation.ValidationResultSet;
import org.thespheres.betula.validation.ui.ValidationProgressUI;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"MultipleSubjectValidationImpl.MultipleSubjectEntry.message=Liste {0} enthält einen Mehrfacheintrag für {1} in {2}"})
class MultipleSubjectValidationImpl extends MultipleSubjectValidation<RemoteStudent, RemoteTargetAssessmentDocument, RemoteUnitsModel> {

    private final PrimaryUnitOpenSupport support;
    private final String[] conventions;
    //move to engine
    private final static int DELAY = 2000;
    private final static int PRIORITY = Thread.NORM_PRIORITY - 1;
    protected final RequestProcessor RP2 = new RequestProcessor(ZensurensprungValidationImpl.class);
    private final Map<DocumentId, DocumentWatch> watches = new HashMap<>();
    private final Listener listener = new Listener();
    protected final RequestProcessor.Task task;

    @SuppressWarnings({"LeakingThisInConstructor"})
    MultipleSubjectValidationImpl(PrimaryUnitOpenSupport support, RemoteUnitsModel model, Properties config) {
        super(model, config);
        this.support = support;
        this.conventions = uniqueMarkerConventions(support);
        final ValidationResultSet.ValidationListener l = ValidationProgressUI.getDefault().createListener(this, () -> getDisplayName(support.getNodeDelegate().getDisplayName()));
        addValidationListener(l);
        task = RP2.post(this::initAll, 0, PRIORITY);
        model.addPropertyChangeListener(WeakListeners.propertyChange(listener, model));
    }

    @Override
    protected MultiSubject findSubject(final RemoteTargetAssessmentDocument rtad) {
        final MultiSubject ret = rtad.getMultiSubject();
        if (ret != null && conventions != null && ret.getRealmMarker() != null) {
            final String rc = ret.getRealmMarker().getConvention();
            if (!Arrays.stream(conventions).anyMatch(rc::equals)) {
                return new MultiSubject(null, ret.getSubjectMarkerSet());
            }
        }
        return ret;
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

    private void postRunOneStudentChange(final RemoteTargetAssessmentDocument d, final StudentId stud, final TermId term) {
        final Set<RemoteTargetAssessmentDocument> set = model.getTargets().stream()
                .filter(rtad -> rtad.getDocumentId().equals(d.getDocumentId()) || rtad.select(stud, term) != null)
                .collect(Collectors.toSet());
        if (!set.isEmpty()) {
            RP2.post(() -> {
                fireStart(set.size());
                try {
                    set.forEach(rtad -> processOneDocument(rtad));
                } finally {
                    fireStop();
                }
            });
        }
    }

    private DocumentWatch createDocumentWatch(RemoteTargetAssessmentDocument rtad) {
        final DocumentWatch ret = new DocumentWatch();
        rtad.addListener(WeakListeners.create(GradeTermTargetAssessment.Listener.class, ret, rtad));
        return ret;
    }

    @Override
    protected MultipleSubjectEntry createResult(RemoteTargetAssessmentDocument target, RemoteStudent student, TermId term) {
        Term t = null;
        try {
            t = getModel().getUnitOpenSupport().findTermSchedule().resolve(term);
        } catch (IOException | TermNotFoundException | IllegalAuthorityException ex) {
            PlatformUtil.getCodeNameBaseLogger(MultipleSubjectValidationImpl.class).log(Level.WARNING, ex.getLocalizedMessage(), ex);
        }
        final String n = target.getName().getDisplayName(t);
        final String s = student.getFullName();
        final String tn = t == null ? Integer.toString(term.getId()) : t.getDisplayName();
        final String msg = NbBundle.getMessage(MultipleSubjectValidationImpl.class, "MultipleSubjectValidationImpl.MultipleSubjectEntry.message", n, s, tn);
        return new MultipleSubjectEntryImpl(target.getDocumentId(), student.getStudentId(), term, msg);
    }

    private class MultipleSubjectEntryImpl extends MultipleSubjectEntry {

        @SuppressWarnings({"OverridableMethodCallInConstructor"})
        private MultipleSubjectEntryImpl(DocumentId documentId, StudentId studentId, TermId termId, String msg) {
            super(documentId, studentId, termId);
            setMessage(msg);
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
            final RemoteTargetAssessmentDocument rtad = (RemoteTargetAssessmentDocument) source;
            if (rtad != null) {
                postRunOneStudentChange(rtad, stud, term);
            }
        }
    }

    static String[] uniqueMarkerConventions(final PrimaryUnitOpenSupport support) {
        final String prop;
        try {
            prop = support.findBetulaProjectProperties().getProperty("unique.realm.conventions", null);
        } catch (IOException ex) {
            PlatformUtil.getCodeNameBaseLogger(MultipleSubjectValidationImpl.class).log(Level.FINE, ex.getLocalizedMessage(), ex);
            return null;
        }
        if (prop == null) {
            return null;
        }
        return prop.split(",");
    }

}
