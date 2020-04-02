/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.zgnimpl.calendar;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import org.apache.http.client.utils.URIBuilder;
import org.netbeans.spi.project.LookupProvider;
import org.openide.awt.NotificationDisplayer;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.TermId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.icalendar.CalendarProvider;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.NoProviderException;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.ui.ConfigurationException;
import org.thespheres.betula.services.ui.util.CalendarHttpUtilities;
import org.thespheres.betula.services.ui.util.dav.URLs;
import org.thespheres.betula.services.util.ServiceConfiguration;
import org.thespheres.betula.ui.util.LogLevel;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.ical.CalendarComponent;
import org.thespheres.ical.CalendarComponentProperty;
import org.thespheres.ical.ICalendar;
import org.thespheres.ical.InvalidComponentException;
import org.thespheres.ical.UID;
import org.thespheres.ical.builder.ICalendarBuilder;
import org.thespheres.ical.util.IComponentUtilities;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"ZeugnisCalendarLookup2.warning.noCalendarUrl=No calendar URL for {0} found.",
    "ZeugnisCalendarLookup2.warning.multipleCalendars=Mehr als ein Kalender unter der Adresse {0} gefunden.",
    "ZeugnisCalendarLookup2.warning.noCalendar=Kein Kalender unter der Adresse {0} gefunden.",
    "ZeugnisCalendarLookup2.loadInitial.error.title=Fehler beim Laden des Zeugnis-Kalenders",
    "ZeugnisCalendarLookup2.loadInitial.error.message=Es ist ein Fehler beim Laden des Zeugnis-Kalenders aufgetreten (Type: {0}, Message: {1}",
    "ZeugnisCalendarLookup2.backupCalendar.multipleCalendars.message=Es wurde mehr als ein Kalender unter der Adresse {0} gefunden. Nur ein Kalender wird im Backup gespeichert."})
public class ZeugnisCalendarLookup2 implements CalendarProvider.Decorator {

    final PrimaryUnitOpenSupport support;
    private ICalendar calendar = ICalendar.EMPTY;
    private final Task initTask;
    private Task loadTask;
    private final ChangeSupport cSupport = new ChangeSupport(this);
    private WebProvider web;
    private final static RequestProcessor RP = new RequestProcessor(ZeugnisCalendarLookup2.class.getName(), 8);
    private URI uri;

    private ZeugnisCalendarLookup2(final PrimaryUnitOpenSupport uos) {
        support = uos;
        initTask = support.getRP().post(this::initialize);
    }

    @Override
    public Object getCalendarId() {
        return "zeugnis-kalender:" + support.getUnitId().getId();
    }

    @Override
    public List<ICalendar> getCalendars() {
        waitInitialized();
        final List<ICalendar> ret;
        synchronized (this) {
            ret = Collections.singletonList(calendar);
        }
        return ret;
    }

    private void waitInitialized() {
        if (!initTask.isFinished() || (loadTask != null && !loadTask.isFinished())) {
            try {
                initTask.waitFinished();
                final long maxWait = ServiceConfiguration.getInstance().getMaxWaitTimeInEDT();
                loadTask.waitFinished(maxWait);
            } catch (InterruptedException ex) {
                Logger.getLogger(ZeugnisCalendarLookup2.class.getName()).log(Level.WARNING, "Could not fetch calendar for {0}", support.getUnitId());
            }
        }
    }

    private void initialize() {
        LocalProperties prop;
        try {
            prop = support.findBetulaProjectProperties();
        } catch (IOException ex) {
            notifyException(ex);
            return;
        }
        final String prov = prop.getProperty("providerURL");
        String urlProp = null;
        try {
            urlProp = URLs.adminCalendarBase(prop);
        } catch (ConfigurationException ex) {
            notifyException(ex);
        }
        if (urlProp != null && prov != null) {
            try {
                final URIBuilder ub = new URIBuilder(urlProp + "reports/");
                ub.addParameter("unit.authority", support.getUnitId().getAuthority());
                ub.addParameter("unit.id", support.getUnitId().getId());
                uri = ub.build();
                web = WebProvider.find(prov, WebProvider.class);
                loadTask = RP.post(this::loadInitial);
            } catch (NoProviderException | URISyntaxException npex) {
                notifyException(npex);
            }
        }
    }

