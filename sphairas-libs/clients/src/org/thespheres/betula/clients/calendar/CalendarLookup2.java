/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.clients.calendar;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import org.netbeans.spi.project.LookupProvider;
import org.openide.awt.NotificationDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.ical.builder.ICalendarBuilder;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.icalendar.util.CalendarLookup;
import org.thespheres.betula.icalendar.util.CalendarUtilities;
import org.thespheres.betula.project.BetulaProject;
import org.thespheres.betula.services.LocalFileProperties;
import org.thespheres.betula.services.NoProviderException;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.ui.util.CalendarHttpUtilities;
import org.thespheres.betula.ui.util.LogLevel;
import org.thespheres.ical.ICalendar;
import org.thespheres.ical.InvalidComponentException;

/**
 *
 * @author boris.heithecker
 */
class CalendarLookup2 {

    public static final String CALENDAR_FILE = "calendar";
    public static final String CALENDAR_BACKUP_PATH = BetulaProject.BACKUP_PATH + "/" + CALENDAR_FILE + ".ics";

//    private final InstanceContent content = new InstanceContent();
//    private final Lookup lookup = new AbstractLookup(content);
    private final CalendarLookup calLookup = new CalendarLookup();
    private final Map<DocumentId, CalendarProviderImpl> calProv = new HashMap<>();
    private final String url;
//    private final Task initialTask;
    private final Lookup contextLkp;
    private final WebProvider wp;

    @Messages({"CalendarLookup2.warning.noCalendarUrl=No calendar URL for {0} found.",
        "CalendarLookup2.warning.noCalendar=Kein Kalender unter der Adresse {0} gefunden.",
        "CalendarLookup2.warning.noBackupPath=Kein Backup-Pfad.",
        "CalendarLookup2.loadInitial.error.title=Fehler beim Laden des Kalenders",
        "CalendarLookup2.loadInitial.error.message=Es ist ein Fehler beim Laden des Kalenders aufgetreten (Type: {0}, Message: {1}",
        "CalendarLookup2.loadBackup.error.title=Fehler beim Laden des Kalendar-Backup",
        "CalendarLookup2.warning.nonUniqueCalendarInBackupFile=In der Kalender-Backup-Datei befinden sich {0} Kalender. Erwartet wird ein Kalender.",
        "CalendarLookup2.loadBackup.error.message=Es ist ein Fehler beim Laden des Kalendar-Backup aufgetreten (Type: {0}, Message: {1}",
        "CalendarLookup2.backupCalendar.multipleCalendars.title=Mehrere Kalender",
        "CalendarLookup2.backupCalendar.multipleCalendars.message=Es wurde mehr als ein Kalender unter der Adresse {0} gefunden. Nur ein Kalender wird im Backup gespeichert."})
    @SuppressWarnings("LeakingThisInConstructor")
    private CalendarLookup2(String url, Lookup context, WebProvider web) {
        this.url = url;
        this.contextLkp = context;
        this.wp = web;
        wp.getDefaultRequestProcessor().post(this::loadInitial);
    }

    private void addCalendar(ICalendar found) {
        DocumentId did = CalendarUtilities.extractCalendarIdFromICalendar(found);
        CalendarProviderImpl p = calProv.computeIfAbsent(did, d -> {
            CalendarProviderImpl cpi = new CalendarProviderImpl(d);
            calLookup.addProvider(cpi);
            return cpi;
        });
        p.addCalendar(found);
    }

    private void loadInitial() {
        List<ICalendar> l;
        try {
            l = CalendarHttpUtilities.parseCalendars(wp, url);
            if (l.isEmpty()) {
                String msg = NbBundle.getMessage(CalendarLookup2.class, "CalendarLookup2.warning.noCalendar", url);
                Logger.getLogger(CalendarLookup2.class.getName()).log(Level.WARNING, msg);
            } else {
//                l.forEach(content::add);
                l.forEach(this::addCalendar);
                wp.getDefaultRequestProcessor().post(() -> backupCalendar(l));
                return;
            }
        } catch (ParseException | IOException | InvalidComponentException ex) {
            Logger.getLogger(CalendarLookup2.class.getName()).log(LogLevel.INFO_WARNING, ex.getMessage());
            Icon ic = ImageUtilities.loadImageIcon("org/thespheres/betula/ui/resources/exclamation-red-frame.png", true);
            String title = NbBundle.getMessage(CalendarLookup2.class, "CalendarLookup2.loadInitial.error.title");
            String message = NbBundle.getMessage(CalendarLookup2.class, "CalendarLookup2.loadInitial.error.message", ex.getClass().getName(), ex.getMessage());
            NotificationDisplayer.getDefault()
                    .notify(title, ic, message, null, NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.WARNING);
        }
        try {
            ICalendar backup = loadBackup();
//            content.add(backup);
            addCalendar(backup);
        } catch (IOException ex) {
            Logger.getLogger(CalendarLookup2.class.getName()).log(LogLevel.INFO_WARNING, ex.getMessage());
            Icon ic = ImageUtilities.loadImageIcon("org/thespheres/betula/ui/resources/exclamation-red-frame.png", true);
            String title = NbBundle.getMessage(CalendarLookup2.class, "CalendarLookup2.loadBackup.error.title");
            String message = NbBundle.getMessage(CalendarLookup2.class, "CalendarLookup2.loadBackup.error.message", ex.getClass().getName(), ex.getMessage());
            NotificationDisplayer.getDefault()
                    .notify(title, ic, message, null, NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.WARNING);
        }
    }

