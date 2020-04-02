/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.parse;

import java.util.Locale;
import java.util.StringJoiner;
import org.apache.commons.lang3.StringUtils;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.Marker;

/**
 *
 * @author boris.heithecker
 */
public class TranslateID {

    static final int DEFAULT_BASE_STUFE = 5;
    public static final String NO_SUBJECT_ELEMENT = "klasse";

    public static String findAbiturId(int stufeOS, int referenzJahr, Marker fach, String targetId, String firstElement) {
        //add first element
        final StringJoiner sj = new StringJoiner("-");
        if (firstElement != null) {
            sj.add(firstElement);
        }
        //add subject or primary unit
        String f;
        if (fach != null) {
            f = fach.getId();
        } else {
            f = NO_SUBJECT_ELEMENT;
        }
        sj.add(f);
        //add year/diff ref
        final int abiturJahrgang = referenzJahr + (3 - stufeOS);
        final String jahrgang = "abitur" + Integer.toString(abiturJahrgang);
        sj.add(jahrgang);
        //add unit/target id
        String uid;
        if (!StringUtils.isBlank(targetId)) {
            uid = targetId.toLowerCase(Locale.getDefault());
        } else {
            uid = "?";
        }
        sj.add(uid);
        return sj.toString();
    }

    public static String findIDWithDefaultBase(String firstElement, Marker fach, int referenzJahr, int stufe, int baseStufe, boolean ascending, String targetId, int defaultBase) {
        //add first element
        StringJoiner sj = new StringJoiner("-");
        if (firstElement != null) {
            sj.add(firstElement);
        }
        //add subject or primary unit
        String f;
        if (fach != null) {
            f = fach.getId();
        } else {
            f = NO_SUBJECT_ELEMENT;
        }
        sj.add(f);
        //add year/diff ref
        int baseYear = referenzJahr;
        String jahrgang = null;
        if (stufe != -1) {
            int diff = stufe - baseStufe;
            if (diff > 0) {
                baseYear = baseYear - diff;
            }
            jahrgang = "jg" + Integer.toString(stufe);
        }
        sj.add(Integer.toString(baseYear));
        if (jahrgang != null) {
            if (!ascending || !(baseStufe == defaultBase)) {
                if (ascending) {
                    jahrgang += "a";
                }
                sj.add(jahrgang);
            }
        }
        //add unit/target id
        String uid;
        if (targetId != null) {
            uid = targetId.toLowerCase(Locale.getDefault());
        } else {
            uid = "?";
        }
        sj.add(uid);
        return sj.toString();
    }

    public static String findId(int stufe, int referenzJahr, int baseStufe, boolean ascending, Marker fach, String targetId, String firstElement) {
        return TranslateID.findIDWithDefaultBase(firstElement, fach, referenzJahr, stufe, baseStufe, ascending, targetId, DEFAULT_BASE_STUFE);
    }

    public static String findId(int stufe, int referenzJahr, Marker fach, String kid, String firstElement) {
        return TranslateID.findId(stufe, referenzJahr, DEFAULT_BASE_STUFE, true, fach, kid, firstElement);
    }

    public static String translateUnitToTarget(String source, Marker fach, String customTargetId) {
        return translateUnitToTarget(source, fach, customTargetId != null ? new String[]{customTargetId} : new String[0]);
    }

    //User nameParse.
    public static String translateUnitToTarget(String source, Marker fach, String[] customTargetIds) {
        final String[] elements = source.split("-");
        final StringJoiner sj = new StringJoiner("-");
        int len = elements.length;
        int c = 0;
        sj.add(elements[c++]);
        if (len >= 2) {
            if (fach != null) {
                final String f = findFachElement(fach);
                final boolean insertFach = insertFach(elements);
                if (insertFach) {
                    sj.add(f);
                } else {//Replace the second element
                    elements[1] = f;
                }
            }
            sj.add(elements[c++]);
        }
//        if (len >= 3) {
//            sj.add(elements[c++]);
//        }
        if (len >= 3) {
            while (c < len - 1) {
                sj.add(elements[c++]);
            }
            if (customTargetIds != null && customTargetIds.length > 0) {
                for (int i = 0; i < customTargetIds.length; i++) {
                    sj.add(customTargetIds[i]);
                }
            } else {
                sj.add(elements[c]);//last one
            }
        }
        return sj.toString();
    }

    private static boolean insertFach(final String[] elements) {
        if (elements.length > 1) {
            final String possiblyReplace = elements[1];
            final String abitur = "abitur";
            if (possiblyReplace.startsWith(abitur) && possiblyReplace.length() == abitur.length() + 4) {
                return true;
            }
        }
        return false;
    }

    private static String findFachElement(Marker fach) {//TODO use documentModel
        if (Marker.isNull(fach)) {
            return "ag";
        }
        String f = fach.getId().toLowerCase();
        if (fach.getConvention().equals("niedersachsen.realschule.profile")) {
            f = "profil" + f;
        }
        return f;
    }

    public static boolean isKlasse(UnitId u) {
        String[] elements = u.getId().split("-");
        return elements.length > 1 && "klasse".equals(elements[1]);
    }

}
