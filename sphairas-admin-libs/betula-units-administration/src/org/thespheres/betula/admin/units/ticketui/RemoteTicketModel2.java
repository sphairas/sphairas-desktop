package org.thespheres.betula.admin.units.ticketui;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ChangeSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.Ticket;
import org.thespheres.betula.admin.units.RemoteTicket;
import org.thespheres.betula.services.jms.TicketEvent;
import org.thespheres.betula.services.client.jms.JMSListener;
import org.thespheres.betula.services.client.jms.JMSTopicListenerService;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.document.Envelope;
import org.thespheres.betula.document.util.DocumentUtilities;
import org.thespheres.betula.document.util.TicketEntry;
import org.thespheres.betula.services.LocalFileProperties;
import org.thespheres.betula.services.NoProviderException;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.jms.JMSTopic;
import org.thespheres.betula.services.ui.ConfigurationException;
import org.thespheres.betula.services.ui.util.CalendarHttpUtilities;
import org.thespheres.betula.services.ui.util.dav.URLs;
import org.thespheres.betula.services.ws.Paths;
import org.thespheres.betula.services.ws.ServiceException;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.ui.util.LogLevel;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.util.ContainerBuilder;
import org.thespheres.ical.CalendarComponent;
import org.thespheres.ical.ICalendar;
import org.thespheres.ical.InvalidComponentException;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"RemoteTicketModel2.status.retry=Enqueuing {0}. retry to load ticket from {1}.",
    "RemoteUnitsModel2.status.initError.title=Fehler beim Laden der Berechtigungen",
    "RemoteUnitsModel2.message.multipleCalendarComp=FEHLER: {0} Kalendereinträge!"})
class RemoteTicketModel2 {

    private final Listener listener = new Listener();
    private final static Map<String, RemoteTicketModel2> CACHE = new HashMap<>();
    private final Map<Ticket, RemoteTicket> cache2 = new HashMap<>();
    private final ChangeSupport cSupport = new ChangeSupport(this);
    private final WebServiceProvider service;
    private final String provider;
    private final int[] times = new int[]{1500, 4500, 13500};
    private Object jmsTickets;
    private String http;
    final RequestProcessor rp;

    @SuppressWarnings("LeakingThisInConstructor")
    private RemoteTicketModel2(String url) throws IOException {
        this.provider = url;
        service = WebProvider.find(url, WebServiceProvider.class);
        final LocalFileProperties lfp = LocalFileProperties.find(provider);
        if (lfp == null) {
            throw new IOException("No LocalFileProperties for provider " + provider);
        }
        String base = null;
        try {
            base = URLs.adminCalendarBase(lfp);
        } catch (ConfigurationException e) {
            throw new IOException(e);
        }
        http = base + "tickets/";
        rp = new RequestProcessor(RemoteTicketModel2.class.getName() + ":" + provider);
        rp.post(() -> initialize(0, null));
    }

    public String getProvider() {
        return provider;
    }

    public String getTicketsUrl() {
        return http;
    }

    static RemoteTicketModel2 get(String url) throws IOException {
        synchronized (CACHE) {
            RemoteTicketModel2 rtm = CACHE.get(url);
            if (rtm == null) {
                try {
                    rtm = new RemoteTicketModel2(url);
                } catch (NoProviderException npex) {
                    throw new IOException(npex);
                }
                CACHE.put(url, rtm);
            }
            return rtm;
        }
    }

    public RemoteTicket getTicket(Ticket ticket) {
        synchronized (cache2) {
            return cache2.get(ticket);
        }
    }

    public Collection<RemoteTicket> tickets() {
        synchronized (cache2) {
            return cache2.values();
        }
    }

