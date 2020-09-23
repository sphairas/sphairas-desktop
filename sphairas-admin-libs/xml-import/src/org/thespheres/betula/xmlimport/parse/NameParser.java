/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.parse;

import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Marker;

/**
 *
 * @author boris.heithecker
 */
//TODO implement as additional Interface for NamingResolver....
public class NameParser {

    private final static Pattern STUFE_MATCHER = Pattern.compile("\\A\\d+");
    private final String authority;
    private final String firstElement;
    private final Integer baseLevel;

    public NameParser(String authority, String firstElement, Integer baseLevel) {
        this.authority = authority;
        this.firstElement = firstElement;
        this.baseLevel = baseLevel;
        if (baseLevel != null && baseLevel < 1) {
            throw new IllegalArgumentException("baseLevel must be greater than zero.");
        }
    }

    //klasse always null, klassenfach always false
//    public UnitId initPreferredTarget(int stufe, String klasse, Marker fach, boolean klassenfach, String kursnr, int referenzJahr) {
//        StringBuilder sb = new StringBuilder();
//        if (firstElement != null) {
//            sb.append(firstElement);
//        }
//        int jahr = referenzJahr;
//        if (stufe != -1 && stufe != 10) {
//            int s = stufe - 5;
//            jahr = referenzJahr;
//            if (s > 0) {
//                jahr = jahr - s;
//            }
//        } else {
//        }
//        boolean os = stufe >= 11;
//        String uid = "?";
//        if (klassenfach) {
//            if (!os) {
//                sb.append("klasse-");
//                if (stufe == 10 && klasse.length() > 2) {
//                    uid = resolveJg10uid(klasse);
////                uid = klasse.length() > 3 ? klasse.substring(3) : "?";
//                } else {
//                    uid = klasse.toLowerCase(Locale.GERMANY).substring(1);
//                }
//            } else if (klasse != null && klasse.length() >= 5) {
//                int qp = 0;
//                try {
//                    qp = Integer.parseInt(klasse.substring(4, 5));
//                } catch (NumberFormatException numberFormatException) {
//                }
//                String id = Integer.toString(referenzJahr + 3 - qp);
//                sb.append("abitur").append(id);
//            }
//        } else {
//            String f;
//            if (fach != null) {
//                f = fach.getId();
//            } else {
//                f = "?";
//            }
//            sb.append(f);
//            sb.append("-");
//            if (!StringUtils.isBlank(kursnr)) {
//                uid = kursnr.toLowerCase(Locale.GERMANY).substring(1);
//            }
//        }
//        if (!os) {
//            sb.append(Integer.toString(jahr));
//            sb.append("-");
//        }
//        if (stufe == 10) {
//            sb.append("jg10-");
//        }
//        if (!os) {
//            sb.append(uid);
//        }
//        return new UnitId(authority, sb.toString());
//    }
    public UnitId findUnitId(String resolvedName, int referenzjahr) {
        return findUnitIdImpl(resolvedName, null, referenzjahr, null);
    }

    public UnitId findUnitId(String resolvedName, int referenzjahr, int levelToCheck) {
        return findUnitIdImpl(resolvedName, null, referenzjahr, (Integer) levelToCheck);
    }

    public UnitId findUnitId(String resolvedName, Marker subject, int referenzjahr) {
        return findUnitIdImpl(resolvedName, subject, referenzjahr, null);
    }

    public UnitId findUnitId(String resolvedName, Marker subject, int referenzjahr, int levelToCheck) {
        return findUnitIdImpl(resolvedName, subject, referenzjahr, (Integer) levelToCheck);
    }

    private UnitId findUnitIdImpl(final String resolvedName, final Marker subject, final int referenzjahr, final Integer checkLevel) {
        final String name = StringUtils.trimToEmpty(resolvedName);
        final StringJoiner sj = new StringJoiner("-");
        if (firstElement != null) {
            sj.add(firstElement);
        }
        String uid = null;
        if (name.startsWith("Q") && name.length() == 2) {
            int qp = Integer.parseInt(name.substring(1, 2));
            final String id = "abitur" + Integer.toString(referenzjahr + 3 - qp);
            sj.add(id);
            uid = sj.toString();
        } else if (name.startsWith("11.") || name.startsWith("12.")) {
            int qp = Integer.parseInt(name.substring(4, 5));
            final String id = "abitur" + Integer.toString(referenzjahr + 3 - qp);
            sj.add(id);
            uid = sj.toString();
        } else {
            Matcher m = STUFE_MATCHER.matcher(name);
            if (m.find()) {
                int stufe = Integer.parseInt(m.group());
                if (stufe >= 1 && (checkLevel == null || checkLevel == stufe)) {
                    final String ident = StringUtils.trimToNull(name.substring(m.end()));
                    if (stufe == 10) {
                        uid = TranslateID.findId(stufe, referenzjahr, 10, false, null, ident, firstElement);
                    } else if (baseLevel != null) {
                        uid = TranslateID.findIDWithDefaultBase(firstElement, subject, referenzjahr, stufe, baseLevel, true, ident, baseLevel);
                    } else {
                        uid = TranslateID.findId(stufe, referenzjahr, subject, ident, firstElement);
                    }
                }
            }
        }
        if (uid != null) {
            return new UnitId(authority, uid);
        }
        return null;
    }

//    private static String resolveJg10uid(String resolvedName) {
//        int index = 2;
//        if (resolvedName.length() > 2 && resolvedName.charAt(index) == '.') {
//            index++;
//        }
//        String uid = resolvedName.substring(index).toLowerCase(Locale.GERMANY);
//        if (!uid.startsWith("e")) {
//            uid = "hr" + uid;
//        }
//        return uid;
//    }
    public DocumentId translateUnitIdToTargetDocumentBase(final String source, final Marker subjectMarker, String[] append) {
        return translateUnitIdToTargetDocumentBase(source, subjectMarker != null ? subjectMarker.getId() : null, append);
    }

    public DocumentId translateUnitIdToTargetDocumentBase(final String source, final String subjectId, String[] append) {
        final StringJoiner target = new StringJoiner("-");
        boolean subjectAdded = false;//Add subject only once
        for (final String el : source.split("-")) {
            if (subjectId != null && TranslateID.NO_SUBJECT_ELEMENT.equals(el)) {
                target.add(subjectId);
                subjectAdded = true;
            } else if (subjectId != null && el.startsWith("abitur") && el.length() == "abitur".length() + 4) {
                if (!subjectAdded) {
                    target.add(subjectId);
                    subjectAdded = true;
                }
                target.add(el);
            } else {
                target.add(el);
                subjectAdded = Objects.equals(el, subjectId);
            }
        }
        if (append != null) {
            Arrays.stream(append)
                    .filter(s -> !StringUtils.isBlank(s))
                    .forEach(target::add);
        }
        return new DocumentId(authority, target.toString(), DocumentId.Version.LATEST);
    }
}
