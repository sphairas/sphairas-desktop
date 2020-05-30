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
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.openide.util.NbBundle;
import org.thespheres.ical.builder.AbstractComponentBuilder;
import org.thespheres.ical.builder.ICalendarBuilder;
import org.thespheres.ical.CalendarComponent;
import org.thespheres.ical.CalendarComponentProperty;
import org.thespheres.ical.ICalendar;
import org.thespheres.ical.InvalidComponentException;
import org.thespheres.ical.Parameter;
import org.thespheres.ical.UID;
import org.thespheres.ical.builder.ICalendarBuilder.PropertyIterator;
import org.thespheres.ical.util.IComponentUtilities;

/**
 *
 * @author boris.heithecker
 */
public class CalendarComponentImpl extends ComponentImpl<ComponentPropertyImpl> implements CalendarComponent, Externalizable {

    private static final long serialVersionUID = 1L;
    ICalendarImpl calendar;
    private UID uid;

    public CalendarComponentImpl() {
    }

    CalendarComponentImpl(String name, ICalendarImpl parent, UID uid) {
        super(name);
        this.calendar = parent;
        this.uid = uid;
    }

    @Override
    public ICalendar getICalendar() {
        return calendar;
    }

    @Override
    public UID getUID() {
        return uid;
    }

    @NbBundle.Messages("CalendarComponentImpl.parseUIDException=Will not replace UID already set on component. ")
    public void parseUID(String value) throws ParseException, InvalidComponentException {
        if (uid != null) {
            throw new InvalidComponentException(this, NbBundle.getMessage(CalendarComponentImpl.class, "CalendarComponentImpl.parseUIDException"));
        }
        uid = UID.parse(value);
    }

    @Override
    public ComponentPropertyImpl createProperty(String name, String value) {
        ComponentPropertyImpl ret = new ComponentPropertyImpl(name, value);
        properties.add(ret);
        return ret;
    }

    @Override
    public List<CalendarComponentProperty> getProperties() {
        return Collections.unmodifiableList(properties);
    }

    @Override
    public List<CalendarComponentProperty> getProperties(final String name) {
        return properties.stream()
                .filter(p -> name.equals(p.getName()))
                .map(CalendarComponentProperty.class::cast)
                .collect(Collectors.toList());
    }

    public PropertyIterator propertyIterator() {
        return new PropertyIteratorImpl();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(uid);
        super.writeExternal(out);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        uid = (UID) in.readObject();
        super.readExternal(in);
    }

    @Override
    protected void toString(StringBuilder sb) {
        sb.append(BEGIN).append(":").append(getName()).append(AbstractComponentBuilder.NEWLINE);
        super.toString(sb);
        if (getUID() != null) {
            ComponentPropertyImpl.propertyToString(sb, CalendarComponentProperty.UID, getUID().toString(), null);
        }
        sb.append(END).append(":").append(getName()).append(AbstractComponentBuilder.NEWLINE);
    }

    @Override
    public void validate() throws InvalidComponentException {
    }

