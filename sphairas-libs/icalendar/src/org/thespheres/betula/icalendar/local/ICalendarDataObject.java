/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.icalendar.local;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.text.ParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.netbeans.spi.actions.AbstractSavable;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ProxyLookup;
import org.thespheres.betula.icalendar.CalendarBuilderProvider;
import org.thespheres.betula.icalendar.util.CalendarLookup;
import org.thespheres.ical.builder.ICalendarBuilder;
import org.thespheres.betula.icalendar.CalendarProvider;
import org.thespheres.ical.ICalendar;
import org.thespheres.ical.InvalidComponentException;

@DataObject.Registration(displayName = "#ICalendarDataObject.displayName", iconBase = "org/thespheres/betula/icalendar/resources/calendar.png", mimeType = "text/calendar")
@MIMEResolver.ExtensionRegistration(displayName = "#ICalendarDataObject.displayName", extension = "ics", mimeType = "text/calendar")
@NbBundle.Messages("ICalendarDataObject.displayName=iCalendar")
public class ICalendarDataObject extends MultiDataObject {

    private final Lookup lookup;
    final CalendarLookup calendars = new CalendarLookup();
    private final Saver saver = new Saver();
    private final FSListener listener = new FSListener();

    @SuppressWarnings({"LeakingThisInConstructor", "OverridableMethodCallInConstructor"})
    public ICalendarDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        getCookieSet().assign(SaveCookie.class, saver);
        lookup = new ProxyLookup(getCookieSet().getLookup(), calendars);
        readCalendars();
        addPropertyChangeListener(listener);
        getPrimaryFile().addFileChangeListener(listener);
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    private synchronized void readCalendars() throws IOException {
        URL url = getPrimaryFile().toURL();
        try {
            final List<ICalendarBuilder> parsed = ICalendarBuilder.parseCalendarsToBuilder(url);
            final DOCalendarProvider p = new DOCalendarProvider(parsed, this);
            calendars.addProvider(p);
            getCookieSet().assign(CalendarBuilderProvider.class, p);
        } catch (ParseException | InvalidComponentException ex) {
            throw new IOException(ex);
        }
    }

    @Messages({"# {0} - The file.",
        "ICalendarDataObject.error.updateCalendar=Could not read calendar from file {0}."})
    private void updateCalendar() {
        calendars.clear();
        try {
            readCalendars();
        } catch (IOException ex) {
            String message = NbBundle.getMessage(ICalendarDataObject.class, "ICalendarDataObject.error.updateCalendar", getPrimaryFile().getPath());
            Logger.getLogger(ICalendarDataObject.class.getCanonicalName()).log(Level.WARNING, message);
        }
    }

    @Override
    public void setModified(boolean modif) {
        super.setModified(modif);
        if (modif) {
            saver.registerSavable();
        } else {
            saver.unregisterSavable();
        }
    }

    private final class FSListener extends FileChangeAdapter implements PropertyChangeListener, AtomicAction {

        @Override
        public void fileChanged(FileEvent fe) {
            if (!fe.firedFrom(this)) {
                fe.runWhenDeliveryOver(ICalendarDataObject.this::updateCalendar);
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (DataObject.PROP_PRIMARY_FILE.equals(evt.getPropertyName())) {
                if (evt.getOldValue() instanceof FileObject) {
                    ((FileObject) evt.getOldValue()).removeFileChangeListener(this);
                }
                getPrimaryFile().addFileChangeListener(this);
                updateCalendar();
            }
        }

        @Override
        public void run() throws IOException {
            final List<ICalendar> l = calendars.providers().stream()
                    .map(CalendarProvider::getCalendars)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
//            Path path = FileUtil.toFile(getPrimaryFile()).toPath();
            OutputStream os = null;
            FileLock lock = null;
            try {
                lock = getPrimaryFile().lock();
                os = getPrimaryFile().getOutputStream(lock);
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os))) {
                    for (ICalendar cal : l) {
                        writer.write(cal.toString());
                    }
                }
            } finally {
                if (os != null) {
                    os.close();
                }
                if (lock != null) {
                    lock.releaseLock();
                }
                setModified(false);
            }
            //don't use -> no atomic action
//            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
//                for (ICalendar cal : l) {
//                    writer.write(cal.toString());
//                }
//            }
        }
    }

    private class Saver extends AbstractSavable {

        @Override
        protected String findDisplayName() {
            return ICalendarDataObject.this.getPrimaryFile().getNameExt();
        }

        @Override
        protected void handleSave() throws IOException {
            ICalendarDataObject.this.getPrimaryFile().getFileSystem().runAtomicAction(listener);
        }

        private void registerSavable() {
            register();
//            getCookieSet().assign(SaveCookie.class, this);
        }

        private void unregisterSavable() {
            unregister();
//            getCookieSet().assign(SaveCookie.class);
        }

        private DataObject getDataObject() {
            return ICalendarDataObject.this;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof Saver) {
                Saver dos = (Saver) other;
                return getDataObject().equals(dos.getDataObject());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return getDataObject().hashCode();
        }
    }
}
