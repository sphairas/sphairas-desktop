/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.navigatorui;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.event.ChangeListener;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.WeakListeners;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.admin.units.AbstractUnitOpenSupport;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GradeTermTargetAssessment;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.services.scheme.spi.TermSchedule;
import org.thespheres.betula.services.WorkingDate;
import org.thespheres.betula.ui.util.IconAnnotator;
import org.thespheres.betula.ui.util.IconAnnotatorFactory;

/**
 *
 * @author boris.heithecker
 */
public class IconDecorator implements IconAnnotator {

    private final Set<RemoteTargetAssessmentDocument> context;
    private final TermSchedule schedule;
    private final GradeTermTargetAssessment.Listener listener = new Listener();
    private final ChangeSupport cSupport = new ChangeSupport(this);
    private final Boolean[] reset = new Boolean[]{true};
    private boolean allEmpty;
    private boolean allNoSignee;

    private IconDecorator(Lookup context) throws IOException {
        this.context = context.lookupAll(RemoteTargetAssessmentDocument.class).stream()
                .map(RemoteTargetAssessmentDocument.class::cast)
                .collect(Collectors.toSet());
        schedule = context.lookup(AbstractUnitOpenSupport.class).findTermSchedule();
        this.context.forEach(rtad -> rtad.addListener(WeakListeners.create(GradeTermTargetAssessment.Listener.class, listener, rtad)));
        this.context.forEach(rtad -> rtad.addPropertyChangeListener(WeakListeners.propertyChange(listener, rtad)));
        Lookup.getDefault().lookup(WorkingDate.class).addChangeListener(ce -> reset());
    }

    @Override
    public Image annotateIcon(Image original, boolean openedNode) {
        return original;
    }

    @Override
    public String annotateHtml(String originalDisplayName, String originalHtml) {
        synchronized (reset) {
            if (reset[0]) {
                final Date wd = Lookup.getDefault().lookup(WorkingDate.class).getCurrentWorkingDate();
                final TermId current = schedule.getTerm(wd).getScheduledItemId();
                allEmpty = context.stream()
                        .allMatch(d -> !d.identities().contains(current));
                allNoSignee = context.stream()
                        .allMatch(d -> d.getSignees().isEmpty());
            }
            reset[0] = false;
        }
        String ret = originalHtml;
        if (allEmpty) {
            ret = "<s>" + (ret != null ? ret : originalDisplayName) + "</s>";
        }
        if (allNoSignee) {
            ret = "<i>" + (ret != null ? ret : originalDisplayName) + "</i>";
        }
        return ret;
    }

    private void reset() {
        synchronized (reset) {
            reset[0] = true;
        }
        Mutex.EVENT.writeAccess(cSupport::fireChange);
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        cSupport.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        cSupport.addChangeListener(listener);
    }

    private class Listener implements GradeTermTargetAssessment.Listener, PropertyChangeListener {

        @Override
        public void valueForStudentChanged(Object source, StudentId student, TermId gradeId, Grade old, Grade newGrade, Timestamp timestamp) {
            reset();
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (RemoteTargetAssessmentDocument.PROP_SIGNEES.equals(evt.getPropertyName())) {
                reset();
            }
        }

    }

    @MimeRegistration(service = IconAnnotatorFactory.class, mimeType = "application/betula-remote-target-assessment-document")
    public static class Factory implements IconAnnotatorFactory {

        @Override
        public IconAnnotator createIconAnnotator(Lookup context) {
            final AbstractUnitOpenSupport uos = context.lookup(AbstractUnitOpenSupport.class);
            if (uos != null) {
                try {
                    return new IconDecorator(context);
                } catch (IOException ex) {
                }
            }
            return null;
        }

    }
}
