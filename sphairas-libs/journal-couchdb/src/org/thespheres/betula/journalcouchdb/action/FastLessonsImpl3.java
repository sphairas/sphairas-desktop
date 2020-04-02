/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journalcouchdb.action;

import java.time.LocalDate;
import org.thespheres.betula.journalcouchdb.model.TargetStudent;
import org.thespheres.betula.journalcouchdb.model.TimeDoc2;
import org.thespheres.betula.journalcouchdb.model.TargetDocSupport;
import org.thespheres.betula.journalcouchdb.model.TargetDoc;
import org.thespheres.betula.journalcouchdb.model.TimeDoc2Support;
import org.thespheres.betula.journalcouchdb.model.TimeRecord;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.ektorp.CouchDbConnector;
import org.netbeans.api.progress.ProgressHandle;
import org.thespheres.betula.RecordId;
import org.thespheres.betula.Student;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.Unit;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.journalcouchdb.model.TimeDoc2.Journal;
import org.thespheres.betula.services.scheme.spi.LessonId;
import org.thespheres.ical.CalendarComponent;
import org.thespheres.ical.CalendarComponentProperty;
import org.thespheres.ical.InvalidComponentException;
import org.thespheres.ical.util.IComponentUtilities;

/**
 *
 * @author boris.heithecker
 */
class FastLessonsImpl3 {

    private final Unit unit2;
    final Map<DocumentId, TargetDoc> targets = new HashMap<>();
    private final Map<RecordId, ReturnRecord> retList = new HashMap<>();
    TargetDocSupport targetsupp;
    TimeDoc2Support timesupp;
    private final ArrayList<CalendarComponent> inflated;
    private final int numRawComponent;

