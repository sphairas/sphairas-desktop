/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.ical.builder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.thespheres.ical.CalendarComponent;
import org.thespheres.ical.CalendarComponentProperty;
import org.thespheres.ical.ICalendar;
import org.thespheres.ical.InvalidComponentException;
import org.thespheres.ical.Parameter;
import org.thespheres.ical.UID;
import org.thespheres.ical.impl.CalendarComponentImpl;
import org.thespheres.ical.impl.CalendarParserImpl;
import org.thespheres.ical.impl.ComponentPropertyImpl;
import org.thespheres.ical.impl.FormatException;
import org.thespheres.ical.impl.ICalendarImpl;
import org.thespheres.ical.util.IComponentUtilities;

/**
 *
 * @author boris.heithecker
 */
public class ICalendarBuilder extends AbstractComponentBuilder {

    private final ICalendarImpl ical;

    public ICalendarBuilder() {
        super(CalendarComponent.VCALENDAR);
        this.ical = new ICalendarImpl();
    }

    public static List<ICalendar> parseCalendars(URL source) throws ParseException, IOException, InvalidComponentException {
        try (InputStream is = source.openStream()) {
            return parseCalendars(is);
        }
    }

    public static List<ICalendarBuilder> parseCalendarsToBuilder(URL source) throws ParseException, IOException, InvalidComponentException {
        try (InputStream is = source.openStream()) {
            return parseCalendarsToBuilder(is);
        }
    }

    public static List<ICalendar> parseCalendars(InputStream source) throws ParseException, IOException, InvalidComponentException {
        final List<ICalendarBuilder> builders = parseCalendarsToBuilder(source);
        return builders.stream()
                .map(ICalendarBuilder::toICalendar)
                .collect(Collectors.toList());
    }

    public static List<ICalendar> parseCalendars(InputStream source, String encoding) throws ParseException, IOException, InvalidComponentException {
        final List<ICalendarBuilder> builders = parseCalendarsToBuilder(source, encoding);
        return builders.stream()
                .map(ICalendarBuilder::toICalendar)
                .collect(Collectors.toList());
    }

    public static List<ICalendarBuilder> parseCalendarsToBuilder(InputStream is) throws ParseException, IOException, InvalidComponentException {
        return parseIS(is, null);
    }

    public static List<ICalendarBuilder> parseCalendarsToBuilder(InputStream is, String encoding) throws ParseException, IOException, InvalidComponentException {
        return parseIS(is, encoding);
    }

    protected static List<ICalendarBuilder> parseIS(final InputStream is, final String enc) throws ParseException, IOException, InvalidComponentException {
        final InputStreamReader r = enc == null ? new InputStreamReader(is) : new InputStreamReader(is, enc);
        final CalendarParserImpl parser = new CalendarParserImpl();
        try {
            return parser.parse(new LineNumberReader(r));
        } catch (FormatException ex) {
            throw new ParseException(ex.getLine(), ex.getOffset());
        }
    }

    public void merge(ICalendar other, Resolver r) throws InvalidComponentException {
        Map<UniqueComponentKey, List<CalendarComponent>> m = other.getComponents().stream().collect(Collectors.groupingBy(cc -> {
            UID uid = cc.getUID();
            Date rid = null;
            try {
                rid = IComponentUtilities.parseDateProperty(cc, CalendarComponentProperty.RECURRENCEID);
            } catch (InvalidComponentException ex) {
            }
            return new UniqueComponentKey(uid, rid);
        }));
        List<CalendarComponent> l = m.values().stream()
                .map(c -> c.toArray(new CalendarComponent[c.size()]))
                .map(r::resolve)
                .collect(Collectors.toList());
        for (CalendarComponent c : l) {
            mergeComponent(c);
        }
    }

    @Override
    public ICalendarBuilder addProperty(String name, String value, Parameter... parameter) throws InvalidComponentException {
        if (checkValue(name, value)) {
            ComponentPropertyImpl ret = ical.createProperty(name, value);
            for (Parameter p : parameter) {
                if (p != null && checkValue(p.getName(), p.getValue())) {
                    ret.addParameter(p.getName(), p.getValue());
                }
            }
        }
        return this;
    }

    public CalendarComponentBuilder addComponent(String name, UID uid) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name must not be null or empty");
        }
        return new CalendarComponentBuilder(name, uid, this);
    }

    public CalendarComponentBuilder mergeComponent(CalendarComponent other) throws InvalidComponentException {
        final CalendarComponentBuilder ret = addComponent(other.getName(), other.getUID());
        for (CalendarComponentProperty p : other.getProperties()) {
            ret.mergeProperty(p);
        }
        return ret;
    }

    public CalendarComponentBuilder toComponentBuilder(CalendarComponent cc) {
        if (cc instanceof CalendarComponentImpl && ical.getComponents().contains(cc)) {
            return new CalendarComponentBuilder((CalendarComponentImpl) cc, this);
        }
        throw new IllegalArgumentException("Component is not a component of this calender.");
    }

    public Iterator<CalendarComponentBuilder> iterator() {

        final Iterator<CalendarComponentImpl> original = ical.getComponentsImpl().iterator();

        class CCBIterator implements Iterator<CalendarComponentBuilder> {

            private CalendarComponentBuilder current;

            @Override
            public boolean hasNext() {
                return original.hasNext();
            }

            @Override
            public CalendarComponentBuilder next() {
                CalendarComponent cc = original.next();
                return current = new CalendarComponentBuilder((CalendarComponentImpl) cc, ICalendarBuilder.this);
            }

            @Override
            public void remove() {
                original.remove();
                current.parent = null;
            }

        }
        return new CCBIterator();
    }

    public ICalendar toICalendar() {
        return ical;
    }

    private boolean checkValue(String name, String value) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name must not be null or empty");
        }
        return value != null && !value.isEmpty();
    }

    public class CalendarComponentBuilder extends AbstractComponentBuilder {

        private final CalendarComponentImpl component;
        private AbstractComponentBuilder parent;

        private CalendarComponentBuilder(String name, UID uid, AbstractComponentBuilder parent) {
            super(name);
            component = ical.createComponent(name, uid);
            this.parent = parent;
        }

        private CalendarComponentBuilder(CalendarComponentImpl component, AbstractComponentBuilder parent) {
            super(component.getName());
            this.component = component;
            this.parent = parent;
        }

        @Override
        public AbstractComponentBuilder getParent() {
            return parent;
        }

        @Override
        public CalendarComponentBuilder addProperty(String name, String value, Parameter... parameter) throws InvalidComponentException {
            if (checkValue(name, value)) {
                if (CalendarComponentProperty.UID.equals(name)) {
                    try {
                        component.parseUID(value);
                    } catch (ParseException ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    ComponentPropertyImpl ret = component.createProperty(name, value);
                    for (Parameter p : parameter) {
                        if (p != null && checkValue(p.getName(), p.getValue())) {
                            ret.addParameter(p.getName(), p.getValue());
                        }
                    }
                }
            }
            return this;
        }

        public void remove() {
            ical.removeComponent(component);
            parent = null;
        }

        public PropertyIterator iterator() {
            return component.propertyIterator();
        }

    }

    public interface PropertyIterator extends Iterator<CalendarComponentProperty> {

        public void setValue(String value);

        public ParameterIterator iterator();

        @Override
        public void remove();
    }

    public interface ParameterIterator extends Iterator<Parameter> {

        public void setValue(String value);

        @Override
        public void remove();
    }

    public static interface Resolver {

        //All input habe same UID/recurrence id
        //return null if no merge
        public CalendarComponent resolve(CalendarComponent[] input);
    }

}
