/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis.impl;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.openide.util.NbBundle;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.gpuntis.ImportedLesson;
import org.thespheres.betula.gpuntis.UntisImportConfiguration;
import org.thespheres.betula.gpuntis.xml.General;
import org.thespheres.betula.gpuntis.xml.Lesson;
import org.thespheres.betula.gpuntis.xml.Time;
import org.thespheres.betula.services.calendar.LessonData;
import org.thespheres.betula.services.calendar.VendorData;
import org.thespheres.betula.services.calendar.LessonTimeData.ExWeeks;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.calendar.LessonTimeData;
import org.thespheres.betula.services.scheme.spi.LessonId;
import org.thespheres.betula.services.scheme.spi.PeriodId;
import org.thespheres.betula.services.ui.util.dav.URLs;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.utilities.AbstractUpdater;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"StudenplanUpdater.message.start=Starte Stundenplan-Import...",
    "StudenplanUpdater.message.finish=Es wurden {0} Studenplan/Studenpläne in {1} ms importiert.",
    "StudenplanUpdater.message.noImport=\"{0}\" kann nicht importiert werden.",
    "StudenplanUpdater.message.http.status=\"{0}\" hat den Status-Code {1} (\"{2}\") zurückgegeben.",
    "StudenplanUpdater.message.kupplung.workaround=\"{0}\" ist eine Verdopplung (clone) mit der Verdopplungsnummer (clone id) {1}; der Unterricht wird als Untis-Kupplung {2} gespeichert."})
public class StudenplanUpdater extends AbstractUpdater<ImportedLesson> {
    
    private final UntisImportConfiguration config;
    
    public StudenplanUpdater(final UntisImportConfiguration config, final Set<ImportedLesson> selected) {
        super(selected.stream().toArray(ImportedLesson[]::new));
        this.config = config;
    }
    
    @Override
    public void run() {
        final String msg = NbBundle.getMessage(StudenplanUpdater.class, "StudenplanUpdater.message.start");
        ImportUtil.getIO().getOut().println(msg);
        int[] numImport = new int[]{0};
        long timeStart = System.currentTimeMillis();
        
        final DocumentId calendar = null; //ub.getCurrentCalendarId();

        Arrays.stream(items)
                .filter(ImportedLesson::doImportTimetable)
                .filter(il -> doUpdate(calendar, il))
                .forEach(il -> ++numImport[0]);
        final long dur = System.currentTimeMillis() - timeStart;
        final String msg2 = NbBundle.getMessage(StudenplanUpdater.class, "StudenplanUpdater.message.finish", numImport[0], dur);
        ImportUtil.getIO().getOut().println(msg2);
    }
    
    boolean doUpdate(final DocumentId calendar, final ImportedLesson imp) {
        final Lesson lesson = imp.getLesson();
        int untisLessonKopplung = imp.getUntisKopplung();
        //
        final int cloneId = imp.id();
        //If it's clone, 
        if (cloneId != 0) {
            //Save the clone a kopplung
            //Workaround to prevent overwriting existing lesson/kopplung mappings in the database
            untisLessonKopplung += (100 * cloneId);
            final String msg = NbBundle.getMessage(StudenplanUpdater.class, "StudenplanUpdater.message.kupplung.workaround", imp.getSourceNodeLabel(), cloneId, untisLessonKopplung);
            ImportUtil.getIO().getOut().println(msg);
        }
        //
        final Signee signee = imp.getSignee();
        final UnitId unit = imp.getUnitId();
        final Marker[] fach = imp.getSubjectMarkers();
        if (signee == null || unit == null || fach == null) {
            String msg2 = NbBundle.getMessage(StudenplanUpdater.class, "StudenplanUpdater.message.noImport", imp.getSourceNodeLabel());
            ImportUtil.getIO().getErr().println(msg2);
            return false;
        }
        final LessonTimeData[] times = imp.getTimes();
        final String teacherId = lesson.getLessonTeacher().getId().substring(3);//remove TR_
        final String grid = lesson.getTimegrid();
        final VendorData vData = new VendorData(imp.getUntisLessonId(), untisLessonKopplung, teacherId);
        Arrays.stream(items)
                .map(ImportedLesson::getUntisLessonId)
                .forEach(vData.getJoinedVendorLessons()::add);
        final LessonId lid = new LessonId(imp.getTargetDocumentIdBase().getAuthority(), imp.getTargetDocumentIdBase().getId());
        final LessonData ld = new LessonData(lid, unit, signee, fach, lesson.getEffectiveBeginDate(), lesson.getEffectiveEndDate(), grid, times, vData, lesson.getText());
        
        final String provider = config.getWebServiceProvider().getInfo().getURL();
        final WebProvider wp = WebProvider.find(provider, WebProvider.class);
        final String base = URLs.adminBase(LocalProperties.find(provider));
        final String url = base + "calendar/resource"; ///untis/ignored/ignored/lesson-data";

        final Client client = ClientBuilder.newBuilder()
                .sslContext(((WebProvider.SSL) wp).getSSLContext())
                //                .hostnameVerifier(arg0)
                .build();
        
        final WebTarget target = client.target(url);
        
        final Response resp;
//        final String authority;
//        try {
//            authority = URLEncoder.encode(config.getAuthority(), "utf-8");
//        } catch (UnsupportedEncodingException ex) {
//            throw new RuntimeException(ex);
//        }
//        final String id = "timetable";
        try {
            resp = target.path("lessons")
                    //                    .path(authority)
                    //                    .path(id)
                    .path("lesson")
                    //                    .queryParam("calendar-authority", authority)
                    //                    .queryParam("calendar-id", id)
                    //                    .queryParam("calendar-version", DocumentId.Version.LATEST.getVersion())
                    .request()
                    .put(Entity.entity(ld, MediaType.APPLICATION_XML), Response.class);
        } catch (final Exception e) {
            e.printStackTrace(ImportUtil.getIO().getErr());
            return false;
        }
        final Response.StatusType statusInfo = resp.getStatusInfo();
        if (statusInfo.getStatusCode() != Response.Status.OK.getStatusCode()) {
            final String msg2 = NbBundle.getMessage(StudenplanUpdater.class, "StudenplanUpdater.message.http.status", imp.getSourceNodeLabel(), statusInfo.getStatusCode(), statusInfo.getReasonPhrase());
            ImportUtil.getIO().getErr().println(msg2);
            return false;
        }
        return true;
    }
    
