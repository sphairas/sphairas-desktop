/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminreports.impl;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import org.thespheres.betula.reports.model.*;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.undo.UndoableEdit;
import org.netbeans.api.editor.guards.InteriorSection;
import org.netbeans.editor.GuardedDocumentEvent;
import org.openide.awt.UndoRedo;
import org.openide.text.StableCompoundEdit;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.thespheres.betula.Student;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.Students;
import org.thespheres.betula.TermId;
import org.thespheres.betula.admin.units.AbstractTargetAssessmentDocument.TargetAssessmentDocumentCreationException;
import org.thespheres.betula.admin.units.SubmitResult;
import org.thespheres.betula.adminreports.editor.SaveReportAnnotation;
import org.thespheres.betula.adminreports.impl.RemoteTextTargetAssessmentDocument.Listener;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.Terms;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
@Messages({"EditableReport.createHeaderMessage.label=Bericht:\u0020"})
public class RemoteEditableReport extends EditableReport implements Listener { //implements ChangeListener {

    private final RemoteEditableReportCollection collection;
    private boolean modif = false;
//    private final ReportUndoRedo manager = new ReportUndoRedo();
    private final Listener listener = new Listener();
    private final SaveReportAnnotation saveAnnotation = new SaveReportAnnotation(this);
    private String message;
    private final RemoteTextTargetAssessmentDocument target;
    private final String provider;

    @SuppressWarnings("LeakingThisInConstructor")
    RemoteEditableReport(String provider, RemoteEditableReportCollection collection, Report report) throws TargetAssessmentDocumentCreationException {
        super(report);
        this.provider = provider;
        this.collection = collection;
        this.target = TextTargetAssessmentDocumentFactory.get(provider).getTargetAssessmentDocument(report.getDocument());
        this.target.addListener(this);
    }

    @Override
    public RemoteEditableReportCollection getCollection() {
        return collection;
    }

    public RemoteTextTargetAssessmentDocument getTargetDocument() {
        return target;
    }

    public DocumentId getDocument() {
        return report.getDocument();
    }

    public StudentId getStudent() {
        return report.getStudent();
    }

    public TermId getTerm() {
        return report.getTerm();
    }

    @Override
    public String getMessage() {
        if (message == null) {
            String prefix = NbBundle.getMessage(RemoteEditableReport.class, "EditableReport.createHeaderMessage.label");
            StringJoiner sj = new StringJoiner(", ", prefix, ""); //(" ", "", ReportsSectionsProvider.NB_ENDOFLINE);
            StudentId sid = report.getStudent();
            if (sid != null) {
                Students stud = getCollection().getContext().lookup(Students.class);//student name
                if (stud != null) {
                    Student s = stud.find(sid);
                    String n = s != null ? s.getFullName() : Long.toString(sid.getId());
                    sj.add(n);
                }
            }
            NamingResolver resolver = null;
            try {
                resolver = getCollection().getContext().lookup(RemoteReportsSupport.class).findNamingResolver();
            } catch (IOException ex) {
            }
            Term term = null;
            TermId tid = report.getTerm();
            if (tid != null) {
                term = Terms.forTermId(tid);
            }
            DocumentId did = report.getDocument();
            final Marker section = findSectionMarker();
            if (section != null) {
                sj.add(section.getLongLabel());
            } else if (did != null) {
                String docdn = null;
                if (resolver != null) {
                    try {
                        docdn = resolver.resolveDisplayNameResult(did).getResolvedName(term);
                    } catch (IllegalAuthorityException ex) {
                    }
                }
                if (docdn == null) {
                    docdn = did.getId();
                }
                sj.add(docdn);
            }
            if (term != null) {
                sj.add(term.getDisplayName());
            }
            message = sj.toString();
        }
        return message;
    }

    public void setSection(InteriorSection ss) {
        this.guarded = ss;

    }

    void notifyEditorReady() {
        addPropertyChangeListener(saveAnnotation);
        getCollection().getDocument().addDocumentListener(listener);
    }

