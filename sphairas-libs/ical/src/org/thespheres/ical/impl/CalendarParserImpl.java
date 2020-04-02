/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.ical.impl;

import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import org.thespheres.ical.builder.AbstractComponentBuilder;
import org.thespheres.ical.builder.ICalendarBuilder;
import org.thespheres.ical.CalendarComponent;
import org.thespheres.ical.InvalidComponentException;

/**
 *
 * @author boris.heithecker
 */
public class CalendarParserImpl extends ParserImpl<ICalendarBuilder> {

    private final ArrayList<ICalendarBuilder> items = new ArrayList<>();

    @Override
    public List<ICalendarBuilder> parse(LineNumberReader text) throws FormatException, IOException, InvalidComponentException {
        parseImpl(text);
        return items;
    }

    @Override
    protected AbstractComponentBuilder addComponent(AbstractComponentBuilder current, String componentName, State state) throws FormatException {
        if (current != null && current instanceof ICalendarBuilder) {
            return ((ICalendarBuilder) current).addComponent(componentName, null);
        } else {
            if (componentName == null || !componentName.equals(CalendarComponent.VCALENDAR)) {
                throw new FormatException("      ", state.line, state.index);
            }
            return new ICalendarBuilder();
        }
    }

    @Override
    protected AbstractComponentBuilder endComponent(AbstractComponentBuilder current, String componentName, State state) throws FormatException {
        // finish the current component
        if (current == null || !componentName.equals(current.getName())) {
            throw new FormatException("Unexpected END " + componentName, state.line, state.index);
        } else if (componentName.equals(CalendarComponent.VCALENDAR)) {
            items.add((ICalendarBuilder) current);
        }
        return current.getParent();
    }
}