    FastLessonsImpl3(final CouchDbConnector db, final LocalDate start, final LocalDate end, final List<CalendarComponent> ical, final Unit unit) throws InvalidComponentException {
        this.unit2 = unit;
        targetsupp = new TargetDocSupport(db);
        timesupp = new TimeDoc2Support(db);
        this.inflated = new ArrayList<>();
        this.numRawComponent = ical.size();
        final Date de = Date.from(end.plusDays(1l).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        for (final CalendarComponent cc : ical) {
            for (CalendarComponent inf : cc.inflate(de).getComponents()) {
                final LocalDateTime d = IComponentUtilities.parseLocalDateTimeProperty(inf, CalendarComponentProperty.DTSTART);
                if (d.isBefore(start.atStartOfDay())) {
                    continue;
                }
                inflated.add(inf);
            }
        }
    }

    Map<RecordId, ReturnRecord> run(ProgressHandle progress, AtomicBoolean cancelled) throws InvalidComponentException {
        progress.switchToDeterminate(inflated.size() + numRawComponent + 1);
        int p;
        for (p = 0; p < inflated.size(); p++) {
            final CalendarComponent cc = inflated.get(p);
            createFastLesson(cc);
            progress.progress(p);
        }
        final int[] i = new int[]{p};
        targets.forEach((d, tdoc) -> {
            if (!cancelled.get()) {
                targetsupp.update(tdoc);
                progress.progress(i[0]++);
            }
        });
        return retList;
    }

    private void createFastLesson(final CalendarComponent cc) throws InvalidComponentException {
        LocalDateTime dtStart = IComponentUtilities.parseLocalDateTimeProperty(cc, CalendarComponentProperty.DTSTART);
        LocalDateTime dtEnd = IComponentUtilities.parseLocalDateTimeProperty(cc, CalendarComponentProperty.DTEND);
        CalendarComponentProperty periodProp = cc.getAnyProperty("X-PERIOD");
        if (dtStart == null || dtEnd == null || periodProp == null) {
            throw new InvalidComponentException("DTSTART, DTEND and X-PERIOD cannot be null");
        }
        int period = 0;
        try {
            period = Integer.parseInt(periodProp.getValue());
        } catch (NumberFormatException nex) {
            throw new InvalidComponentException(nex);
        }
        final LessonId extracted = extractLessonIdFromCalendarComponent(cc);
        //Workaround
        final DocumentId toDid = new DocumentId(extracted.getAuthority(), extracted.getId(), DocumentId.Version.LATEST);
        final DocumentId targetId = CouchDBMappings.getInstance().mapTarget(toDid);
        TargetDoc tdoc = targets.get(targetId);
        if (tdoc == null) {
            tdoc = createTarget(targetId, cc);
            targets.put(targetId, tdoc);
        }
        final TimeDoc2 ret = createTime(tdoc, dtStart, dtEnd, period, cc);//Maybe catch DBAccessException in case only one document is corrupted.
        if (!tdoc.getTimes().contains(ret)) {
            tdoc.getTimes().add(ret);
        }
    }

    private TimeDoc2 createTime(TargetDoc tdoc, LocalDateTime dtStart, LocalDateTime dtEnd, int period, CalendarComponent cc) {
        final List<TimeRecord> rec = unit2.getStudents().stream()
                .map(Student::getStudentId)
                .map(TimeRecord::new)
                .collect(Collectors.toList());
        TimeDoc2 ret;
        boolean update = false;
        if (timesupp.contains(TimeDoc2.createId(dtStart))) {
            ret = timesupp.get(TimeDoc2.createId(dtStart));
            final RecordId rid = ret.getRecordId();
            final Set<StudentId> present = Arrays.stream(ret.getRecords())
                    .peek(tr -> retList.computeIfAbsent(rid, r -> new ReturnRecord()).list.add(tr))
                    .map(TimeRecord::getStudent)
                    .collect(Collectors.toSet());
            if (ret.getJournal() != null) {
                retList.computeIfAbsent(rid, r -> new ReturnRecord()).journal = ret.getJournal();
            }
            final List<TimeRecord> trupdate = rec.stream()
                    .filter(r -> !present.contains(r.getStudent()))
                    .collect(Collectors.toList());
            if (!trupdate.isEmpty()) {
                ret.addRecords(trupdate);
                update = true;
            }
            if (!Objects.equals(dtEnd, ret.getEnd())) {
                ret.setEnd(dtEnd);
                update = true;
            }
            if (period != ret.getPeriod()) {
                ret.setPeriod(period);
                update = true;
            }
            if (!Objects.equals(ret.getTarget(), tdoc.getDocumentId())) {
                ret.setTarget(tdoc);
                update = true;
            }
        } else {
            ret = new TimeDoc2(tdoc, dtStart, dtEnd, period, 0, rec);
            timesupp.add(ret);
            ret = timesupp.get(ret.getId());
        }
        final CalendarComponentProperty summary = cc.getAnyProperty(CalendarComponentProperty.SUMMARY);
        if (summary != null) {
            final boolean gen = summary.getAnyParameter("x-generated-summary").map(sp -> sp.equals("true")).orElse(Boolean.FALSE);
            final String text = summary.getValue();
            //TODO map name !!!!!!!!
            if (!gen || (tdoc.getDisplay() != null && !tdoc.getDisplay().equals(text))) {
                ret.setDisplay(text);
                update = true;
            }
        }
        final CalendarComponentProperty location = cc.getAnyProperty(CalendarComponentProperty.LOCATION);
        if (location != null) {
            ret.setLocation(location.getValue());
            update = true;
        }
        if (update) {
            timesupp.update(ret); //Ausgekommiert nur für debug-Zwecke!!, nicht überschreiben!!!!
        }
        return timesupp.get(ret.getId());
    }

    private TargetDoc createTarget(final DocumentId targetBase, final CalendarComponent cc) throws InvalidComponentException {
        TargetDoc ret;
        if (targetsupp.contains(TargetDoc.createId(targetBase))) {
            ret = targetsupp.get(TargetDoc.createId(targetBase));
        } else {
            final TargetDoc td = new TargetDoc(targetBase);
            targetsupp.add(td);
            ret = targetsupp.get(td.getId());
        }
        final UnitId extracted = extractUnitIdFromCalendarComponent(cc);
        final UnitId uid = CouchDBMappings.getInstance().mapUnit(extracted);
        ret.setUnit(uid);
        ret.setDisplay(CouchDBMappings.getInstance().getDisplayName(unit2));
        final List<TargetStudent> us = unit2.getStudents().stream()
                .map(FastLessonsImpl3::createTargetStudent)
                .collect(Collectors.toList());
        final List<TargetStudent> keep = Arrays.stream(ret.getStudents())
                .filter(s -> !us.contains(s))
                .collect(Collectors.toList());
        final TargetStudent[] ts = Stream.concat(us.stream(), keep.stream())
                .sorted(Comparator.comparingLong(s -> s.getStudent().getId()))
                .toArray(TargetStudent[]::new);
        ret.setStudents(ts);
        targetsupp.update(ret);
        return targetsupp.get(ret.getId());
    }

    private static TargetStudent createTargetStudent(Student s) {
        return new TargetStudent(s.getStudentId(), s.getSurname(), s.getGivenNames());
    }

    public static UnitId extractUnitIdFromCalendarComponent(CalendarComponent cc) throws InvalidComponentException {
        cc.validate();
        final CalendarComponentProperty unitProp = cc.getAnyProperty("X-UNIT");
        Optional<UnitId> map = Optional.empty();
        if (unitProp != null) {
            map = unitProp.getAnyParameter("x-authority").map(au -> new UnitId(au, unitProp.getValue()));

        }
        return map.orElseThrow(() -> new InvalidComponentException("No unit authority."));
    }

    public static LessonId extractLessonIdFromCalendarComponent(CalendarComponent cc) throws InvalidComponentException {
        cc.validate();
        final CalendarComponentProperty lessonProp = cc.getAnyProperty("X-LESSON");
        Optional<LessonId> map = Optional.empty();
        if (lessonProp != null) {
            map = lessonProp.getAnyParameter("x-authority").map(au -> new LessonId(au, lessonProp.getValue()));
        }
        return map.orElseThrow(() -> new InvalidComponentException("No lesson authority."));
    }

    static class ReturnRecord {

        List<TimeRecord> list = new ArrayList<>();
        Journal journal;
    }
}
