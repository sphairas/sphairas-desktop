/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.ical.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.TreeSet;
import org.thespheres.ical.CalendarComponentProperty;
import org.thespheres.ical.InvalidComponentException;
import org.thespheres.ical.util.IComponentUtilities;

/**
 *
 * @author boris.heithecker
 */
class Inflater implements Iterable<Date> {

    private final CalendarComponentImpl original;
    private final RRule rrule;
    private final Date start;
    private final Date max;
    private final ArrayList<RDates> rdates = new ArrayList<>();
    private final ArrayList<RDates> exdates = new ArrayList<>();

    Inflater(CalendarComponentImpl orig, Date max) throws InvalidComponentException {
        this.original = orig;
        this.max = max;
        ComponentPropertyImpl dtStart = original.getAnyProperty(CalendarComponentProperty.DTSTART);
        start = IComponentUtilities.parseDate(dtStart.getValue(), dtStart.getAnyParameter("VALUE").orElse(null));
        ComponentPropertyImpl rr = original.getAnyProperty(CalendarComponentProperty.RRULE);
        rrule = RRule.parse(rr);
        for (ComponentPropertyImpl ccp : original.properties) {
            switch (ccp.getName()) {
                case CalendarComponentProperty.RDATE:
                    rdates.add(RDates.parse(ccp, start));
                    break;
                case CalendarComponentProperty.EXDATE:
                    exdates.add(RDates.parse(ccp, null));
                    break;
            }
        }
    }

    @Override
    public ResultHolder iterator() {
        return new ResultHolder(start, max);
    }

    private class ResultHolder implements Iterator<Date> {

        private final TreeSet<Date> dates = new TreeSet<>();
        private Iterator<Date> datesIterator;
        private final Calendar currentRaw = Calendar.getInstance();
        private Date next;
//        private final ByRuleLink initialByRule;
        private final Date maximumDate;
        private int returned = 0;

        public ResultHolder(final Date initial, Date max) {
            this.currentRaw.setTime(initial);
            next = initial;
//            this.initialByRule = rrule.getFirstLinkedByRule();
            this.maximumDate = max.before(rrule.getEndDate()) ? max : rrule.getEndDate();
            rdates.stream()
                    .flatMap(RDates::stream)
                    .forEach(dates::add);
        }

        private boolean isExDate(final Date n) {
            return exdates.stream()
                    .anyMatch(rd -> rd.contains(n));
        }

        private Date nextRaw() {
            dates.clear();
            currentRaw.add(rrule.getFrequencyField(), rrule.getInterval());
            rrule.applyByRuleLinks(currentRaw, dates);
            datesIterator = dates.iterator();
            return datesIterator.hasNext() ? datesIterator.next() : null;
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public Date next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Date ret = next;
            next = null;
            if (returned++ < rrule.getCount()) {
                findNext();
            }
            return ret;
        }

        private void findNext() {
            Date n;
            if (datesIterator != null && datesIterator.hasNext()) {
                n = datesIterator.next();
            } else {
                n = nextRaw();
            }
            if (n != null && n.compareTo(maximumDate) <= 0) {
                if (!isExDate(n)) {
                    next = n;
                } else {
                    findNext();
                }
            }
        }
    }
}