    private void initialize(final int trial, final Ticket added) {
        boolean success = false;
        try {
            if (added == null) {
                success = doInit();
            } else {
                success = onAddImpl(added);
            }
        } catch (IOException ex) {
            PlatformUtil.getCodeNameBaseLogger(RemoteTicketModel2.class).log(Level.FINE, ex.getMessage(), ex);
            if (trial < 3) {
                final int next = trial + 1;
                final String msg = NbBundle.getMessage(RemoteTicketModel2.class, "RemoteTicketModel2.status.retry", Integer.toString(next), provider);
                PlatformUtil.getCodeNameBaseLogger(RemoteTicketModel2.class).log(Level.INFO, msg);
                rp.schedule(() -> initialize(trial + 1, null), times[trial], TimeUnit.MILLISECONDS);
            }
        }
        if (success && jmsTickets == null) {
            final LocalFileProperties lfp = LocalFileProperties.find(provider);
            final String jmsProp = lfp.getProperty("jms.providerURL", provider);
            try {
                final JMSTopicListenerService jms = JMSTopicListenerService.find(jmsProp, JMSTopic.TICKETS_TOPIC.getJmsResource());
                jmsTickets = jms;
                jms.registerListener(TicketEvent.class, listener);
            } catch (NoProviderException ex) {
                PlatformUtil.getCodeNameBaseLogger(RemoteTicketModel2.class).log(Level.INFO, ex.getLocalizedMessage());
                jmsTickets = ex;
            }
        }
    }

    private boolean doInit() throws IOException {
        final ContainerBuilder builder = new ContainerBuilder();
        builder.createTemplate(null, null, null, Paths.UNITS_TICKETS_PATH, null, Action.REQUEST_COMPLETION);
        Container ret = null;
        try {
            ret = service.createServicePort().solicit(builder.getContainer());
        } catch (ServiceException ex) {
            notifyError(ex, ex.getLocalizedMessage());
        }
        if (ret == null) {
            return false;
        }
        final List<Envelope> l = DocumentUtilities.findEnvelope(ret, Paths.UNITS_TICKETS_PATH);

        final Map<Ticket, RemoteTicket> entries = createRemoteTickets(l);

        synchronized (cache2) {
            cache2.clear();
            cache2.putAll(entries);
        }

        cSupport.fireChange();
        return true;
    }

    private Map<Ticket, RemoteTicket> createRemoteTickets(final List<Envelope> l) throws IOException {
        final Map<Ticket, RemoteTicket> entries = l.stream()
                .map(Envelope::getChildren)
                .flatMap(Collection::stream)
                .filter(TicketEntry.class::isInstance)
                .map(TicketEntry.class::cast)
                .collect(Collectors.toMap(TicketEntry::getIdentity, e -> new RemoteTicket(e.getIdentity(), e.getValue())));
        updateCalEntries(entries);
        return entries;
    }

    private void updateCalEntries(final Map<Ticket, RemoteTicket> entries) throws IOException {
        //        final TicketsCalendarBean calBean = remote.lookup(TicketsCalendarBean.class);
        String url = getTicketsUrl();
        List<ICalendar> il = null;
        try {
            il = CalendarHttpUtilities.parseCalendars(service, url);
        } catch (ParseException | InvalidComponentException ex) {
            notifyError(ex, ex.getLocalizedMessage());
        }
        final List<ICalendar> ical = il == null ? Collections.EMPTY_LIST : il;
        entries.forEach((t, rt) -> {
            final List<CalendarComponent> cmp = ical.stream()
                    .map(ICalendar::getComponents)
                    .flatMap(Collection::stream)
                    .filter(cc -> cc.getProperties("X-TICKET").stream()
                    .anyMatch(ccp -> ccp.getValue().equals(Long.toString(t.getId()))
                    && ccp.getAnyParameter("x-ticket-authority").map(t.getAuthority()::equals).orElse(false)))
                    .collect(Collectors.toList());
//                final UID comp = calBean.getSingleComponentForTicket(t);
//                final CalendarComponent component = calBean.getICalendar(new UID[]{comp}).getComponents().stream()
//                        .collect(CollectionUtil.requireSingleOrNull());
            if (cmp.size() == 1) {
                rt.setCalendar(cmp.get(0));
            } else if (cmp.size() > 1) {
                final String msg = NbBundle.getMessage(RemoteTicketModel2.class, "RemoteUnitsModel2.message.multipleCalendarComp", Integer.toString(cmp.size()));
                rt.setMessage(msg);
            }
        });
    }

