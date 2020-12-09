/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.gpuntis.xml.General;
import org.thespheres.betula.gpuntis.xml.Lesson;
import org.thespheres.betula.gpuntis.xml.Subject;
import org.thespheres.betula.gpuntis.xml.Teacher;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.scheme.spi.TermSchedule;
import org.thespheres.betula.xmlimport.ImportUtil;

/**
 *
 * @author boris.heithecker
 */
public class ImportUntisUtil {

    public static final DateTimeFormatter UNTIS_DATES = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static final DateTimeFormatter MSG_DATES = DateTimeFormatter.ofPattern("d.M.yyyy");

    public static String dirName(Teacher t) {
        return t != null ? t.getSurname() + ", " + t.getForename() : "";
    }

    public static String subject(Subject su) {
        if (su == null) {
            return "";
        }
        if (su.getLongName() != null) {
            return su.getLongName();
        } else {
            return su.getId().substring(3);
        }
    }

    public static Marker findSubjectMarker(final UntisImportConfiguration config, final Subject su) {
        return Arrays.stream(config.getSubjectMarkerConventions())
                .flatMap(mc -> Arrays.stream(mc.getAllMarkers()))
                .filter(m -> (m.getLongLabel().equalsIgnoreCase(su.getLongName()) || m.getShortLabel().equalsIgnoreCase(su.getId().substring(3))))
                .findAny()
                .orElse(Marker.NULL);
    }

    @Messages("ImportUntisUtil.findDocumentTerm.exception.nosameterm=Das Halbjahres-Ende in Untis ({1}) liegt nicht mehr im Import-Halbjahr \"{0}\".")
    public static Term findDocumentTerm(General general, TermSchedule ts) {
        final LocalDate begin = general.getTermBeginDate();
        final LocalDate end = general.getTermEndDate();
        final Term ret = ts.termOf(begin);
        if (end.isAfter(ret.getEndDate())) {
            String msg = NbBundle.getMessage(ImportUntisUtil.class, "ImportUntisUtil.findDocumentTerm.exception.nosameterm", ret.getDisplayName(), MSG_DATES.format(end));
            ImportUtil.getIO().getErr().println(msg);
        }
        return ret;
    }

    public static int findSchuljahr(Lesson lesson) {
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(lesson.getEffectiveBeginDate());
//        int ret = cal.get(Calendar.YEAR);
//        return cal.get(Calendar.MONTH) < 7 ? --ret : ret;
        int ret = lesson.getEffectiveBeginDate().getYear();
        return lesson.getEffectiveBeginDate().getMonth().compareTo(Month.AUGUST) < 0 ? --ret : ret;
    }

    public static Signee parseEmail(final String email) {
        final String[] parts = email.split("@");
        final String suffix = parts.length > 1 ? parts[1].trim() : "unknown";
        return new Signee(parts[0].trim(), suffix, true);
    }

    public static Integer computeSchoolYearId(General general) {
        final int y1 = general.getSchoolYearBeginDate().getYear();
        final int y2 = general.getSchoolYearEndDate().getYear();
        if (y2 == y1 + 1) {
            return y1 * 10000 + y2;
        }
        return null;
    }

}
