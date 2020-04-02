/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.ical.impl;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.thespheres.ical.CalendarComponentProperty;
import org.thespheres.ical.InvalidComponentException;
import org.thespheres.ical.impl.RRule3.ByRuleLink.Num;
import org.thespheres.ical.util.IComponentUtilities;

/**
 *
 * @author boris.heithecker
 */
class RRule3 {

    private final static Pattern BYDAYRULEPATTERN = Pattern.compile("([+-]?([1-9]|[1-4][0-9]|5[0-3]))?(SU|MO|TU|WE|TH|FR|SA)");
    private int interval;
    private List<ByRuleLink> byRuleLinks;
    private final ChronoUnit frequencyField;
    private final int count;
    private final LocalDateTime endDate;

    private RRule3(ChronoUnit frequField, int count) {
        this.frequencyField = frequField;
        this.count = count;
        this.endDate = LocalDateTime.of(9999, Month.DECEMBER, 31, 23, 59);
    }

    private RRule3(ChronoUnit frequField, LocalDateTime end) {
        this.frequencyField = frequField;
        this.endDate = end;
        this.count = Integer.MAX_VALUE;
    }

    public int getInterval() {
        return interval;
    }

    private ByRuleLink getFirstLinkedByRule() {
        return byRuleLinks.isEmpty() ? null : byRuleLinks.get(0);
    }

    void applyByRuleLinks(LocalDateTime raw, TreeSet<LocalDateTime> dates) {
        final ByRuleLink initialByRule = getFirstLinkedByRule();
        if (initialByRule != null) {
            initialByRule.apply(raw, dates);
            initialByRule.finish(dates);
        } else {
            dates.add(raw);
        }
    }

    public ChronoUnit getFrequencyField() {
        return frequencyField;
    }