    public void saveReport() throws IOException {
        final String text = guarded.getBody();
        final Marker section = findSectionMarker();
        final SubmitTextEdit submit = target.submit(report.getStudent(), report.getTerm(), section, text);
        if (submit != null) {
            class ModifiedListener implements TaskListener {

                @Override
                public void taskFinished(Task task) {
                    submit.task.removeTaskListener(this);
                    updateModif();
                }

                private void updateModif() {
                    if (!submit.task.isFinished()) {
                        submit.task.addTaskListener(this);
                        return;
                    }
                    if (SubmitResult.isOK(submit.getResult())) {
                        setModified(false);
                    }
                }
            }
            ModifiedListener ml = new ModifiedListener();
            ml.updateModif();
        }
    }

    public void removeReport() {
        final Marker section = findSectionMarker();
        final SubmitTextEdit submit = target.submit(report.getStudent(), report.getTerm(), section, null);
        if (submit != null) {
            class ModifiedListener implements TaskListener {

                @Override
                public void taskFinished(Task task) {
                    submit.task.removeTaskListener(this);
                    updateModif();
                }

                private void updateModif() {
                    if (!submit.task.isFinished()) {
                        submit.task.addTaskListener(this);
                        return;
                    }
                    if (SubmitResult.isOK(submit.getResult())) {
                        getCollection().removeReport(RemoteEditableReport.this);
                    }
                }
            }
            ModifiedListener ml = new ModifiedListener();
            ml.updateModif();
        }
    }

    public Marker findSectionMarker() {
        final String sp = report.getProperty(RemoteReportsModel.PROP_SECTION_CONVENTION);
        return report.getMarkers().stream()
                .filter(m -> sp != null && m.getConvention().equals(sp))
                .collect(CollectionUtil.singleOrNull());
    }

    public void stateChanged(ChangeEvent e) {
//        setModified(manager.canUndo());
    }

    public boolean isModified() {
        return modif;
    }

    private void setModified(boolean b) {
        boolean before = modif;
        modif = b;
        pSupport.firePropertyChange(PROP_MODIFIED, before, modif);
    }

    @Override
    public void valueForStudentChanged(StudentId source, TermId term, Marker section, String old, String value, Timestamp timestamp) {
        if (Objects.equals(getReport().getStudent(), source)
                && Objects.equals(getReport().getTerm(), term)
                && Objects.equals(findSectionMarker(), section)) {
            if ((old == null && value != null)) {

            } else if (old != null && value == null) {//Removed
                collection.removeReport(this);
            }
        }
    }

    void removeFromDocument() throws BadLocationException {
        final Position s = getStartPosition();
        final int len = getEndPosition().getOffset() - s.getOffset();
        guarded.removeSection();
        getCollection().getDocument().remove(s.getOffset(), len);
    }

    private class ReportUndoRedo extends UndoRedo.Manager {

        @Override
        public void undoableEditHappened(UndoableEditEvent evt) {
            UndoableEdit e = evt.getEdit();
            if (e instanceof StableCompoundEdit) {
                List<UndoableEdit> l = ((StableCompoundEdit) e).getEdits();
                UndoableEdit last = !l.isEmpty() ? l.get(l.size() - 1) : null;
                if (last instanceof GuardedDocumentEvent) {
                    GuardedDocumentEvent event = (GuardedDocumentEvent) last;
                    Position p;
                    try {
                        p = getCollection().getDocument().createPosition(event.getOffset());
                        if (guarded.contains(p, true));
                        super.undoableEditHappened(evt);
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
//                    if (getBodyStartPosition().getOffset() < offset && getBodyEndPosition().getOffset() >= offset) {
//                        super.undoableEditHappened(evt);
//                    }
                }
            } else if (e instanceof AbstractDocument.DefaultDocumentEvent) {
                AbstractDocument.DefaultDocumentEvent edit = (AbstractDocument.DefaultDocumentEvent) e;
                int offset = edit.getOffset();
                if (getBodyStartPosition().getOffset() < offset && getBodyEndPosition().getOffset() >= offset) {
                    super.undoableEditHappened(evt);
                }
            }
        }
    }

    private final class Listener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            update(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            update(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            update(e);
        }

        private void update(DocumentEvent e) {
            try {
                final Position change = e.getDocument().createPosition(e.getOffset());
                getCollection().RP.post(() -> processChange(change), 0, Thread.NORM_PRIORITY);
            } catch (BadLocationException ex) {
                Logger.getLogger(RemoteEditableReport.class.getCanonicalName()).log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
        }

        private void processChange(Position p) {
            if (guarded.contains(p, false)) {
                setModified(true);
            }
        }

    }
}