    @Override
    public LocalDateTime resolveStart() throws InvalidComponentException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public LocalDateTime resolveEnd() throws InvalidComponentException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ICalendar inflate(Date limit) throws InvalidComponentException {
        Inflater inflater = new Inflater(this, limit);
        ICalendarBuilder builder = new ICalendarBuilder();
        builder.addProperty(CalendarComponent.VERSION, "2.0");
        ComponentPropertyImpl prodid = calendar.getAnyProperty(CalendarComponent.PRODID);
        if (prodid != null) {
            Parameter[] prodparams = prodid.getParameters().stream().toArray(Parameter[]::new);
            builder.addProperty(CalendarComponent.PRODID, prodid.getValue(), prodparams);
        }
        ComponentPropertyImpl dtStart = getAnyProperty(CalendarComponentProperty.DTSTART);
        boolean isStartDateFormat = false;
        Date start = null;
        if (dtStart != null) {
            isStartDateFormat = IComponentUtilities.isDateFormat(dtStart);
            start = IComponentUtilities.parseDateProperty(dtStart);
        }
        Parameter[] dtStartValueParam = isStartDateFormat ? new Parameter[]{new Parameter("VALUE", "DATE")} : new Parameter[0];
        ComponentPropertyImpl dtEnd = getAnyProperty(CalendarComponentProperty.DTSTART);
        Date end = null;
        boolean isEndDateFormat = false;
        long dur = 0;
        if (dtEnd != null && start != null) {
            isEndDateFormat = IComponentUtilities.isDateFormat(dtStart);
            end = IComponentUtilities.parseDateProperty(this, CalendarComponentProperty.DTEND);
            dur = end.getTime() - start.getTime();
            if (dur < 0) {
                throw new InvalidComponentException(this, "DTEND before DTSTART");
            }
        }
        Parameter[] dtEndValueParam = isEndDateFormat ? new Parameter[]{new Parameter("VALUE", "DATE")} : new Parameter[0];
//   Property Name:  DTEND
//
//   Purpose:  This property specifies the date and time that a calendar
//      component ends.
//
//   Value Type:  The default value type is DATE-TIME.  The value type can
//      be set to a DATE value type.
//
//   Property Parameters:  IANA, non-standard, value data type, and time
//      zone identifier property parameters can be specified on this
//      property.
//
//   Conformance:  This property can be specified in "VEVENT" or
//      "VFREEBUSY" calendar components.
//
//   Description:  Within the "VEVENT" calendar component, this property
//      defines the date and time by which the event ends.  The value type
//      of this property MUST be the same as the "DTSTART" property, and
//      its value MUST be later in time than the value of the "DTSTART"
//      property.  Furthermore, this property MUST be specified as a date
//      with local time if and only if the "DTSTART" property is also
//      specified as a date with local time.
        for (Date d : inflater) {
            ICalendarBuilder.CalendarComponentBuilder cmp = builder.addComponent(getName(), getUID());
            String startValue = isStartDateFormat ? IComponentUtilities.DATE.format(d) : IComponentUtilities.DATE_TIME.format(d);
            cmp.addProperty(CalendarComponentProperty.DTSTART, startValue, dtStartValueParam);
            //add recurrence id
            //add dtend
            if (end != null) {
                Date shiftedEnd = new Date(d.getTime() + dur);
                String endValue = isEndDateFormat ? IComponentUtilities.DATE.format(shiftedEnd) : IComponentUtilities.DATE_TIME.format(shiftedEnd);
                cmp.addProperty(CalendarComponentProperty.DTEND, endValue, dtEndValueParam);
            }
            String[] exclude = new String[]{CalendarComponentProperty.DTSTART,
                CalendarComponentProperty.DTEND,
                CalendarComponentProperty.RRULE,
                CalendarComponentProperty.RDATE,
                CalendarComponentProperty.EXDATE,
                CalendarComponentProperty.EXRULE,
                CalendarComponentProperty.UID,
                CalendarComponentProperty.RECURRENCEID};
            for (ComponentPropertyImpl cp : properties) {
                final String name = cp.getName();
                if (Arrays.stream(exclude).anyMatch(name::equals)) {
                    continue;
                }
                Parameter[] params = cp.getParameters().stream().toArray(Parameter[]::new);
                cmp.addProperty(name, cp.getValue(), params);
            }
        }
        return builder.toICalendar();
    }

