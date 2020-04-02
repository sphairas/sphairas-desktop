/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journalcouchdb.action;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.Action;
import javax.swing.Icon;
import org.ektorp.CouchDbConnector;
import org.ektorp.DbAccessException;
import org.netbeans.api.progress.*;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.StatusDisplayer;
import org.openide.loaders.DataObject;
import org.openide.util.Cancellable;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.thespheres.betula.Unit;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.couchdb.CouchDBProvider;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.journal.model.JournalEditor;
import org.thespheres.betula.journalcouchdb.model.TimeDoc2;
import org.thespheres.betula.journalcouchdb.model.TimeDoc2Support;
import org.thespheres.betula.services.scheme.spi.LessonId;
import org.thespheres.betula.ui.util.LogLevel;
import org.thespheres.ical.CalendarComponent;
import org.thespheres.ical.CalendarComponentProperty;
import org.thespheres.ical.ICalendar;
import org.thespheres.ical.InvalidComponentException;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"DeleteTask.ProgressHandle.name=Lösche Server-Einträge für {0}"})
class DeleteTask implements Runnable, Cancellable {

    private final Unit unit;
    private final JournalEditor editor;
    private final ICalendar iCalendar;
    private final CouchDBProvider couchdb;
    private long taskStart;
    private final ProgressHandle progress;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    @SuppressWarnings("LeakingThisInConstructor")
    DeleteTask(Unit unit, JournalEditor je, ICalendar ical, CouchDBProvider conn) {
        this.unit = unit;
        this.editor = je;
        this.iCalendar = ical;
        this.couchdb = conn;
        String name = NbBundle.getMessage(DeleteTask.class, "DeleteTask.ProgressHandle.name", findName());
        this.progress = ProgressHandleFactory.createHandle(name, this, (Action) null);
    }

    @Override
    public void run() {
        try {
            runImpl();
        } catch (IOException | DbAccessException ex) {
            notifyError(ex);
        }
    }

    private String findName() {
        DataObject dob = editor.getLookup().lookup(DataObject.class);
        if (dob != null) {
            return dob.getNodeDelegate().getDisplayName();
        }
        return null;
    }

    @NbBundle.Messages({"DeleteTask.error.title=Fehler beim Löschen",
        "DeleteTask.error.message=Beim Löschen von {0} (Gruppe: {1}) ist ein Fehler (Typ: {2}) aufgetreten (siehe sphairas-Log)."})
    private void notifyError(Exception ex) {
        progress.finish();
        Logger.getLogger(DeleteTask.class.getCanonicalName()).log(LogLevel.INFO_WARNING, ex.getMessage(), ex);
        Icon ic = ImageUtilities.loadImageIcon("org/thespheres/betula/ui/resources/exclamation-red-frame.png", true);
        String title = NbBundle.getMessage(DeleteTask.class, "DeleteTask.error.title");
        final String name = findName();
        String message = NbBundle.getMessage(DeleteTask.class, "DeleteTask.error.message", unit.getUnitId().getId(), name, ex.getClass().getSimpleName());
        NotificationDisplayer.getDefault()
                .notify(title, ic, message, null, NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.WARNING);
    }

    @NbBundle.Messages({"DeleteTask.success=Server-Einträge für {0} erfolgreich in {1}ms synchronisiert."})
    private void logSuccess() {
        long dur = System.currentTimeMillis() - taskStart;
        final String name = findName();
        final String msg = NbBundle.getMessage(DeleteTask.class, "DeleteTask.success", name, Long.toString(dur));
        Logger.getLogger(DeleteTask.class.getCanonicalName()).log(Level.INFO, msg);
        StatusDisplayer.getDefault().setStatusText(msg);
    }

    @NbBundle.Messages({"DeleteTask.cancelled=Löschen von Server-Einträgen für {0} abgebrochen."})
    private void logCancelled() {
        final String name = findName();
        final String msg = NbBundle.getMessage(DeleteTask.class, "DeleteTask.cancelled", name);
        Logger.getLogger(DeleteTask.class.getCanonicalName()).log(Level.INFO, msg);
        StatusDisplayer.getDefault().setStatusText(msg);
    }

    @Override
    public boolean cancel() {
        cancelled.set(true);
        return cancelled.get();
    }

    private void runImpl() throws IOException {

        progress.start();
        progress.switchToIndeterminate();
        taskStart = System.currentTimeMillis();

        final UnitId uid = unit.getUnitId();

        List<CalendarComponent> cc = iCalendar.getComponents().stream()
                .filter(get -> {
                    CalendarComponentProperty up = get.getAnyProperty("X-UNIT");
                    return up != null && new UnitId(up.getAnyParameter("x-authority").get(), up.getValue()).equals(uid);
                })
                .collect(Collectors.toList());

        if (cancelled.get()) {
            progress.finish();
            logCancelled();
            return;
        }

        CouchDbConnector connector = couchdb.getUserDatabase();

        if (cancelled.get()) {
            progress.finish();
            logCancelled();
            return;
        }

        final TimeDoc2Support support = new TimeDoc2Support(connector);

        if (cancelled.get()) {
            progress.finish();
            logCancelled();
            return;
        }

        final Set<DocumentId> targets = new HashSet<>();
        for (CalendarComponent comp : cc) {
            try {
                final LessonId lid = FastLessonsImpl3.extractLessonIdFromCalendarComponent(comp);
                final DocumentId targetId = new DocumentId(lid.getAuthority(), lid.getId(), DocumentId.Version.LATEST);
                targets.add(targetId);
            } catch (InvalidComponentException ex) {
                throw new IOException(ex);
            }
        }

        if (cancelled.get()) {
            progress.finish();
            logCancelled();
            return;
        }

        final Set<TimeDoc2> delete = targets.stream()
                .map(support::findByTarget)
                .flatMap(List::stream)
                .collect(Collectors.toSet());

        if (cancelled.get()) {
            progress.finish();
            logCancelled();
            return;
        }

        delete.stream()
                .forEach(support::remove);

        progress.finish();
        logSuccess();
    }

}
