/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis.xml.rest;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author boris.heithecker
 */
public class UntisSubstitutionUtil {

    public static final LocalDate YEAR_1900_JAN_1ST = LocalDate.of(1900, Month.JANUARY, 1);
    public static final DateTimeFormatter SUBSTITUTION_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private UntisSubstitutionUtil() {
    }

    public static String charToText(char c) {
        switch (c) {
//    20        Vertretungsart
            case 'T':
                return "verlegt";
            case 'F':
                return "verlegt von"; //Entfällt
            case 'W':
                return "Tausch";
            case 'S':
                return "Betreuung";
            case 'A':
                return "Sondereinsatz";
            case 'C':
                return "Entfall";
            case 'L':
                return "Freisetzung";
            case 'P':
                return "Teil-Vertretung";
            case 'R':
                return "Raumvertretung";
            case 'B':
                return "Pausenaufsichtsvertretung";
// ~  Lehrertausch
            case 'E':
                return "Klausur";
        }
        return "[" + c + "]";
    }

    public static UntisSubstitutionData.ViewData charToFlags(final char c, Integer bits, int lessonId, String flags) {
        final String v = UntisSubstitutionUtil.charToText(c);
//        boolean cancelled = c == 'C' || c == 'L' || c == 'T';
//        boolean deferred = c == 'T' || c == 'F';
//        boolean nonRegular = lessonId == 0 || c == 'A';
        return new UntisSubstitutionData.ViewData(v, bits, (char) 0, flags);
    }
}