    public static String untisAuthority(final General general) {
        return "gpuntis/" + Integer.toString(general.getSchoolnumber());
    }
    
    public static LessonTimeData[] createTimes(final Lesson lesson, final General general) {
        return lesson.getTimes().stream()
                .map(t -> StudenplanUpdater.createTime(lesson, general, t))
                .filter(Objects::nonNull)
                .toArray(LessonTimeData[]::new);
    }
    
    private static LessonTimeData createTime(final Lesson lesson, final General general, final Time t) {
        if (t.getPeriod() == 0) {
            return null;
        }
//        LocalDateTime ldt = LocalDateTime.from(general.getSchoolyearbegindate());

        final Calendar cal = Calendar.getInstance(Locale.GERMANY);
        final String occ = lesson.getOccurence();
//        cal.setTime(lesson.getEffectiveBeginDate());
//        cal.setFirstDayOfWeek(Calendar.MONDAY);
//        cal.setMinimalDaysInFirstWeek(1);
        final Date d = Date.from(general.getSchoolyearbegindate().atStartOfDay(ZoneId.systemDefault()).toInstant());
        cal.setTime(d);
        final List<ExWeeks> exWeeks = new ArrayList<>();
        for (char c : occ.toCharArray()) {
            int wday = cal.get(Calendar.DAY_OF_WEEK);
            if (wday != Calendar.SATURDAY && wday != Calendar.SUNDAY && ('F' == c || '0' == c) && wday == t.getDay() + 1) {
                int year = cal.get(Calendar.YEAR);
                int week = cal.get(Calendar.WEEK_OF_YEAR);
                if (cal.get(Calendar.MONTH) == Calendar.JANUARY && week == 53) {
                    --year;
                }
                final int y = year;
                ExWeeks ew = exWeeks.stream().filter(e -> e.getYear() == y).collect(CollectionUtil.requireSingleOrNull());
                int[] sn;
                if (ew == null) {
                    sn = new int[]{week};
                    exWeeks.add(new ExWeeks(y, sn));
                } else {
                    sn = Arrays.copyOf(ew.getExWeeks(), ew.getExWeeks().length + 1);
                    sn[sn.length - 1] = week;
                    ew.setExWeeks(sn);
                }
//                exWeeks.put(year, sn);
            }
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
//        d = Date.from(lesson.getEffectiveBeginDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
//        cal.setTime(d);
//        final int firstOccurenceDay = cal.get(Calendar.DAY_OF_WEEK);
//        cal.set(Calendar.DAY_OF_WEEK, t.getDay() + 1);
//        if (firstOccurenceDay > t.getDay() + 1) {
//            cal.add(Calendar.WEEK_OF_YEAR, 1);
//        }
//        cal.set(Calendar.HOUR_OF_DAY, t.getStarttime().getHour());
//        cal.set(Calendar.MINUTE, t.getStarttime().getMinute());
//        final Date start = cal.getTime();
//        cal.set(Calendar.HOUR_OF_DAY, t.getEndtime().getHour());
//        cal.set(Calendar.MINUTE, t.getEndtime().getMinute());
//        final Date end = cal.getTime();
        final String room = t.getAssignedRoom() != null ? t.getAssignedRoom().getId() : null;
        //TODO: add lesson.getEffectiveEndDate() || general.schoolYearEnd() ==> Calendar:: RRULE unitl
        final PeriodId period = new PeriodId(untisAuthority(general), t.getPeriod(), PeriodId.Version.UNSPECIFIED);
        return new LessonTimeData(t.getStarttime(), t.getEndtime(), DayOfWeek.of(t.getDay()), lesson.getTimegrid(), period, exWeeks.toArray(new ExWeeks[exWeeks.size()]), room);
    }
    
}
