/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport;

import com.google.common.eventbus.EventBus;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.event.EventListenerList;
import org.openide.nodes.Node;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.util.CollectionChangeEvent;

/**
 *
 * @author boris.heithecker
 */
public abstract class TermReport {

    public static final String PROP_ASSESSMENTS = "term-report-assessments";
    public static final String PROP_NOTES = "note";
    protected final EventListenerList listeners = new EventListenerList();
    protected final EventBus eventBus = new EventBus();
    protected final List<AssessmentProvider> assessments = new ArrayList<>();
    private Node node;

    public List<AssessmentProvider> getProviders() {
        return Collections.unmodifiableList(assessments);
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public Node getNodeDelegate() {
        if (node == null) {
            node = createNodeDelegate();
        }
        return node;
    }

    protected abstract Node createNodeDelegate();

    public abstract Map<StudentId, List<Note>> getNotes();

    public abstract boolean addNote(StudentId s, String text);

    public abstract void removeNotes(StudentId il);

    protected void fireAssessmentAdded(AssessmentProvider p) {
        final CollectionChangeEvent pce = new CollectionChangeEvent(this, TermReport.PROP_ASSESSMENTS, p, CollectionChangeEvent.Type.ADD);
        eventBus.post(pce);
    }

    protected void fireAssessmentRemoved(AssessmentProvider p) {
        final CollectionChangeEvent pce = new CollectionChangeEvent(this, TermReport.PROP_ASSESSMENTS, p, CollectionChangeEvent.Type.REMOVE);
        eventBus.post(pce);
    }

    protected void fireNoteAdded(Note n) {
        final CollectionChangeEvent pce = new CollectionChangeEvent(this, TermReport.PROP_NOTES, n, CollectionChangeEvent.Type.ADD);
        eventBus.post(pce);
    }

    protected void fireNoteRemoved(Note n) {
        final CollectionChangeEvent pce = new CollectionChangeEvent(this, TermReport.PROP_NOTES, n, CollectionChangeEvent.Type.REMOVE);
        eventBus.post(pce);
    }

    public static abstract class Note {

        public abstract String getText();

        public abstract Timestamp getTimestamp();

    }
}
