/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.ical.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.AbstractSet;
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
class RDates3 extends AbstractSet<LocalDateTime> {

    private boolean isDateFormat;
    private final HashSet<DateItem> values = new HashSet<>();
//    private boolean isExDates;
//    private LocalDateTime startCal;

    private RDates3() {
    }

    @Override
    public boolean add(LocalDateTime e) {
        return values.add(new DateItem(e.toLocalDate(), e.toLocalTime()));
    }

    boolean add(LocalDate date, LocalTime time) {
        return values.add(new DateItem(date, time));
    }

    @Override
    public Iterator<LocalDateTime> iterator() {
        class ItemIterator implements Iterator<LocalDateTime> {

            private final Iterator<DateItem> delegate;

            private ItemIterator(Iterator<DateItem> orig) {
                delegate = orig;
            }

            @Override
            public boolean hasNext() {
                return delegate.hasNext();
            }

            @Override
            public LocalDateTime next() {
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
        if (o instanceof LocalDateTime) {
            LocalDateTime ldt = (LocalDateTime) o;
            DateItem odi = new DateItem(ldt.toLocalDate(), ldt.toLocalTime());
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
        if (o instanceof LocalDateTime) {
            LocalDateTime ldt = (LocalDateTime) o;
            DateItem odi = new DateItem(ldt.toLocalDate(), ldt.toLocalTime());
            return values.stream()
                    .anyMatch(di -> di.equals(odi));
        }
        return false;
    }

    static RDates3 parse(ComponentPropertyImpl calprop, LocalDateTime start) throws InvalidComponentException {
        String propName = calprop.getName();
        if (!(propName.equals(CalendarComponentProperty.RDATE) || propName.equals(CalendarComponentProperty.EXDATE))) {
            throw new IllegalArgumentException("Component name must be RDATE or EXDATE.");
        }
        final RDates3 ret = new RDates3();
        String value;
        if (calprop.getValue() == null || (value = calprop.getValue().trim()).isEmpty()) {
            throw new InvalidComponentException("Empty value.");
        }
        ret.isDateFormat = IComponentUtilities.isDateFormat(calprop);
        final LocalTime lt = start != null ? start.toLocalTime() : LocalTime.of(0, 0, 0);
        for (String v : value.split(",")) {
            try {
                if (ret.isDateFormat) {
                    final LocalDate ld = LocalDate.parse(v, IComponentUtilities.DATE_FORMATTER);
                    ret.add(ld, lt);
                } else {
                    final LocalDateTime ldt = LocalDateTime.parse(v, IComponentUtilities.DATETIME_FORMATTER);
                    ret.add(ldt.toLocalDate(), ldt.toLocalTime());
                }
            } catch (DateTimeParseException ex) {
                throw new InvalidComponentException(ex);
            }
        }
        return ret;
    }

    private final class DateItem {

        final LocalDate date;
        final LocalTime time;

        private DateItem(LocalDate date, LocalTime time) {
            this.date = date;
            this.time = time;
        }

        private LocalDateTime getDate() {
            return LocalDateTime.of(date, time);
        }

        private boolean isDateFormat() {
            return isDateFormat;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 37 * hash + Objects.hashCode(this.date);
            hash = 37 * hash + Objects.hashCode(this.time);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final DateItem other = (DateItem) obj;
            if (!Objects.equals(this.date, other.date)) {
                return false;
            }
            if (this.isDateFormat() && other.isDateFormat()) {
                return true;
            }
            return Objects.equals(this.time, other.time);
        }

    }
}
