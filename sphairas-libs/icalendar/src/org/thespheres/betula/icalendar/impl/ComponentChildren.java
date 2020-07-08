/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.icalendar.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.thespheres.betula.icalendar.CalendarProvider;
import org.thespheres.betula.icalendar.impl.ComponentChildren.Key;
import org.thespheres.ical.CalendarComponent;
import org.thespheres.ical.CalendarComponentProperty;
import org.thespheres.ical.ICalendar;
import org.thespheres.ical.InvalidComponentException;
import org.thespheres.ical.UID;
import org.thespheres.ical.util.IComponentUtilities;

/**
 *
 * @author boris.heithecker
 */
class ComponentChildren extends ChildFactory.Detachable<Key> implements LookupListener, ChangeListener {

    private final Lookup.Result<CalendarProvider> lkpResult;
    private final List<CalendarProvider> providers = new ArrayList<>();

    ComponentChildren(Lookup context) {
        this.lkpResult = context.lookupResult(CalendarProvider.class);
    }

    @Override
    protected boolean createKeys(final List<Key> toPopulate) {
        final List<Key> l = new ArrayList<>();
        synchronized (providers) {
            providers.stream().forEach(cp -> {
                cp.getCalendars().stream()
                        .map(ICalendar::getComponents)
                        .flatMap(List::stream)
                        .map(cc -> new Key(cc.getUID(), cc, cp))
                        .forEach(l::add);
            });
        }
        l.stream()
                .sorted()
                .forEachOrdered(toPopulate::add);
        return true;
    }

    @Override
    protected void removeNotify() {
        lkpResult.removeLookupListener(this);
    }

    @Override
    protected void addNotify() {
        lkpResult.addLookupListener(this);
        resultChanged(null);
    }

    @Override
    protected Node createNodeForKey(Key key) {
        if (key.provider instanceof CalendarProvider.Decorator) {
            return ((CalendarProvider.Decorator) key.provider).decorate(key.comp);
        }
        return null;
    }

    @Override
    public void resultChanged(final LookupEvent ev) {
        synchronized (providers) {
            providers.stream().forEach(cp -> cp.removeChangeListener(this));
            providers.clear();
            lkpResult.allInstances().stream()
                    .forEach(cp -> {
                        providers.add(cp);
                        cp.addChangeListener(this);
                    });
        }
        stateChanged(null);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        refresh(false);
    }

    final class Key implements Comparable<Key> {

        private final UID uid;
        private final CalendarComponent comp;
        private final CalendarProvider provider;
        private final Date compare;

        private Key(UID uid, CalendarComponent cc, CalendarProvider cp) {
            this.uid = uid;
            this.comp = cc;
            this.provider = cp;
            Date d;
            try {
                d = IComponentUtilities.parseDateProperty(comp, CalendarComponentProperty.DTSTART);
                if (d == null) {
                    d = IComponentUtilities.parseDateProperty(comp, CalendarComponentProperty.DUE);
                }
            } catch (InvalidComponentException ex) {
                d = new Date(0l);
            }
            this.compare = d;
        }

        @Override
        public int compareTo(Key o) {
            return compare.compareTo(o.compare);
        }
    }
}
