/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.ical.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
class Inflater3 implements Iterable<LocalDateTime> {

    private final CalendarComponentImpl original;
    private final RRule3 rrule;
    private final LocalDateTime start;
    private final LocalDateTime max;
    private final ArrayList<RDates3> rdates = new ArrayList<>();
    private final ArrayList<RDates3> exdates = new ArrayList<>();

    Inflater3(CalendarComponentImpl orig, LocalDateTime max) throws InvalidComponentException {
        this.original = orig;
        this.max = max;
        ComponentPropertyImpl dtStart = original.getAnyProperty(CalendarComponentProperty.DTSTART);
        start = IComponentUtilities.parseLocalDateTime(dtStart.getValue(), dtStart.getAnyParameter("VALUE").orElse(null));
        ComponentPropertyImpl rr = original.getAnyProperty(CalendarComponentProperty.RRULE);
        rrule = RRule3.parse(rr);
        for (ComponentPropertyImpl ccp : original.properties) {
            switch (ccp.getName()) {
                case CalendarComponentProperty.RDATE:
                    rdates.add(RDates3.parse(ccp, start));
                    break;
                case CalendarComponentProperty.EXDATE:
                    exdates.add(RDates3.parse(ccp, null));
                    break;
            }
        }
    }

    @Override
    public ResultHolder iterator() {
        return new ResultHolder(start, max);
    }

    private class ResultHolder implements Iterator<LocalDateTime> {

        private final TreeSet<LocalDateTime> dates = new TreeSet<>();
        private Iterator<LocalDateTime> datesIterator;
        private LocalDateTime currentRaw;
        private LocalDateTime next;
//        private final ByRuleLink initialByRule;
        private final LocalDateTime maximumDate;
        private int returned = 0;

        public ResultHolder(final LocalDateTime initial, LocalDateTime max) {
            this.currentRaw = initial;
            next = initial;
//            this.initialByRule = rrule.getFirstLinkedByRule();
            this.maximumDate = max.isBefore(rrule.getEndDate()) ? max : rrule.getEndDate();
            rdates.stream()
                    .flatMap(RDates3::stream)
                    .forEach(dates::add);
        }

        private boolean isExDate(final LocalDateTime n) {
            return exdates.stream()
                    .anyMatch(rd -> rd.contains(n));
        }

        private LocalDateTime nextRaw() {
            dates.clear();
            currentRaw = currentRaw.plus(rrule.getInterval(), rrule.getFrequencyField());
            rrule.applyByRuleLinks(currentRaw, dates);
            datesIterator = dates.iterator();
            return datesIterator.hasNext() ? datesIterator.next() : null;
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public LocalDateTime next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            LocalDateTime ret = next;
            next = null;
            if (returned++ < rrule.getCount()) {
                findNext();
            }
            return ret;
        }

        private void findNext() {
            LocalDateTime n;
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