    @Override
    public ICalendar inflate(final LocalDateTime limit) throws InvalidComponentException {
//        final ZoneId zid = ZoneId.systemDefault();
//        LocalDateTime ldt = LocalDateTime.ofInstant(limit.toInstant(), zid);
        final Inflater3 inflater = new Inflater3(this, limit);
        ICalendarBuilder builder = new ICalendarBuilder();
        builder.addProperty(CalendarComponent.VERSION, "2.0");
        ComponentPropertyImpl prodid = calendar.getAnyProperty(CalendarComponent.PRODID);
        if (prodid != null) {
            Parameter[] prodparams = prodid.getParameters().stream().toArray(Parameter[]::new);
            builder.addProperty(CalendarComponent.PRODID, prodid.getValue(), prodparams);
        }
        ComponentPropertyImpl dtStart = getAnyProperty(CalendarComponentProperty.DTSTART);
        boolean isStartDateFormat = false;
        Date start = null;
        if (dtStart != null) {
            isStartDateFormat = IComponentUtilities.isDateFormat(dtStart);
            start = IComponentUtilities.parseDateProperty(dtStart);
        }
        Parameter[] dtStartValueParam = isStartDateFormat ? new Parameter[]{new Parameter("VALUE", "DATE")} : new Parameter[0];
        ComponentPropertyImpl dtEnd = getAnyProperty(CalendarComponentProperty.DTEND);
        Date end = null;
        boolean isEndDateFormat = false;
        long dur = 0;
        if (dtEnd != null && start != null) {
            isEndDateFormat = IComponentUtilities.isDateFormat(dtEnd);
            end = IComponentUtilities.parseDateProperty(dtEnd);
            dur = end.getTime() - start.getTime();
            if (dur < 0) {
                throw new InvalidComponentException(this, "DTEND before DTSTART");
            }
        }
        Parameter[] dtEndValueParam = isEndDateFormat ? new Parameter[]{new Parameter("VALUE", "DATE")} : new Parameter[0];
//   Property Name:  DTEND
//
//   Purpose:  This property specifies the date and time that a calendar
//      component ends.
//
//   Value Type:  The default value type is DATE-TIME.  The value type can
//      be set to a DATE value type.
//
//   Property Parameters:  IANA, non-standard, value data type, and time
//      zone identifier property parameters can be specified on this
//      property.
//
//   Conformance:  This property can be specified in "VEVENT" or
//      "VFREEBUSY" calendar components.
//
//   Description:  Within the "VEVENT" calendar component, this property
//      defines the date and time by which the event ends.  The value type
//      of this property MUST be the same as the "DTSTART" property, and
//      its value MUST be later in time than the value of the "DTSTART"
//      property.  Furthermore, this property MUST be specified as a date
//      with local time if and only if the "DTSTART" property is also
//      specified as a date with local time.
        for (LocalDateTime d : inflater) {
            ICalendarBuilder.CalendarComponentBuilder cmp = builder.addComponent(getName(), getUID());
            String startValue = isStartDateFormat ? IComponentUtilities.DATE_FORMATTER.format(d) : IComponentUtilities.DATETIME_FORMATTER.format(d);
            cmp.addProperty(CalendarComponentProperty.DTSTART, startValue, dtStartValueParam);
            //add recurrence id
            //add dtend
            if (end != null) {
                LocalDateTime shiftedEnd = d.plus(dur, ChronoUnit.MILLIS); // new Date(d.getTime() + dur);
                String endValue = isEndDateFormat ? IComponentUtilities.DATE_FORMATTER.format(shiftedEnd) : IComponentUtilities.DATETIME_FORMATTER.format(shiftedEnd);
                cmp.addProperty(CalendarComponentProperty.DTEND, endValue, dtEndValueParam);
            }
            String[] exclude = new String[]{CalendarComponentProperty.DTSTART,
                CalendarComponentProperty.DTEND,
                CalendarComponentProperty.RRULE,
                CalendarComponentProperty.RDATE,
                CalendarComponentProperty.EXDATE,
                CalendarComponentProperty.EXRULE,
                CalendarComponentProperty.UID,
                CalendarComponentProperty.RECURRENCEID};
            for (ComponentPropertyImpl cp : properties) {
                final String name = cp.getName();
                if (Arrays.stream(exclude).anyMatch(name::equals)) {
                    continue;
                }
                Parameter[] params = cp.getParameters().stream().toArray(Parameter[]::new);
                cmp.addProperty(name, cp.getValue(), params);
            }
        }
        return builder.toICalendar();
    }

    public class PropertyIteratorImpl implements PropertyIterator {

        private final Iterator<ComponentPropertyImpl> delegate;
        private ComponentPropertyImpl last;

        private PropertyIteratorImpl() {
            delegate = properties.iterator();
        }

        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        @Override
        public CalendarComponentProperty next() {
            last = delegate.next();
            return last;
        }

        @Override
        public void setValue(String value) {
            if (last == null) {
                throw new IllegalStateException();
            }
            last.setValue(value);
        }

        @Override
        public ICalendarBuilder.ParameterIterator iterator() {
            if (last == null) {
                throw new IllegalStateException();
            }
            return last.iterator();
        }

        @Override
        public void remove() {
            delegate.remove();
        }
    }
}