    private void loadInitial() {
        ICalendar cal = null;
        try {
            final List<ICalendar> l = CalendarHttpUtilities.parseCalendars(web, uri);
            if (l.isEmpty()) {
                final String msg = NbBundle.getMessage(ZeugnisCalendarLookup2.class, "ZeugnisCalendarLookup2.warning.noCalendar", uri);
                throw new IOException(msg);
            } else if (l.size() > 1) {
                final String msg = NbBundle.getMessage(ZeugnisCalendarLookup2.class, "ZeugnisCalendarLookup2.warning.multipleCalendars", uri);
                throw new IOException(msg);
            } else {
                cal = l.get(0);
            }
        } catch (ParseException | IOException | InvalidComponentException ex) {
            notifyException(ex);
        }
        synchronized (this) {
            if (cal != null) {
                calendar = cal;
            } else {
                calendar = ICalendar.EMPTY;
            }
        }
    }

    void createZeugnisEvent(final ICalendarBuilder cb) {
        if (web == null || uri == null) {
            Logger.getLogger(ZeugnisCalendarLookup2.class.getName()).log(LogLevel.INFO_WARNING, "No WebProvider set, no calendar url present.");
            return;
        }
        loadTask = RP.post(() -> createImpl(cb.toICalendar()));
        loadInitial();
        fireChange();
    }

    private void createImpl(final ICalendar uid) {
        try {
            CalendarHttpUtilities.putCalendar(web, uri, uid);
        } catch (IOException ex) {
            notifyException(ex);
        }
        loadInitial();
        fireChange();
    }

    void remove(final UID uid) {
        if (web == null || uri == null) {
            Logger.getLogger(ZeugnisCalendarLookup2.class.getName()).log(LogLevel.INFO_WARNING, "No WebProvider set, no calendar url present.");
            return;
        }
        loadTask = web.getDefaultRequestProcessor().post(() -> removeImpl(uid));
    }

    private void removeImpl(final UID uid) {
        try {
            CalendarHttpUtilities.deleteEvent(web, uri, uid);
        } catch (IOException ex) {
            notifyException(ex);
        }
        loadInitial();
        fireChange();
    }

    public Date findZeugnisDate(UnitId unitId, TermId term) {
        waitInitialized();
        final ICalendar cal;
        synchronized (this) {
            cal = calendar;
        }
        return cal.getComponents().stream()
                .filter(cc -> unitAndTermEquals(cc, unitId, term))
                .map(cc -> {
                    try {
                        return IComponentUtilities.parseDateProperty(cc, CalendarComponentProperty.DTSTART);
                    } catch (InvalidComponentException ex) {
                        throw new IllegalStateException(ex);
                    }
                })
                .collect(CollectionUtil.requireSingleOrNull());
    }

    private boolean unitAndTermEquals(final CalendarComponent cc, final UnitId unit, final TermId term) {
        final TermId tid = IComponentUtilities.parseTermId(cc);
        final UnitId uid = IComponentUtilities.parseUnitId(cc);
        return Objects.equals(unit, uid) && Objects.equals(term, tid);
    }

    static void notifyException(final Exception ex) {
        Logger.getLogger(ZeugnisCalendarLookup2.class.getName()).log(LogLevel.INFO_WARNING, ex.getMessage());
        Icon ic = ImageUtilities.loadImageIcon("org/thespheres/betula/ui/resources/exclamation-red-frame.png", true);
        String title = NbBundle.getMessage(ZeugnisCalendarLookup2.class, "ZeugnisCalendarLookup2.loadInitial.error.title");
        String message = NbBundle.getMessage(ZeugnisCalendarLookup2.class, "ZeugnisCalendarLookup2.loadInitial.error.message", ex.getClass().getName(), ex.getMessage());
        NotificationDisplayer.getDefault()
                .notify(title, ic, message, null, NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.WARNING);
    }

    @Override
    public Node decorate(final CalendarComponent component) {
        boolean ourComp = calendar.getComponents().stream()
                .map(CalendarComponent::getUID)
                .anyMatch(component.getUID()::equals);
        if (ourComp) {
            return new ZeugnisKonferenzNode2(component, this);
        }
        throw new IllegalArgumentException("Not our component.");
    }

    void fireChange() {
        cSupport.fireChange();
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        cSupport.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        cSupport.removeChangeListener(listener);
    }

    @ServiceProvider(service = LookupProvider.class, path = "Loaders/application/betula-unit-data/Lookup", position = 1000)
    public static class CalendarLookupProvider implements LookupProvider {

        @Override
        public Lookup createAdditionalLookup(final Lookup baseContext) {
            final PrimaryUnitOpenSupport uos = baseContext.lookup(PrimaryUnitOpenSupport.class);
            if (uos != null) {
                final ZeugnisCalendarLookup2 cp = new ZeugnisCalendarLookup2(uos);
                return Lookups.singleton(cp);
            } else {
                return Lookup.EMPTY;
            }
        }

    }
}
