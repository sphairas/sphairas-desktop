/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.icalendar;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.StringJoiner;
import javax.swing.Action;
import org.openide.actions.DeleteAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.thespheres.ical.CalendarComponent;
import org.thespheres.ical.CalendarComponentProperty;
import org.thespheres.ical.InvalidComponentException;
import org.thespheres.ical.util.IComponentUtilities;

/**
 *
 * @author boris.heithecker
 */
public class ComponentNode extends AbstractNode {

    protected final CalendarComponent event;
    private final static DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("d.M.yy' 'HH:mm");
    private final static DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d.M.yy");

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    protected ComponentNode(CalendarComponent event) {
        super(Children.LEAF, Lookups.singleton(event));
        this.event = event;
        setName(event.getUID().toString());
        setDisplayName(getName());
        setIconBaseWithExtension("org/thespheres/betula/icalendar/resources/calendar-blue.png");
    }

    @Override
    public String getHtmlDisplayName() {
        StringJoiner sj = new StringJoiner(" ", "<html>", "</html>");
        getSummary().map(s -> "<font color='0000FF'>" + s + "</font>").ifPresent(sj::add);
        CalendarComponentProperty dtStart = event.getAnyProperty(CalendarComponentProperty.DTSTART);
        if (dtStart != null) {
            try {
                LocalDateTime start = IComponentUtilities.parseLocalDateTimeProperty(dtStart);
                boolean date = dtStart.getAnyParameter("VALUE").orElse("").equals("DATE");
                DateTimeFormatter df = date ? DATE : DATETIME;
                sj.add("<font color='AAAAAA'><i>" + df.format(start) + "</i></font>");
            } catch (InvalidComponentException ex) {
            }
        }
        return sj.toString();
    }

    protected Optional<String> getSummary() {
        return event.getAnyPropertyValue(CalendarComponentProperty.SUMMARY);
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{SystemAction.get(DeleteAction.class)};
    }

}
