/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis.updates;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;import org.thespheres.betula.gpuntis.xml.rest.UntisSubstitutionData;
import org.thespheres.betula.gpuntis.xml.rest.UntisSubstitutionUtil;
;

/**
 *
 * @author boris.heithecker
 */
class ParseUtil {

    private static final String ELEMENT_SEPARATOR = ",";
    static final DateTimeFormatter TIMESTAMP_DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private static String removeQuots(String raw) {
        String ret = StringUtils.trimToNull(raw);
        if (ret != null) {
            ret = StringUtils.removeEnd(raw, "\"");
            ret = StringUtils.removeStart(ret, "\"");
        }
        return ret;
    }

    static UntisSubstitutionData parseSubstitution(String line) {
        final String[] elements = line.split(ELEMENT_SEPARATOR);
        //ID
        int id = Integer.parseInt(elements[0]);
        final UntisSubstitutionData d = new UntisSubstitutionData();
        //date
        String dateValue = removeQuots(elements[1]);
        LocalDate date = LocalDate.parse(dateValue, UntisSubstitutionUtil.SUBSTITUTION_DATE_FORMAT);
        d.setDate(date);
        //untisPeriod
        int period = Integer.parseInt(elements[2]);
        d.setUntisPeriod(period);
        //AbsenceId
        //LessonId
        int lessonId = Integer.parseInt(elements[4]);
        d.setLessonId(lessonId);
        //TeacherLesson
        String trLesson = removeQuots(elements[5]);
        d.setTeacherLessonUntisName(trLesson);
        //TeacherSubstitution
        String trSubstitution = removeQuots(elements[6]);
        d.setTeacherSubstitutingUntisName(trSubstitution);
        //TeacherSubstitution
        String location = removeQuots(elements[12]);
        d.setLocation(location);
        //Text
        String text = removeQuots(elements[16]);
        d.setText(text);
        //Flags
        String flagValue = removeQuots(elements[19]);
        final char c = flagValue != null ? flagValue.charAt(0) : 0;
        int bits = Integer.parseInt(elements[17]);
        UntisSubstitutionData.ViewData flags = UntisSubstitutionUtil.charToFlags(c, bits, lessonId, flagValue);
        d.setFlags(flags);
        //Timestamp
        Date timestamp = dateFileTimestamp(elements[20]);
        d.setTimestamp(timestamp);
        return d;
    }

    private static Date dateFileTimestamp(String tms) {
        if (tms.length() == 12) {
            final LocalDateTime nts = LocalDateTime.parse(tms, TIMESTAMP_DATETIME_FORMAT);
            return Date.from(nts.atZone(ZoneId.systemDefault()).toInstant());
        }
        //"0000" --> was bedeutet das
        return null;
    }
}
