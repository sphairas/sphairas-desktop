/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.icalendar.local;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.thespheres.betula.icalendar.CalendarBuilderProvider;
import javax.swing.event.ChangeListener;
import org.netbeans.api.actions.Savable;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.thespheres.ical.builder.ICalendarBuilder;
import org.thespheres.betula.icalendar.CalendarProvider;
import org.thespheres.ical.CalendarComponent;
import org.thespheres.ical.ICalendar;

/**
 *
 * @author boris.heithecker
 */
class DOCalendarProvider implements CalendarBuilderProvider, CalendarProvider.Decorator {

    private final List<ICalendarBuilder> builders;
    private final String id;
    private final ICalendarDataObject data;
    private final ChangeSupport cSupport = new ChangeSupport(this);

    DOCalendarProvider(List<ICalendarBuilder> builders, ICalendarDataObject data) {
        this.builders = builders;
        this.data = data;
        String url = data.getPrimaryFile().toURL().toString();
        id = url;
    }

    @Override
    public void setModified() {
        cSupport.fireChange();
        data.setModified(true);
        data.calendars.getExecutor().post(this::save);
    }

    private void save() {
        Savable sv = data.getLookup().lookup(Savable.class);
        if (sv != null) {
            try {
                sv.save();
            } catch (IOException ex) {
                Logger.getLogger(ICalendarDataObject.class.getCanonicalName()).log(Level.SEVERE, ex.getLocalizedMessage());
            }
        }
    }

    @Override
    public List<ICalendar> getCalendars() {
        return builders.stream()
                .map(ICalendarBuilder::toICalendar)
                .collect(Collectors.toList());
    }

    @Override
    public List<ICalendarBuilder> getCalendarBuilders() {
        return builders;
    }

    @Override
    public Object getCalendarId() {
        return id;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        cSupport.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        cSupport.removeChangeListener(listener);
    }

    @Override
    public Node decorate(CalendarComponent component) {
        if (component != null && component.getName().equals(CalendarComponent.VTODO)) {
            return new VToDoComponentNode(component);
        }
        return null;
    }

}
