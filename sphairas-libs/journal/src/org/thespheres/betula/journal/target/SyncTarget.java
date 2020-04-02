/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.target;

import com.google.common.eventbus.Subscribe;
import java.beans.PropertyChangeEvent;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.openide.loaders.DataObject;
import org.openide.util.WeakListeners;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.TargetAssessment;
import org.thespheres.betula.journal.model.EditableJournal;
import org.thespheres.betula.journal.model.EditableParticipant;
import org.thespheres.betula.journal.model.JournalEditor;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.journal.JournalConfiguration;
import org.thespheres.betula.util.CollectionChangeEvent;
import org.thespheres.betula.util.XmlTargetAssessment;

/**
 *
 * @author boris.heithecker
 */
final class SyncTarget implements TargetAssessment.Listener<Grade> {

    private WeakReference<XmlTargetAssessment> target;
    private final DataObject data;
    private static final boolean removeOrphanedEntries = true;

    @SuppressWarnings({"LeakingThisInConstructor"})
    private SyncTarget(XmlTargetAssessment target, DataObject dob) {
        this.target = new WeakReference<>(target);
        this.data = dob;
        TargetAssessment.Listener wl = WeakListeners.create(TargetAssessment.Listener.class, this, null);
        target.addListener(wl);
    }

    static void start(XmlTargetAssessment target, JournalEditor je) {
        final EditableJournal<?, ?> journal = je.getEditableJournal();
        final Grade dg = JournalConfiguration.getInstance().getDefaultGrade();
        journal.getEditableParticipants()
                .forEach(ep -> {
                    StudentId s = ep.getStudent().getStudentId();
                    if (target.select(s) == null) {
                        target.submit(s, dg, Timestamp.now());
                    }
                });
        final Set<StudentId> remove = target.students().stream()
                .filter(sid -> journal.findParticipant(sid) == null)
                .filter(sid -> Objects.equals(dg, target.select(sid)))
                .collect(Collectors.toSet());
        if (removeOrphanedEntries) {
            remove.stream()
                    .forEach(sid -> target.submit(sid, null, null));
        }
        final SyncTarget ret = new SyncTarget(target, je.getLookup().lookup(DataObject.class));
        journal.getEventBus().register(ret);
    }

    private Optional<XmlTargetAssessment> getTargetAssessment(EditableJournal ecal) {
        XmlTargetAssessment ta = target.get();
        if (ta == null) {
            target = null;
            ecal.getEventBus().unregister(this);
            return Optional.empty();
        }
        return Optional.of(ta);
    }

    @Subscribe
    public void onModelChange(final CollectionChangeEvent event) {
        if (EditableJournal.COLLECTION_PARTICIPANTS.equals(event.getCollectionName())) {
            event.getItemAs(EditableParticipant.class).ifPresent(student -> {
                final StudentId s = student.getStudent().getStudentId();
                final EditableJournal source = student.getEditableJournal();
                getTargetAssessment(source).ifPresent(ta -> {
                    switch (event.getType()) {
                        case ADD:
                            ta.submit(s, JournalConfiguration.getInstance().getDefaultGrade(), Timestamp.now());
                            break;
                        case REMOVE:
                            ta.submit(s, null, null);
                            break;
                    }
                });

            });
        }
    }

    @Override
    public void valueForStudentChanged(Object source, StudentId s, Grade old, Grade newGrade, Timestamp timestamp) {
        if (data != null && data.isValid()) {
            data.setModified(true);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        XmlTargetAssessment t = target.get();
        if (t != null
                && evt.getSource() == t
                && TargetAssessment.PROP_PREFERRED_CONVENTION.equals(evt.getPropertyName())
                && data != null && data.isValid()) {
            data.setModified(true);
        }
    }
}
