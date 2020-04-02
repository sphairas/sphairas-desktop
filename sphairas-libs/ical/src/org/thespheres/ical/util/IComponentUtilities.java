/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.ical.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.thespheres.betula.TermId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.ical.CalendarComponent;
import org.thespheres.ical.CalendarComponentProperty;
import org.thespheres.ical.ComponentProperty;
import org.thespheres.ical.IComponent;
import org.thespheres.ical.InvalidComponentException;
import org.thespheres.ical.Parameter;

/**
 *
 * @author boris.heithecker
 */
public class IComponentUtilities {

    public static final DateFormat DATE_TIME = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
    public static final SimpleDateFormat DATE = new SimpleDateFormat("yyyyMMdd");
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss['Z']");

    public static String parseStringProperty(CalendarComponent prop, String propertyName) throws InvalidComponentException {
        CalendarComponentProperty p = prop.getAnyProperty(propertyName);
        return p != null ? p.getValue() : null;
    }

    public static Set<String> parseCategories(CalendarComponent prop) {
        return prop.getProperties(CalendarComponentProperty.CATEGORIES).stream()
                .flatMap(p -> Arrays.stream(p.getValue().trim().split(",")))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    public static Date parseDateProperty(CalendarComponent prop, String propertyName) throws InvalidComponentException {
        CalendarComponentProperty p = prop.getAnyProperty(propertyName);
        return p != null ? parseDate(p.getValue(), p.getAnyParameter("VALUE").orElse(null)) : null;
    }

    public static Date parseDateProperty(CalendarComponentProperty prop) throws InvalidComponentException {
        return parseDate(prop.getValue(), prop.getAnyParameter("VALUE").orElse(null));
    }

    public static Date parseDate(String value, String valueParameter) throws InvalidComponentException {
        DateFormat df = DATE_TIME;
        if (valueParameter != null) {
            if ("DATE".equals(valueParameter)) {
                df = DATE;
            } else if (!"DATE-TIME".equals(valueParameter)) {
                throw new InvalidComponentException(null, "Unsupported date value.");
            }
        }
        try {
            return df.parse(value);
        } catch (ParseException | NumberFormatException ex) {
            throw new InvalidComponentException(null, ex);
        }
    }

    public static LocalDateTime parseLocalDateTimeProperty(CalendarComponent prop, String propertyName) throws InvalidComponentException {
        CalendarComponentProperty p = prop.getAnyProperty(propertyName);
        return p != null ? parseLocalDateTime(p.getValue(), p.getAnyParameter("VALUE").orElse(null)) : null;
    }

    public static LocalDateTime parseLocalDateTimeProperty(CalendarComponentProperty prop) throws InvalidComponentException {
        return parseLocalDateTime(prop.getValue(), prop.getAnyParameter("VALUE").orElse(null));
    }

    public static LocalDateTime parseLocalDateTime(String value, String valueParameter) throws InvalidComponentException {
        if (valueParameter != null && "DATE".equals(valueParameter)) {
            return LocalDate.parse(value, DATE_FORMATTER).atStartOfDay();
        } else {
            return LocalDateTime.parse(value, DATETIME_FORMATTER);
        }
    }

    public static boolean isDateFormat(CalendarComponentProperty prop) throws InvalidComponentException {
        String valueParameter = prop.getAnyParameter("VALUE").orElse(null);
        if (valueParameter != null) {
            if ("DATE".equals(valueParameter)) {
                return true;
            } else if (!"DATE-TIME".equals(valueParameter)) {
                throw new InvalidComponentException(null, "Unsupported date value.");
            }
        }
        return false;
    }

    public static UnitId parseUnitId(final CalendarComponent cc) {
        return cc.getProperties("X-UNIT").stream()
                .filter(ccp -> ccp.getAnyParameter("x-authority").isPresent())
                .map(ccp -> new UnitId(ccp.getAnyParameter("x-authority").get(), ccp.getValue()))
                .collect(CollectionUtil.singleOrNull());
    }

    public static DocumentId parseDocumentId(final CalendarComponent cc) {
        return cc.getProperties("X-DOCUMENT").stream()
                .filter(ccp -> ccp.getAnyParameter("x-authority").isPresent() && ccp.getAnyParameter("x-version").isPresent())
                .map(ccp -> new DocumentId(ccp.getAnyParameter("x-authority").get(), ccp.getValue(), DocumentId.Version.parse(ccp.getAnyParameter("x-version").get())))
                .collect(CollectionUtil.singleOrNull());
    }

    public static List<DocumentId> parseDocumentIds(final CalendarComponent cc) {
        return cc.getProperties("X-DOCUMENT").stream()
                .filter(ccp -> ccp.getAnyParameter("x-authority").isPresent() && ccp.getAnyParameter("x-version").isPresent())
                .map(ccp -> new DocumentId(ccp.getAnyParameter("x-authority").get(), ccp.getValue(), DocumentId.Version.parse(ccp.getAnyParameter("x-version").get())))
                .collect(Collectors.toList());
    }

    public static TermId parseTermId(final CalendarComponent cc) {
        return cc.getProperties("X-TERM").stream()
                .filter(ccp -> ccp.getAnyParameter("x-authority").isPresent())
                .map(ccp -> new TermId(ccp.getAnyParameter("x-authority").get(), Integer.parseInt(ccp.getValue())))
                .collect(CollectionUtil.singleOrNull());
    }

    public static <P extends ComponentProperty> boolean equals(IComponent<P> c1, IComponent<P> c2, String[] ignoreProperties) {
        if (c1 == c2) {
            return true;
        }
        if (c1 == null || c2 == null) {
            return false;
        }
        final List<P> l1 = c1.getProperties().stream()
                .filter(c -> ignoreProperties == null || Arrays.stream(ignoreProperties).noneMatch(c.getName()::equals))
                .collect(Collectors.toList());
        final List<P> l2 = c2.getProperties().stream()
                .filter(c -> ignoreProperties == null || Arrays.stream(ignoreProperties).noneMatch(c.getName()::equals))
                .collect(Collectors.toList());
        if (l1.size() != l2.size()) {
            return false;
        }
        final int s = l1.size();
        final boolean[] paired = new boolean[s];
        prop1:
        for (int i = 0; i < s; i++) {
            final P p1 = l1.get(i);
            for (int j = 0; j < s; j++) {
                if (paired[j]) {
                    continue;
                }
                final P p2 = l2.get(j);
                if (equals(p1, p2)) {
                    paired[j] = true;
                    continue prop1;
                }
            }
            return false;
        }
        return true;
    }

    public static boolean equals(ComponentProperty cp1, ComponentProperty cp2) {
        if (cp1 == cp2) {
            return true;
        }
        if (cp1 == null || cp2 == null) {
            return false;
        }
        final boolean eq = cp1.getName().equals(cp2.getName()) && cp1.getValue().equals(cp2.getValue());
        if (!eq) {
            return false;
        }
        final List<Parameter> l1 = cp1.getParameters();
        final List<Parameter> l2 = cp1.getParameters();
        if (l1.size() != l2.size()) {
            return false;
        }
        final int s = l1.size();
        final boolean[] paired = new boolean[s];
        param1:
        for (int i = 0; i < s; i++) {
            final Parameter p1 = l1.get(i);
            for (int j = 0; j < s; j++) {
                if (paired[j]) {
                    continue;
                }
                final Parameter p2 = l2.get(j);
                if (p2.getName().equals(p1.getName()) && p2.getValue().equals(p1.getValue())) {
                    paired[j] = true;
                    continue param1;
                }
            }
            return false;
        }
        return true;
    }
}
