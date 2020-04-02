/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.ical.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.AbstractSet;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import org.thespheres.ical.CalendarComponentProperty;
import org.thespheres.ical.InvalidComponentException;
import org.thespheres.ical.util.IComponentUtilities;

/**
 *
 * @author boris.heithecker
 */
class RDates extends AbstractSet<Date> {

    private boolean isDateFormat;
    private final HashSet<DateItem> values = new HashSet<>();
    private boolean isExDates;
    private final Calendar startCal = Calendar.getInstance();

    private RDates() {
    }

    @Override
    public boolean add(Date e) {
        return values.add(new DateItem(e));
    }

    @Override
    public Iterator<Date> iterator() {
        class ItemIterator implements Iterator<Date> {

            private final Iterator<DateItem> delegate;

            private ItemIterator(Iterator<DateItem> orig) {
                delegate = orig;
            }

            @Override
            public boolean hasNext() {
                return delegate.hasNext();
            }

            @Override
            public Date next() {
                return delegate.next().getDate();
            }

            @Override
            public void remove() {
                delegate.remove();
            }

        }
        return new ItemIterator(values.iterator());
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof Date) {
            DateItem odi = new DateItem((Date) o);
            Iterator<DateItem> it = values.iterator();
            while (it.hasNext()) {
                if (odi.equals(it.next())) {
                    it.remove();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof Date) {
            DateItem odi = new DateItem((Date) o);
            return values.stream()
                    .anyMatch(di -> di.equals(odi));
        }
        return false;
    }

    static RDates parse(ComponentPropertyImpl calprop, Date start) throws InvalidComponentException {
        String propName = calprop.getName();
        if (!(propName.equals(CalendarComponentProperty.RDATE) || propName.equals(CalendarComponentProperty.EXDATE))) {
            throw new IllegalArgumentException("Component name must be RDATE or EXDATE.");
        }
        RDates ret = new RDates();
        ret.isExDates = propName.equals(CalendarComponentProperty.EXDATE);
        ret.isDateFormat = IComponentUtilities.isDateFormat(calprop);
        final DateFormat df = ret.isDateFormat ? IComponentUtilities.DATE : IComponentUtilities.DATE_TIME;
        if (start != null) {
            ret.startCal.setTime(start);
        }
        String value;
        if (calprop.getValue() == null || (value = calprop.getValue().trim()).isEmpty()) {
            throw new InvalidComponentException("Empty value.");
        }
        for (String v : value.split(",")) {
            Date parsed;
            try {
                synchronized (df) {
                    parsed = df.parse(v);
                }
            } catch (ParseException ex) {
                throw new InvalidComponentException(ex);
            }
            ret.add(parsed);
        }
        return ret;
    }

    private final class DateItem {

        Calendar cal = Calendar.getInstance();

        private DateItem(Date origianal) {
            if (!isExDates && isDateFormat) {
                cal.setTime(startCal.getTime());
                initDate(origianal);
            } else {
                cal.setTime(origianal);
            }
        }

        private void initDate(Date origianal) {
            Calendar temp = Calendar.getInstance();
            temp.setTime(origianal);
            cal.set(Calendar.ERA, temp.get(Calendar.ERA));
            cal.set(Calendar.YEAR, temp.get(Calendar.YEAR));
            cal.set(Calendar.DAY_OF_YEAR, temp.get(Calendar.DAY_OF_YEAR));
        }

        private Date getDate() {
            return cal.getTime();
        }

        @Override
        public int hashCode() {
            int hash = 7;
            return 71 * hash + Objects.hashCode(this.cal);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final DateItem other = (DateItem) obj;
            if (!isDateFormat) {
                return Objects.equals(this.cal, other.cal);
            } else {
                return this.cal.get(Calendar.ERA) == other.cal.get(Calendar.ERA)
                        && this.cal.get(Calendar.YEAR) == other.cal.get(Calendar.YEAR)
                        && this.cal.get(Calendar.DAY_OF_YEAR) == other.cal.get(Calendar.DAY_OF_YEAR);
            }
        }

    }
}
