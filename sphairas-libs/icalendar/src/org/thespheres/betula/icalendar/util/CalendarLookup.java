/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.icalendar.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.thespheres.betula.icalendar.CalendarProvider;
import org.thespheres.ical.CalendarComponent;
import org.thespheres.ical.ICalendar;
import org.thespheres.ical.UID;

/**
 *
 * @author boris.heithecker
 */
public class CalendarLookup extends AbstractLookup {

    private final RequestProcessor RP;
    private final ComponentContent content;
    private final Map<CalendarProvider, ChangeListener> map = new HashMap<>();

    public CalendarLookup() {
        this(new RequestProcessor(CalendarLookup.class));
    }

    private CalendarLookup(RequestProcessor executor) {
        this(new ComponentContent(executor), executor);
    }

    private CalendarLookup(ComponentContent c, RequestProcessor executor) {
        super(c);
        this.content = c;
        this.RP = executor;
    }

    public RequestProcessor getExecutor() {
        return RP;
    }

    public void addProvider(final CalendarProvider provider) {
        final class Listener implements ChangeListener {

            @Override
            public synchronized void stateChanged(ChangeEvent e) {
                synchronized (content) {

                    final List<ICalendar> update = provider.getCalendars().stream()
                            .collect(Collectors.toList());

                    final List<CalendarPair> l = lookupResult(ICalendar.class).allItems().stream()
                            .map(CalendarPair.class::cast)
                            .filter(ci -> ci.provider == provider)
                            .collect(Collectors.groupingBy(ci -> ci.provider))
                            .getOrDefault(e, Collections.EMPTY_LIST);

                    l.stream()
                            .forEach(cp -> {
                                if (!update.remove(cp.calendar)) {
                                    content.removePair(cp);
                                } else {
                                    cp.update();
                                }
                            });

                    update.stream()
                            .map(cal -> new CalendarPair(provider, cal))
                            .forEach(content::add);
                }
            }
        }
        synchronized (content) {
            final ChangeListener l = map.computeIfAbsent(provider, cp -> new Listener());
            provider.addChangeListener(l);
            provider.getCalendars().stream()
                    .map(cal -> new CalendarPair(provider, cal))
                    .forEach(content::add);
        }
    }

    public Set<CalendarProvider> providers() {
        synchronized (content) {
            return map.keySet();
        }
    }

    public void clear() {
        synchronized (content) {
            lookupResult(ICalendar.class).allItems().stream()
                    .map(CalendarPair.class::cast)
                    .forEach(CalendarPair::remove);
        }
    }

    private final class CalendarPair extends Pair<ICalendar> {

        private final CalendarProvider provider;
        private final ICalendar calendar;

        @SuppressWarnings("LeakingThisInConstructor")
        private CalendarPair(CalendarProvider cp, ICalendar cal) {
            if (cp == null) {
                throw new NullPointerException();
            }
            provider = cp;
            calendar = cal;
//            provider.addChangeListener(this);
        }

        private synchronized void update() {
            synchronized (content) {
                Set<UID> update = getInstance().getComponents().stream()
                        .map(CalendarComponent::getUID)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toCollection(HashSet::new));

                lookupResult(CalendarComponent.class).allItems().stream()
                        .map(ComponentItem.class::cast)
                        .filter(ci -> ci.parent == this)
                        .filter(ci -> !update.remove(ci.cc.getUID()))
                        .forEach(content::removePair);

                getInstance().getComponents().stream()
                        .filter(cc -> cc.getUID() != null)
                        .filter(update::contains)
                        .map(cc -> new ComponentItem(cc, this))
                        .forEach(content::addPair);
            }
        }

        private void remove() {
//            provider.removeChangeListener(this);
            removeAll();
            content.removePair(this);
        }

        private void addAll() {
            getInstance().getComponents().stream()
                    .filter(cc -> cc.getUID() != null)
                    .map(cc -> new ComponentItem(cc, this))
                    .forEach(content::addPair);
        }

        private void removeAll() {
            lookupResult(CalendarComponent.class).allItems().stream()
                    .map(ComponentItem.class::cast)
                    .filter(ci -> ci.parent == this)
                    .forEach(content::removePair);
        }

        @Override
        protected boolean instanceOf(Class<?> c) {
            return c.isInstance(calendar);
        }

        @Override
        protected boolean creatorOf(Object obj) {
            return obj == null ? false : obj.equals(calendar);
        }

        @Override
        public ICalendar getInstance() {
            return calendar;
        }

        @Override
        public Class<? extends ICalendar> getType() {
            return calendar.getClass();
        }

        @Override
        public String getId() {
            return provider.getCalendarId().toString();
        }

        @Override
        public String getDisplayName() {
            return provider.getCalendarId().toString();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof CalendarPair) {
//                return obj.equals(((Listener) o).obj);
                return provider.getCalendarId().equals(((CalendarPair) o).provider.getCalendarId());
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return calendar.hashCode();
        }
    }

    private class ComponentItem extends Pair<CalendarComponent> {

        private final CalendarComponent cc;
        private CalendarPair parent;

        private ComponentItem(CalendarComponent obj, CalendarPair parent) {
            this.cc = obj;
            this.parent = parent;
        }

        @Override
        public boolean instanceOf(Class<?> c) {
            return c.isInstance(cc);
        }

        @Override
        public CalendarComponent getInstance() {
            return cc;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof ComponentItem) {
                return getId().equals(((ComponentItem) o).getId());
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return cc.hashCode();
        }

        @Override
        public String getId() {
            return cc.getUID().toString();
        }

        @Override
        public String getDisplayName() {
            return cc.toString();
        }

        @Override
        protected boolean creatorOf(Object obj) {
            return obj == null ? false : obj.equals(this.cc);
        }

        @Override
        public Class<? extends CalendarComponent> getType() {
            return (Class<? extends CalendarComponent>) cc.getClass();
        }
    }

    private static class ComponentContent extends Content {

        private ComponentContent(RequestProcessor rp) {
            super(rp);
        }

        private void add(CalendarPair l) {
            super.addPair(l);
            l.addAll();
        }
    }

}
