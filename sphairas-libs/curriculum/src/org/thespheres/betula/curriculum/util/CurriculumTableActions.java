/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculum.util;

import com.google.common.eventbus.EventBus;
import java.beans.PropertyChangeEvent;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoableEditSupport;
import org.openide.awt.NotificationDisplayer;
import org.openide.loaders.XMLDataObject;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.curriculum.CourseEntry;
import org.thespheres.betula.curriculum.Curriculum;
import org.thespheres.betula.curriculum.Section;
import org.thespheres.betula.curriculum.DefaultCourseSelectionValue;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.ui.util.WriteLockCapability;
import org.thespheres.betula.services.ui.util.WriteLockCapability.WriteLock;
import org.thespheres.betula.services.ui.util.WriteLockException;
import org.thespheres.betula.ui.util.LogLevel;

/**
 *
 * @author boris.heithecker
 * @param <C>
 */
public abstract class CurriculumTableActions<C extends Curriculum> implements Lookup.Provider {

    public static final String PROP_CURRICULUM = "curriculum";
    protected final EventBus events = new EventBus();
    private C curriculum;
    private final Lookup lookup;
    protected final UndoableEditSupport undoSupport = new UndoableEditSupport(this);
    private final WriteLock[] lock = new WriteLock[]{null};

    protected CurriculumTableActions(C node, Lookup lookup) {
        this.curriculum = node;
        this.lookup = lookup;
    }

    public EventBus getEventBus() {
        return events;
    }

    public C getCurriculum() {
        return curriculum;
    }

    protected final void setCurriculum(final C curriculum) {
        final C old = this.curriculum;
        this.curriculum = curriculum;
        final PropertyChangeEvent evt = new PropertyChangeEvent(this, PROP_CURRICULUM, old, curriculum);
        events.post(evt);
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    public XMLDataObject getDataObject() {
        return getLookup().lookup(XMLDataObject.class);
    }

    public LocalProperties getLocalProperties() {
        return getLookup().lookup(LocalProperties.class);
    }

    protected void ensureDataLocked() throws WriteLockException {
        synchronized (lock) {
            if (lock[0] == null) {
                final WriteLockCapability wlc = getLookup().lookup(WriteLockCapability.class);
                if (wlc != null) {
                    lock[0] = wlc.writeLock();
                }
            }
        }
        if (lock[0] == null || !lock[0].isLockValid()) {
            throw new WriteLockException(getDataObject().getPrimaryFile().getPath());
        }
    }

    public abstract Node getNode(final String id);

    public boolean setNumLessons(CourseEntry course, Section section, DefaultCourseSelectionValue dcsv, Integer value) {
        try {
            ensureDataLocked();
        } catch (final WriteLockException ex) {
            notify(ex);
            return false;
        }
        Object ov = null;
        if (dcsv.getNumLessons() != null) {
            ov = dcsv.getNumLessons();
        } else if (dcsv.getOption() != null) {
            ov = dcsv.getOption();
        }
        dcsv.setNumLessons(value);
        if (!Objects.equals(ov, value)) {
            final CurriculumCourseSelectionChangeEvent evt = new CurriculumCourseSelectionChangeEvent(dcsv, "num", ov, value);
            getEventBus().post(evt);
            //            getUndoSupport().postEdit(new );
        }
        return true;
    }

    public boolean setOption(CourseEntry course, Section section, DefaultCourseSelectionValue dcsv, Marker value) {
        try {
            ensureDataLocked();
        } catch (final WriteLockException ex) {
            notify(ex);
            return false;
        }
        Object ov = null;
        if (dcsv.getNumLessons() != null) {
            ov = dcsv.getNumLessons();
        } else if (dcsv.getOption() != null) {
            ov = dcsv.getOption();
        }
        dcsv.setOption(value);
        if (!Objects.equals(ov, value)) {
            final CurriculumCourseSelectionChangeEvent evt = new CurriculumCourseSelectionChangeEvent(dcsv, "option", ov, value);
            getEventBus().post(evt);
            //            getUndoSupport().postEdit(new );
        }
        return true;
    }

    @Messages({
        "CurriculumTableActions.WriteLockException.error.title=Schreibschutz-Fehler"})
    protected void notify(final WriteLockException ex) {
        Logger.getLogger(CurriculumTableActions.class.getName()).log(LogLevel.INFO_WARNING, ex.getMessage());
        Icon ic = ImageUtilities.loadImageIcon("org/thespheres/betula/ui/resources/exclamation-red-frame.png", true);
        String title = NbBundle.getMessage(CurriculumTableActions.class, "CurriculumTableActions.WriteLockException.error.title");
        NotificationDisplayer.getDefault()
                .notify(title, ic, ex.getLocalizedMessage(), null, NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.WARNING);
    }

    public abstract void addCourse(CourseEntry course) throws IllegalArgumentException;

    public abstract void removeCourse(String id);

    public abstract void addSection(Section section) throws IllegalArgumentException;

    public abstract void removeSection(int base, int sequence);

    public void removeSection(int sequence) {
        removeSection(1, sequence);
    }

    public UndoableEditSupport getUndoSupport() {
        return undoSupport;
    }

    public synchronized void addUndoableEditListener(UndoableEditListener l) {
        undoSupport.addUndoableEditListener(l);
    }

    public synchronized void removeUndoableEditListener(UndoableEditListener l) {
        undoSupport.removeUndoableEditListener(l);
    }

}
