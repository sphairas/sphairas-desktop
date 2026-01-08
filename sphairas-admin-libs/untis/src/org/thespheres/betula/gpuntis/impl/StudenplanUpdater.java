/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.openide.util.NbBundle;
import org.thespheres.betula.gpuntis.ImportedLesson;
import org.thespheres.betula.gpuntis.xml.General;
import org.thespheres.betula.gpuntis.xml.Lesson;
import org.thespheres.betula.gpuntis.xml.Time;
import org.thespheres.betula.services.calendar.LessonTimeData;
import org.thespheres.betula.services.calendar.VendorData;
import org.thespheres.betula.services.scheme.spi.PeriodId;
import org.thespheres.betula.xmlimport.ImportUtil;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"StudenplanUpdater.message.kupplung.workaround=\"{0}\" ist eine Verdopplung (clone) mit der Verdopplungsnummer (clone id) {1}; der Unterricht wird als Untis-Kupplung {2} gespeichert."})
public class StudenplanUpdater {

    public static String untisAuthority(final General general) {
        return "gpuntis/" + Integer.toString(general.getSchoolnumber());
    }

    public static LessonTimeData[] createTimes(final Lesson lesson, final General general, final ImportedLesson il) {
        return lesson.getTimes().stream()
                .map(t -> StudenplanUpdater.createTime(lesson, general, t, il))
                .filter(Objects::nonNull)
                .toArray(LessonTimeData[]::new);
    }

    private static LessonTimeData createTime(final Lesson lesson, final General general, final Time t, final ImportedLesson il) {
        if (t.getPeriod() == 0) {
            return null;
        }
        final DayOfWeek day = DayOfWeek.of(t.getDay());
        final PeriodId period = new PeriodId(untisAuthority(general), t.getPeriod(), PeriodId.Version.UNSPECIFIED);
        final LessonTimeData ret = new LessonTimeData(t.getStarttime(), t.getEndtime(), day, period);
        final String occ = lesson.getOccurence();
        LocalDate ld = general.getSchoolyearbegindate();
        final List<LocalDate> exDates = new ArrayList<>();
        for (char c : occ.toCharArray()) {
            final DayOfWeek dow = ld.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY && ('F' == c || '0' == c) && dow.equals(day)) {
                exDates.add(ld);
            }
            ld = ld.plusDays(1);
        }
//        ret.setSince(general.getTermbegindate());
        ret.setSince(lesson.getEffectiveBeginDate());
//        ret.setUntil(general.getTermenddate());
        ret.setUntil(lesson.getEffectiveEndDate());
        if (!exDates.isEmpty()) {
            ret.setExdates(exDates.toArray(LocalDate[]::new));
        }
        final String room = t.getAssignedRoom() != null ? t.getAssignedRoom().getId() : null;
        if (room != null) {
            ret.setLocation(room);
        }

        int untisLessonKopplung = il.getUntisKopplung();
        //
        final int cloneId = il.id();
        //If it's clone, 
        if (cloneId != 0) {
            //Save the clone a kopplung
            //Workaround to prevent overwriting existing lesson/kopplung mappings in the database
            untisLessonKopplung += (100 * cloneId);
            final String msg = NbBundle.getMessage(StudenplanUpdater.class, "StudenplanUpdater.message.kupplung.workaround", il.getSourceNodeLabel(), cloneId, untisLessonKopplung);
            ImportUtil.getIO().getOut().println(msg);
        }
        final String teacherId = lesson.getLessonTeacher().getId().substring(3);//remove TR_
        final VendorData vData = new VendorData(il.getUntisLessonId(), untisLessonKopplung, teacherId);
        ret.setVendorData(vData);
        return ret;
    }
}