    void removeTicket(final Ticket ticket) {
        rp.post(() -> {
            try {
                deleteTicket(service, ticket);
            } catch (IOException ex) {
                notifyError(ex, ex.getLocalizedMessage());
                return;
            }
            synchronized (cache2) {
                cache2.remove(ticket);
            }
            cSupport.fireChange();
        });
    }

    static void deleteTicket(final WebServiceProvider service, final Ticket ticket) throws IOException {
        final ContainerBuilder builder = new ContainerBuilder();
        final TicketEntry te = new TicketEntry(Action.ANNUL, ticket);
        builder.add(te, Paths.TICKETS_PATH);
        Container ret = null;
        try {
            ret = service.createServicePort().solicit(builder.getContainer());
        } catch (ServiceException ex) {
            throw new IOException(ex);
        }
        if (ret == null) {
            throw new IOException();
        }
        final List<Envelope> l = DocumentUtilities.findEnvelope(ret, Paths.TICKETS_PATH);
        final TicketEntry returned = l.stream()
                //                    .map(Envelope::getChildren)
                //                    .flatMap(Collection::stream)
                .filter(TicketEntry.class::isInstance)
                .map(TicketEntry.class::cast)
                .filter(e -> Action.CONFIRM.equals(e.getAction()))
                .filter(e -> e.getIdentity().equals(ticket))
                .collect(CollectionUtil.singleOrNull());
        if (returned == null) {
            throw new IOException();
        }
    }

    static void notifyError(Exception ex, String message) {
        PlatformUtil.getCodeNameBaseLogger(RemoteTicketModel2.class).log(LogLevel.INFO_WARNING, ex.getMessage(), ex);
        Icon ic = ImageUtilities.loadImageIcon("org/thespheres/betula/ui/resources/exclamation-red-frame.png", true);
        String title = NbBundle.getMessage(RemoteTicketModel2.class, "RemoteUnitsModel2.status.initError.title");
        NotificationDisplayer.getDefault()
                .notify(title, ic, message, null, NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.WARNING);
    }

    private void onTicketEvent(TicketEvent event) {
        final Ticket ticket = event.getSource();
        switch (event.getType()) {
            case REMOVE:
                onRemove(ticket);
                break;
            case ADD:
                rp.post(() -> initialize(0, ticket));
                break;
        }
    }

    private boolean onAddImpl(Ticket t) throws IOException {
        final ContainerBuilder builder = new ContainerBuilder();
        final TicketEntry te = new TicketEntry(Action.REQUEST_COMPLETION, t);
        builder.add(te, Paths.TICKETS_PATH);
        Container ret = null;
        try {
            ret = service.createServicePort().solicit(builder.getContainer());
        } catch (ServiceException ex) {
            notifyError(ex, ex.getLocalizedMessage());
        }
        if (ret != null) {
            final List<Envelope> l = DocumentUtilities.findEnvelope(ret, Paths.TICKETS_PATH);
            final Map<Ticket, RemoteTicket> entries = l.stream()
                    //                    .map(Envelope::getChildren)
                    //                    .flatMap(Collection::stream)
                    .filter(TicketEntry.class::isInstance)
                    .map(TicketEntry.class::cast)
                    .collect(Collectors.toMap(TicketEntry::getIdentity, e -> new RemoteTicket(e.getIdentity(), e.getValue())));
            updateCalEntries(entries);
            if (!entries.isEmpty()) {
                synchronized (cache2) {
                    cache2.putAll(entries);
                }
                cSupport.fireChange();
            }
            return true;
        }
        return false;
    }

    private void onRemove(Ticket t) {
        final RemoteTicket old;
        synchronized (cache2) {
            old = cache2.remove(t);
        }
        if (old != null) {
            cSupport.fireChange();
        }
    }

    void addChangeListener(ChangeListener listener) {
        cSupport.addChangeListener(listener);
    }

    void removeChangeListener(ChangeListener listener) {
        cSupport.removeChangeListener(listener);
    }

    private final class Listener implements JMSListener<TicketEvent> {

        @Override
        public void addNotify() {
        }

        @Override
        public void removeNotify() {
        }

        @Override
        public void onMessage(TicketEvent event) {
            onTicketEvent(event);
        }
    }

}