    private ICalendar loadBackup() throws IOException {
        BetulaProject prj = contextLkp.lookup(BetulaProject.class);
        if (prj != null) {
            Path path = prj.getBackupDir();
            FileObject fo = FileUtil.toFileObject(path.toFile());//prj.getProjectDirectory().getFileObject(BetulaProject.BACKUP_PATH);
            if (fo != null) {
                fo = fo.getFileObject(CALENDAR_FILE, "ics");
                if (fo != null) {
                    try {
                        List<ICalendar> l = ICalendarBuilder.parseCalendars(fo.toURL());
                        if (l.size() == 1) {
                            return l.get(0);
                        } else {
                            String msg = NbBundle.getMessage(CalendarLookup2.class, "CalendarLookup2.warning.nonUniqueCalendarInBackupFile", l.size());
                            throw new IOException(msg);
                        }
                    } catch (ParseException | InvalidComponentException ex) {
                        throw new IOException(ex);
                    }
                }
            }
        }
        String msg = NbBundle.getMessage(CalendarLookup2.class, "CalendarLookup2.warning.noBackupPath");
        Logger.getLogger(CalendarLookup2.class.getName()).log(Level.WARNING, msg);
        throw new IOException(msg);
    }

    private void backupCalendar(List<ICalendar> l) {
        if (l.size() > 1) {
            Icon ic = ImageUtilities.loadImageIcon("org/thespheres/betula/ui/resources/exclamation--frame.png", true);
            String title = NbBundle.getMessage(CalendarLookup2.class, "CalendarLookup2.backupCalendar.multipleCalendars.title");
            String message = NbBundle.getMessage(CalendarLookup2.class, "CalendarLookup2.v.message", url);
            NotificationDisplayer.getDefault()
                    .notify(title, ic, message, null, NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.WARNING);
        }
        ICalendar backup = l.get(0);
        BetulaProject betula = contextLkp.lookup(BetulaProject.class);
        if (betula != null && backup != null) {
            try {
//                Path path = Paths.get(betula.getProjectDirectory().getPath(), BetulaProject.BACKUP_PATH, CALENDAR_FILE + ".ics");
                Path path = betula.getBackupDir().resolve(CALENDAR_FILE + ".ics");
                try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                    writer.write(backup.toString());
                }
            } catch (IOException ex) {
                String msg = NbBundle.getMessage(CalendarLookup2.class, "CalendarLookup2.warning.noBackupPath");
                Logger.getLogger(CalendarLookup2.class.getName()).log(Level.WARNING, msg);
            }
        }
    }

    @LookupProvider.Registration(projectType = {"org-thespheres-betula-project-local"})
    public static class Reg implements LookupProvider {

        @Override
        public Lookup createAdditionalLookup(Lookup baseContext) {
//        Project prj = baseContext.lookup(Project.class);
            LocalFileProperties prop = baseContext.lookup(LocalFileProperties.class);
            String prov = prop.getProperty("providerURL");
            String urlProp = prop.getProperty("calendarUrl");
            Exception ex = null;
            if (urlProp != null && prov != null) {
                try {
                    WebProvider web = WebProvider.find(prov, WebProvider.class);
                    CalendarLookup2 lkp = new CalendarLookup2(urlProp, baseContext, web);
                    return lkp.calLookup; //.lookup;
                } catch (NoProviderException npex) {
                    ex = npex;
                }
            }
            String msg = NbBundle.getMessage(CalendarLookup2.class, "CalendarLookup2.warning.noCalendarUrl");
            Logger.getLogger(CalendarLookup2.class.getName()).log(Level.CONFIG, msg, ex);
            return Lookup.EMPTY;
        }
    }

}