    public int getCount() {
        return count;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    static RRule3 parse(ComponentPropertyImpl calprop) throws InvalidComponentException {
        String propName = calprop.getName();
        if (propName.equals(CalendarComponentProperty.EXRULE)) {
            throw new IllegalArgumentException("Component EXRULE is deprecated and not supported.");
        }
        if (!propName.equals(CalendarComponentProperty.RRULE)) {
            throw new IllegalArgumentException("Component name must be RRULE.");
        }
        String dValue = calprop.getAnyParameter("VALUE").orElse(null);
        if (dValue != null) {
            //TODO check, if dValue is the same as defined in dtStart, requirement
        }
        String cv = calprop.getValue();
        Map<String, String> props = new HashMap<>();
        for (String ds : cv.split(";")) {
            String[] def = ds.split("=");
            if (def.length != 2) {
                throw new InvalidComponentException("Excepted \"=\".");
            }
            String name = def[0];
            String value = def[1];
            props.put(name, value);
        }
        //
        ChronoUnit frequencyField;
        int interval = 1;
        String f = props.get("FREQ");
        RRule3 ret = null;
        if (f == null) {
            throw new InvalidComponentException(null, "FREQ must be present.");
        }
        switch (f) {
            case "SECONDLY":
                frequencyField = ChronoUnit.SECONDS;
                break;
            case "MINUTELY":
                frequencyField = ChronoUnit.MINUTES;
                break;
            case "HOURLY":
                frequencyField = ChronoUnit.HOURS;
                break;
            case "DAILY":
                frequencyField = ChronoUnit.DAYS;
                break;
            case "WEEKLY":
                frequencyField = ChronoUnit.WEEKS;
                break;
            case "MONTHLY":
                frequencyField = ChronoUnit.MONTHS;
                break;
            case "YEARLY":
                frequencyField = ChronoUnit.YEARS;
                break;
            default:
                throw new InvalidComponentException("FREQ value is invalid.");

        }
        String countValue = props.get("COUNT");
        if (countValue != null) {
            try {
                int count = Integer.parseInt(countValue);
                ret = new RRule3(frequencyField, count);
            } catch (NumberFormatException nex) {
                throw new InvalidComponentException(nex);
            }
        }
        String untilValue = props.get("UNTIL");
        if (ret == null && untilValue != null) {
            LocalDateTime until = IComponentUtilities.parseLocalDateTime(untilValue, dValue);
            ret = new RRule3(frequencyField, until);
        }
        if (ret == null) {
            throw new InvalidComponentException("Either UNTIL or COUNT must be set, but not both.");
        }
        String intervalValue = props.get("INTERVAL");
        if (intervalValue != null) {
            try {
                interval = Integer.parseInt(intervalValue);
            } catch (NumberFormatException nex) {
                throw new InvalidComponentException(nex);
            }
        }
        ret.interval = interval;
        //create ByLinkChain
        final List<ByRuleLink> bylinks = new ArrayList<>();
        ret.createAllWeeksInMonthResolverLink(bylinks);
        ret.createByDayLink(props, bylinks);
        //bySetPos must be last
        ret.createBySetposLink(props, bylinks);
        for (int i = 0; i < bylinks.size(); i++) {
            int next = i + 1;
            ByRuleLink parent = next < bylinks.size() ? bylinks.get(next) : null;
            bylinks.get(i).parent = parent;
        }
        ret.byRuleLinks = bylinks;
        return ret;
    }

    private void createAllWeeksInMonthResolverLink(final List<ByRuleLink> toAdd) throws InvalidComponentException {
        final ChronoUnit base = (ChronoUnit) ChronoField.ALIGNED_WEEK_OF_MONTH.getBaseUnit();
        if (getFrequencyField().compareTo(base) > 0) {
            AllWeeksInMonthResolverLink sl = new AllWeeksInMonthResolverLink();
            toAdd.add(sl);
        }
    }

    private void createByDayLink(Map<String, String> props, final List<ByRuleLink> toAdd) throws InvalidComponentException {
        String value = props.get("BYDAY");
        if (value != null) {
            toAdd.add(new ByDayRuleLink(value));
        } else {
            final ChronoUnit base = (ChronoUnit) ChronoField.DAY_OF_WEEK.getBaseUnit();
            if (getFrequencyField().compareTo(base) < 0) {
                toAdd.add(new ByDayRuleLink());
            }
        }
    }

    private void createBySetposLink(Map<String, String> props, List<ByRuleLink> toAdd) throws InvalidComponentException {
        String value = props.get("BYSETPOS");
        if (value != null) {
            BySetposRuleLink ret = new BySetposRuleLink(value);
            toAdd.add(ret);
        }
    }

    private class AllWeeksInMonthResolverLink extends ByRuleLink<ByRuleLink.Num> {

        private AllWeeksInMonthResolverLink() throws InvalidComponentException {
            super(ChronoField.ALIGNED_WEEK_OF_MONTH, null);
        }

        @Override
        protected List<ByRuleLink.Num> getValues() {
            return IntStream.range(1, 6).mapToObj(ByRuleLink.Num::new).collect(Collectors.toList());
        }

    }

    private class ByDayRuleLink extends ByRuleLink<ByDayRuleLink.ByDayNum> {

        private ByDayRuleLink() throws InvalidComponentException {
            this("MO,DI,MI,DO,FR,SA,SO");
        }

        private ByDayRuleLink(String value) throws InvalidComponentException {
            super(ChronoField.DAY_OF_WEEK, value);
            for (String s : byRules) {
                values.add(checkAndParse(s));
            }
        }

        protected final ByDayRuleLink.ByDayNum checkAndParse(String ve) throws InvalidComponentException {
            if (!BYDAYRULEPATTERN.matcher(ve).matches()) {
                throw new InvalidComponentException("BYDAY rule " + ve + " is invalid.");
            }
            int l = ve.length();
            assert l >= 2;
            String dayValue = ve.substring(l - 2);
            DayOfWeek calendarDay;
            switch (dayValue) {
                case "SU":
                    calendarDay = DayOfWeek.SUNDAY;
                    break;
                case "MO":
                    calendarDay = DayOfWeek.MONDAY;
                    break;
                case "TU":
                    calendarDay = DayOfWeek.TUESDAY;
                    break;
                case "WE":
                    calendarDay = DayOfWeek.WEDNESDAY;
                    break;
                case "TH":
                    calendarDay = DayOfWeek.THURSDAY;
                    break;
                case "FR":
                    calendarDay = DayOfWeek.FRIDAY;
                    break;
                case "SA":
                    calendarDay = DayOfWeek.SATURDAY;
                    break;
                default:
                    throw new RuntimeException();
            }
            ByDayNum ret = new ByDayNum(calendarDay);
            if (l > 2) {
                ChronoUnit f = getFrequencyField();
                if (!f.equals(ChronoUnit.MONTHS) && !f.equals(ChronoUnit.YEARS)) {
                    throw new InvalidComponentException("nth occurrence in BYDAY can be set only with monthly or yearly recurrence");
                }
                //Checke Frequence  ..... 
                String ordwk = ve.substring(0, l - 2);
                Integer fv = Integer.parseInt(ordwk);
                if (fv == 0) {
                    throw new InvalidComponentException("nth occurrence in BYDAY cannot be 0.");
                }
                ret.filter = fv;
            }
            return ret;
        }

        @Override
        protected LocalDateTime applyValue(LocalDateTime seed, ByDayRuleLink.ByDayNum num) {
            LocalDateTime ret = super.applyValue(seed, num);
            if (ret != null) {
                final Integer f = num.filter;
                if (f != null) {
                    switch (getFrequencyField()) {
                        case MONTHS:
                            LocalDateTime query = ret.with(TemporalAdjusters.dayOfWeekInMonth(f, DayOfWeek.of(num.num)));
                            return ret.equals(query) ? ret : null;
                        case YEARS:
                            LocalDateTime query2 = ret.with(ConvUtil.dayOfWeekInYear(f, DayOfWeek.of(num.num)));
                            return ret.equals(query2) ? ret : null;
                    }
                }
            }
            return ret;
        }

        private class ByDayNum extends Num {

            private Integer filter = null;

            private ByDayNum(DayOfWeek value) {
                super(value.getValue());
            }

        }
    }

    private class BySetposRuleLink extends ByRuleLink<ByRuleLink.Num> {

        final TreeSet<Integer> ints = new TreeSet<>(Comparator.naturalOrder());

        private BySetposRuleLink(String value) throws InvalidComponentException {
            super(value);
            for (String s : byRules) {
                int n;
                try {
                    n = Integer.parseInt(s);
                } catch (NumberFormatException nfex) {
                    throw new InvalidComponentException(nfex);
                }
                if (n > 366 || n < -366) {
                    throw new InvalidComponentException("BYSETPOS value " + s + " is invalid (<-366 || >366).");
                }
                ints.add(n);
            }
            ints.stream()
                    .map(Num::new)
                    .forEach(values::add);
        }

        @Override
        void apply(LocalDateTime seed, final TreeSet<LocalDateTime> dates) {
            if (parent != null) {
                throw new IllegalStateException();
            }
            synchronized (dates) {
                dates.add(seed);
            }
        }

        @Override
        protected void finish(final TreeSet<LocalDateTime> dates) {
            if (parent != null) {
                throw new IllegalStateException();
            }
            final Set<LocalDateTime> toRemove = new HashSet<>();
            synchronized (dates) {
                final int size = dates.size();
                Integer pos = 0;
                for (LocalDateTime d : dates) {
                    if (!ints.contains(pos) && !ints.contains(pos - size)) {
                        toRemove.add(d);
                    }
                    ++pos;
                }
                toRemove.stream()
                        .forEach(dates::remove);
            }
        }
    }

    //One link for year, month, week, day, hour, minute, second, 
    //chaind: year.parent=month
    abstract class ByRuleLink<N extends ByRuleLink.Num> {

        protected ByRuleLink parent;
        private final ChronoField chronoField;//Calendar.field....
        protected final String[] byRules;
        protected List<N> values = new ArrayList<>();

        protected ByRuleLink(ChronoField calendarField, String value) throws InvalidComponentException {
            this.chronoField = calendarField;
            this.byRules = value != null ? value.split(",") : null;
        }

        protected ByRuleLink(String value) throws InvalidComponentException {
            this(null, value);
        }

        ChronoField getChronoField() {
            return chronoField;
        }

        protected List<N> getValues() {
            return values;
        }

        void apply(LocalDateTime seed, final TreeSet<LocalDateTime> dates) {
            final List<LocalDateTime> resolvedByMe = new ArrayList<>();
            getValues().stream()
                    .forEach(iv -> {
                        final LocalDateTime resolved;
                        if ((resolved = applyValue(seed, iv)) != null) {
                            resolvedByMe.add(resolved);
                        }
                    });
            //resolve yourself
            resolvedByMe.stream()
                    .forEach(c -> {
                        if (parent != null) {
                            parent.apply(c, dates);
                        } else {
                            synchronized (dates) {
                                dates.add(c);
                            }
                        }
                    });
        }

        protected LocalDateTime applyValue(LocalDateTime seed, N iv) {
            if (chronoField != null) {
                final LocalDateTime candidate = seed.with(chronoField, iv.getValue());
                if (chronoField.range().getSmallestMaximum() < iv.getValue()) { //May be set to next
                    return mayBeResolveCandidate(seed, candidate, iv);
                }
                return candidate;
            }
            return null;
        }

        protected LocalDateTime mayBeResolveCandidate(LocalDateTime seed, LocalDateTime candidate, N num) {
            return candidate;
        }

        protected void finish(final TreeSet<LocalDateTime> dates) {
            if (parent != null) {
                parent.finish(dates);
            }
        }

        class Num {

            final int num;

            protected Num(int value) {
                this.num = value;
            }

            int getValue() {
                return num;
            }

        }
    }

}
