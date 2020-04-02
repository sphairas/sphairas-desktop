/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.ical.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
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
import org.thespheres.ical.impl.RRule.ByRuleLink.Num;
import org.thespheres.ical.util.IComponentUtilities;

/**
 *
 * @author boris.heithecker
 */
class RRule {

    private final static Pattern BYDAYRULEPATTERN = Pattern.compile("([+-]?([1-9]|[1-4][0-9]|5[0-3]))?(SU|MO|TU|WE|TH|FR|SA)");
    private int interval;
    private List<ByRuleLink> byRuleLinks;
    private final int frequencyField;
    private final int count;
    private final Date endDate;

    private RRule(int frequField, int count) {
        this.frequencyField = frequField;
        this.count = count;
        this.endDate = new Date(Long.MAX_VALUE);
    }

    private RRule(int frequField, Date end) {
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

    void applyByRuleLinks(Calendar raw, TreeSet<Date> dates) {
        final ByRuleLink initialByRule = getFirstLinkedByRule();
        if (initialByRule != null) {
            initialByRule.apply(raw, dates);
            initialByRule.finish(dates);
        } else {
            dates.add(raw.getTime());
        }
    }

    public int getFrequencyField() {
        return frequencyField;
    }

    public int getCount() {
        return count;
    }

    public Date getEndDate() {
        return endDate;
    }

    static RRule parse(ComponentPropertyImpl calprop) throws InvalidComponentException {
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
        int frequencyField;
        int interval = 1;
        String f = props.get("FREQ");
        RRule ret = null;
        if (f == null) {
            throw new InvalidComponentException(null, "FREQ must be present.");
        }
        switch (f) {
            case "SECONDLY":
                frequencyField = Calendar.SECOND;
                break;
            case "MINUTELY":
                frequencyField = Calendar.MINUTE;
                break;
            case "HOURLY":
                frequencyField = Calendar.HOUR_OF_DAY;
                break;
            case "DAILY":
                frequencyField = Calendar.DAY_OF_YEAR;
                break;
            case "WEEKLY":
                frequencyField = Calendar.WEEK_OF_YEAR;
                break;
            case "MONTHLY":
                frequencyField = Calendar.MONTH;
                break;
            case "YEARLY":
                frequencyField = Calendar.YEAR;
                break;
            default:
                throw new InvalidComponentException("FREQ value is invalid.");

        }
        String countValue = props.get("COUNT");
        if (countValue != null) {
            try {
                int count = Integer.parseInt(countValue);
                ret = new RRule(frequencyField, count);
            } catch (NumberFormatException nex) {
                throw new InvalidComponentException(nex);
            }
        }
        String untilValue = props.get("UNTIL");
        if (ret == null && untilValue != null) {
            Date until = IComponentUtilities.parseDate(untilValue, dValue);
            ret = new RRule(frequencyField, until);
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
        List<ByRuleLink> bylinks = new ArrayList<>();

        ret.createByWeekNoLink(props, bylinks);
        ret.createByDayLink(props, bylinks);
        ret.createBySetposLink(props, bylinks);
        
        //bySetPos must be last
        for (int i = 0; i < bylinks.size(); i++) {
            int next = i + 1;
            ByRuleLink parent = next < bylinks.size() ? bylinks.get(next) : null;
            bylinks.get(i).parent = parent;
        }
        ret.byRuleLinks = bylinks;
        return ret;
    }

    private boolean createByWeekNoLink(Map<String, String> props, List<ByRuleLink> toAdd) throws InvalidComponentException {
        String value = props.get("BYWEEKNO");
        if (value != null) {
            ByWeekNoLink ret = new ByWeekNoLink(value);
            toAdd.add(ret);
            return true;
        }
        return false;
    }

    private boolean createByDayLink(Map<String, String> props, List<ByRuleLink> toAdd) throws InvalidComponentException {
        String value = props.get("BYDAY");
        if (value != null) {
            AllWeeksInMonthSupportLink sl = new AllWeeksInMonthSupportLink();
            ByDayRuleLink ret = new ByDayRuleLink(value);
            toAdd.add(sl);
            toAdd.add(ret);
            return true;
        }
        return false;
    }

    private void createBySetposLink(Map<String, String> props, List<ByRuleLink> toAdd) throws InvalidComponentException {
        String value = props.get("BYSETPOS");
        if (value != null) {
            BySetposRuleLink ret = new BySetposRuleLink(value);
            toAdd.add(ret);
        }
    }

    private class AllWeeksInMonthSupportLink extends ByRuleLink<ByRuleLink.Num> {

        private AllWeeksInMonthSupportLink() throws InvalidComponentException {
            super(Calendar.WEEK_OF_MONTH, IntStream.range(1, 5).mapToObj(Integer::toString).collect(Collectors.joining(",")));
            for (String s : byRules) {
                values.add(new ByRuleLink.Num(Integer.parseInt(s)));
            }
        }
    }

    private class ByWeekNoLink extends ByRuleLink<ByRuleLink.Num> {

        private ByWeekNoLink(String value) throws InvalidComponentException {
            super(Calendar.WEEK_OF_YEAR, value);
            for (String s : byRules) {
                values.add(new ByRuleLink.Num(Integer.parseInt(s)));
            }
        }

        private ByWeekNoLink() throws InvalidComponentException {
            super(Calendar.WEEK_OF_YEAR, IntStream.range(1, 53).mapToObj(Integer::toString).collect(Collectors.joining(",")));
            for (String s : byRules) {
                values.add(new ByRuleLink.Num(Integer.parseInt(s)));
            }
        }
    }

    private class ByDayRuleLink extends ByRuleLink<ByDayRuleLink.ByDayNum> {

        private ByDayRuleLink(String value) throws InvalidComponentException {
            super(Calendar.DAY_OF_WEEK, value);
            for (String s : byRules) {
                values.add(checkAndParse(s));
            }
        }

        private ByDayRuleLink() throws InvalidComponentException {
            super(Calendar.DAY_OF_WEEK, "1,2,3,4,5,6,7");
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
            int calendarDay;
            switch (dayValue) {
                case "SU":
                    calendarDay = Calendar.SUNDAY;
                    break;
                case "MO":
                    calendarDay = Calendar.MONDAY;
                    break;
                case "TU":
                    calendarDay = Calendar.TUESDAY;
                    break;
                case "WE":
                    calendarDay = Calendar.WEDNESDAY;
                    break;
                case "TH":
                    calendarDay = Calendar.THURSDAY;
                    break;
                case "FR":
                    calendarDay = Calendar.FRIDAY;
                    break;
                case "SA":
                    calendarDay = Calendar.SATURDAY;
                    break;
                default:
                    throw new RuntimeException();
            }
            ByDayNum ret = new ByDayNum(calendarDay);
            if (l > 2) {
                int f = getFrequencyField();
                if (f != Calendar.MONTH && f != Calendar.YEAR) {
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
        protected boolean applyValue(Calendar seed, ByDayRuleLink.ByDayNum num) {
            boolean ret = super.applyValue(seed, num);
            if (ret) {
                LocalDateTime ldt = LocalDateTime.ofInstant(seed.toInstant(), ZoneId.systemDefault());
                final Integer f = num.filter;
                if (f != null) {
                    switch (getFrequencyField()) {
                        case Calendar.MONTH:
                            LocalDateTime query = ldt.with(TemporalAdjusters.dayOfWeekInMonth(f, ConvUtil.convert(num.num)));
                            return ldt.equals(query);
                        case Calendar.YEAR:
                            LocalDateTime query2 = ldt.with(ConvUtil.dayOfWeekInYear(f, ConvUtil.convert(num.num)));
                            return ldt.equals(query2);
                    }
                }
            }
            return ret;
        }

        private class ByDayNum extends Num {

            private Integer filter = null;

            private ByDayNum(int value) {
                super(value);
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
        void apply(Calendar seed, final TreeSet<Date> dates) {
            if (parent != null) {
                throw new IllegalStateException();
            }
            synchronized (dates) {
                dates.add(seed.getTime());
            }
        }

        @Override
        protected void finish(final TreeSet<Date> dates) {
            if (parent != null) {
                throw new IllegalStateException();
            }
            final Set<Date> toRemove = new HashSet<>();
            synchronized (dates) {
                final int size = dates.size();
                Integer pos = 0;
                for (Date d : dates) {
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
        private final int calendarField;//Calendar.field....
        protected final String[] byRules;
        protected List<N> values = new ArrayList<>();

        protected ByRuleLink(int calendarField, String value) throws InvalidComponentException {
            this.calendarField = calendarField;
            this.byRules = value.split(",");
        }

        protected ByRuleLink(String value) throws InvalidComponentException {
            this(-1, value);
        }

        protected List<N> getValues() {
            return values;
        }

        void apply(Calendar seed, final TreeSet<Date> dates) {
            List<N> vs = getValues();
            List<Calendar> resolvedByMe = new ArrayList<>();
            for (N iv : vs) {
                if (applyValue(seed, iv)) {
                    resolvedByMe.add(seed);
                }
                seed = (Calendar) seed.clone();
            }
            //resolve yourself
            resolvedByMe.stream().forEach(c -> {
                if (parent != null) {
                    parent.apply(c, dates);
                } else {
                    synchronized (dates) {
                        dates.add(c.getTime());
                    }
                }
            });
        }

        protected boolean applyValue(Calendar seed, N iv) {
            if (calendarField != -1) {
                seed.set(calendarField, iv.num);
                return true;
            }
            return false;
        }

        protected void finish(final TreeSet<Date> dates) {
            if (parent != null) {
                parent.finish(dates);
            }
        }

        class Num {

            final int num;

            protected Num(int value) {
                this.num = value;
            }
        }
    }

}
