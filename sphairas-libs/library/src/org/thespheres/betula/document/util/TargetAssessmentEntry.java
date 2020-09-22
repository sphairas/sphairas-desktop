/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document.util;

import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.event.EventListenerList;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.thespheres.betula.Identity;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.TargetAssessment;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Entry;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.assess.IdentityTargetAssessment;

/**
 *
 * @author boris.heithecker
 * @param <I>
 */
@XmlRootElement(name = "betula-assessment-document", namespace = "http://www.thespheres.org/xsd/betula/container.xsd")
@XmlType(name = "targetAssessmentEntryType", namespace = "http://www.thespheres.org/xsd/betula/container.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class TargetAssessmentEntry<I extends Identity> extends AbstractTargetAssessmentEntry<I, StudentId> implements IdentityTargetAssessment<Grade, I, IdentityTargetAssessment.Listener<Grade, I>> {

    private static final long serialVersionUID = 1L;
    private transient EventListenerList listenerList;

    public TargetAssessmentEntry() {
    }

    public TargetAssessmentEntry(DocumentId id, Action action, boolean fragment) {
        this(action, id, new GenericXmlDocument(fragment));
    }

    @SuppressWarnings("OverridableMethodCallInConstructor")
    private TargetAssessmentEntry(Action action, DocumentId id, GenericXmlDocument doc) {
        super(action, id);
        setValue(doc);
    }

    @Override
    public Set<StudentId> students() {
        return getChildren().stream()
                .filter(Entry.class::isInstance)
                .map(Entry.class::cast)
                .flatMap(e -> e.getChildren().stream())
                .filter(Entry.class::isInstance)
                .map(Entry.class::cast)
                .map(Entry::getIdentity)
                .filter(StudentId.class::isInstance)
                .map(StudentId.class::cast)
                .collect(Collectors.toSet());
    }

    @Override
    public void addListener(IdentityTargetAssessment.Listener<Grade, I> listener) {
        getListenerList().add(TargetAssessment.Listener.class, listener);
    }

    @Override
    public void removeListener(IdentityTargetAssessment.Listener<Grade, I> listener) {
        getListenerList().remove(TargetAssessment.Listener.class, listener);
    }

    @Override
    protected void studentChanged(StudentId s, Identity id, Grade o, Grade n, Timestamp ts) {
        if (o == null || !o.equals(n)) { //TODO: temestamp !!!!
            // Guaranteed to return a non-null array
            final Object[] listeners = getListenerList().getListenerList();
            // Process the listeners last to first, notifying
            // those that are interested in this event
            for (int i = listeners.length - 2; i >= 0; i -= 2) {
                if (listeners[i] == TargetAssessment.Listener.class) {
                    ((TargetAssessment.Listener) listeners[i + 1]).valueForStudentChanged(this, s, o, n, ts);
                } else if (id != null && listeners[i] == IdentityTargetAssessment.Listener.class) {
                    ((IdentityTargetAssessment.Listener) listeners[i + 1]).valueForStudentChanged(this, s, id, o, n, ts);
                }
            }
        }
    }

    @Override
    protected void studentRemoved(StudentId s, Identity id) {
        // Guaranteed to return a non-null array
        final Object[] listeners = getListenerList().getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TargetAssessment.Listener.class) {
                ((TargetAssessment.Listener) listeners[i + 1]).studentRemoved(s);
            } else if (id != null && listeners[i] == IdentityTargetAssessment.Listener.class) {
                ((IdentityTargetAssessment.Listener) listeners[i + 1]).studentRemoved(s, id);
            }
        }
    }

    private EventListenerList getListenerList() {
        if (listenerList == null) {
            listenerList = new EventListenerList();
        }
        return listenerList;
    }

}
