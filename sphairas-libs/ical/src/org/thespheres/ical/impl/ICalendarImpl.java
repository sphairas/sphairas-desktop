/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.ical.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.thespheres.ical.CalendarComponent;
import org.thespheres.ical.CalendarComponentProperty;
import org.thespheres.ical.ICalendar;
import org.thespheres.ical.InvalidComponentException;
import org.thespheres.ical.UID;

/**
 *
 * @author boris.heithecker
 */
public class ICalendarImpl extends ComponentImpl<ComponentPropertyImpl> implements Externalizable, ICalendar {

    private static final long serialVersionUID = 1L;
    protected final ArrayList<CalendarComponentImpl> components = new ArrayList<>();

    public ICalendarImpl() {
        super(CalendarComponent.VCALENDAR);
    }

    @Override
    public ComponentPropertyImpl createProperty(String name, String value) {
        ComponentPropertyImpl ret = new ComponentPropertyImpl(name, value);
        properties.add(ret);
        return ret;
    }

    public CalendarComponentImpl createComponent(String name, UID uid) {
        CalendarComponentImpl ret = new CalendarComponentImpl(name, this, uid);
        components.add(ret);
        return ret;
    }

    public void removeComponent(CalendarComponentImpl cc) {
        if (components.contains(cc)) {
            components.remove(cc);
            cc.calendar = null;
        }
    }

    @Override
    public List<CalendarComponentProperty> getProperties(String name) {
        return properties.stream().filter((p) -> name.equals(p.getName())).map(i -> (CalendarComponentProperty) i).collect(Collectors.toList());
    }

    @Override
    public List<CalendarComponentProperty> getProperties() {
        return properties.stream().map(i -> (CalendarComponentProperty) i).collect(Collectors.toList());
    }

    @Override
    public List<CalendarComponent> getComponents() {
        return components.stream().map(i -> (CalendarComponent) i).collect(Collectors.toList());
    }

    //Internal use only, non-API
    public ArrayList<CalendarComponentImpl> getComponentsImpl() {
        return components;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeInt(components.size());
        for (CalendarComponentImpl cci : components) {
            out.writeObject(cci);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        int s = in.readInt();
        for (int i = 0; i++ < s;) {
            CalendarComponentImpl cci = (CalendarComponentImpl) in.readObject();
            components.add(cci);
            cci.calendar = this;
        }
    }

    @Override
    protected void toString(final StringBuilder sb) {
        properties.forEach(pi -> {
            pi.toString(sb);
        });
        components.forEach(pi -> {
            pi.toString(sb);
        });
    }

    @Override
    public void validate() throws InvalidComponentException {
        getAnyPropertyValue(CalendarComponent.PRODID).orElseThrow(() -> {
            return new InvalidComponentException(this, "No PRODID.");
        });
        getAnyPropertyValue(CalendarComponent.VERSION).orElseThrow(() -> {
            return new InvalidComponentException(this, "No VERSION.");
        });
        if (getComponents().isEmpty()) {
            throw new InvalidComponentException(this, "Empty calendar.");
        }
        for (CalendarComponent cc : getComponents()) {
            cc.validate();
        }
    }

}
