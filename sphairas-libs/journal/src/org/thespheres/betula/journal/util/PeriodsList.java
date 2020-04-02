/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.netbeans.api.project.Project;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.thespheres.betula.RecordId;
import org.thespheres.betula.Unit;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.icalendar.util.CalendarUtilities;
import org.thespheres.betula.journal.model.EditableJournal;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.scheme.spi.LessonId;
import org.thespheres.betula.services.scheme.spi.Period;
import org.thespheres.betula.services.scheme.spi.PeriodId;
import org.thespheres.betula.services.util.UnitTarget;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.ical.CalendarComponent;
import org.thespheres.ical.CalendarComponentProperty;
import org.thespheres.ical.InvalidComponentException;
import org.thespheres.ical.util.IComponentUtilities;

/**
 *
 * @author boris.heithecker
 */
public class PeriodsList implements LookupListener, PropertyChangeListener {

    private final LessonId lesson;
    private final UnitId unit;
//    private final DocumentId targetBase;
    private final EditableJournal ejour;
    private final List<Period> periods = new ArrayList<>();
    private final Lookup.Result<CalendarComponent> result;

    @SuppressWarnings({"LeakingThisInConstructor"})
    private PeriodsList(Lookup.Result<CalendarComponent> cc, UnitId unit, LessonId lesson, EditableJournal ejour) {
        this.result = cc;
        this.unit = unit;
        this.lesson = lesson;
        this.ejour = ejour;
        this.result.addLookupListener(this);
        this.ejour.addPropertyChangeListener(this);
    }

    public static PeriodsList create(Project context, EditableJournal ejour) {
        final Unit unit = context.getLookup().lookup(Unit.class);
        final LocalProperties lp = context.getLookup().lookup(LocalProperties.class);
        if (lp != null) {
            final DocumentId target = UnitTarget.parseTargetBase(lp);
            if (unit != null && target != null) {
                final LessonId lesson = new LessonId(target.getAuthority(), target.getId());
                Lookup.Result<CalendarComponent> r = context.getLookup().lookupResult(CalendarComponent.class);
                PeriodsList processor = new PeriodsList(r, unit.getUnitId(), lesson, ejour);
                processor.update();
                return processor;
            }
        }
        return null;
    }

    public List<Period> getPeriods() {
        return Collections.unmodifiableList(periods);
    }

    public Period findForRecordId(RecordId rec) {
        synchronized (periods) {
            return periods.stream()
                    .filter(p -> p.resolveStart().equals(rec.getLocalDateTime()))
                    .collect(CollectionUtil.singleOrNull());
        }
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        update();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (EditableJournal.PROP_JOURNAL_END.equals(evt.getPropertyName())) {
            update();
        }
    }

    protected void update() {
        try {
            runImpl();
        } catch (InvalidComponentException ex) {
        }
    }

    private void runImpl() throws InvalidComponentException {

        List<CalendarComponent> cc = result.allInstances().stream()
                .filter(c -> CalendarComponent.VEVENT.equals(c.getName()))
                .filter(c -> c.getAnyPropertyValue("CATEGORIES")
                .map(v -> v.split(","))
                .map(a -> Arrays.stream(a).anyMatch("regular"::equals))
                .orElse(Boolean.FALSE))
                .filter(c -> Objects.equals(CalendarUtilities.extractLessonIdFromCalendarComponent(c), lesson))
                .filter(c -> Objects.equals(CalendarUtilities.extractUnitIdFromCalendarComponent(c), unit))
                //                .filter(c -> Objects.equals(CalendarUtilities.extractTargetDocumentIdFromCalendarComponent(c), targetBase))
                .collect(Collectors.toList());

        final Date limit = getLimit();

        if (limit == null) {
            return;
        }
        synchronized (periods) {
            periods.clear();
            for (CalendarComponent c : cc) {

                for (CalendarComponent inf : c.inflate(limit).getComponents()) {
                    createFastLesson(inf);
                }
            }
        }
    }

    private Date getLimit() {
        final LocalDate je = ejour.getJournalEnd();
        return je == null ? null : Date.from(je.plusDays(1l).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    private void createFastLesson(CalendarComponent cc) throws InvalidComponentException {
        LocalDateTime dtStart = IComponentUtilities.parseLocalDateTimeProperty(cc, CalendarComponentProperty.DTSTART);
        LocalDateTime dtEnd = IComponentUtilities.parseLocalDateTimeProperty(cc, CalendarComponentProperty.DTEND);
        PeriodId periodProp = CalendarUtilities.extractPeriodIdFromCalendarComponent(cc);
        if (dtStart != null && dtEnd != null && periodProp != null) {
            PeriodImpl pi = new PeriodImpl(dtStart, dtEnd, periodProp);
            periods.add(pi);
        }
    }

}
