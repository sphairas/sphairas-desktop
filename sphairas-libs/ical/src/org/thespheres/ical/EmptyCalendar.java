/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.ical;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 *
 * @author boris.heithecker
 */
class EmptyCalendar implements ICalendar {

    private final EmptyProperty prodid = new EmptyProperty(CalendarComponent.PRODID, ICalendar.class.getCanonicalName());
    private final EmptyProperty version = new EmptyProperty(CalendarComponent.VERSION, "2.0");
    private final Set<String> propNames = new HashSet<>(Arrays.asList(prodid.name, version.name));

    EmptyCalendar() {
    }

    @Override
    public List<CalendarComponent> getComponents() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public String getName() {
        return CalendarComponent.VCALENDAR;
    }

    @Override
    public CalendarComponentProperty getAnyProperty(String name) {
        switch (name) {
            case CalendarComponent.PRODID:
                return prodid;
            case CalendarComponent.VERSION:
                return version;
            default:
                return null;
        }
    }

    @Override
    public Optional<String> getAnyPropertyValue(String name) {
        switch (name) {
            case CalendarComponent.PRODID:
                return Optional.of(prodid.value);
            case CalendarComponent.VERSION:
                return Optional.of(version.value);
            default:
                return Optional.empty();
        }
    }

    @Override
    public List<CalendarComponentProperty> getProperties() {
        return Arrays.asList(prodid, version);
    }

    @Override
    public Set<String> getPropertyNames() {
        return propNames;
    }

    @Override
    public List<CalendarComponentProperty> getProperties(String name) {
        switch (name) {
            case CalendarComponent.PRODID:
                return Collections.singletonList(prodid);
            case CalendarComponent.VERSION:
                return Collections.singletonList(version);
            default:
                return Collections.EMPTY_LIST;
        }
    }

    @Override
    public void validate() throws InvalidComponentException {
    }

    private class EmptyProperty implements CalendarComponentProperty {

        private final String name;
        private final String value;

        private EmptyProperty(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public Optional<String> getAnyParameter(String name) {
            return Optional.empty();
        }

        @Override
        public List<Parameter> getParameters() {
            return Collections.EMPTY_LIST;
        }

        @Override
        public Set<String> getParameterNames() {
            return Collections.EMPTY_SET;
        }

        @Override
        public List<Parameter> getParameters(String name) {
            return Collections.EMPTY_LIST;
        }

    }
}
