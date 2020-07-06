/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.zeugnis;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 *
 * @author boris
 */
public class NdsReportConstants {

    public static final SubjectOrderDefinition FACH_COMPARATOR;
    static final SubjectOrderDefinition FACH_COMPARATOR_HS;
    static final SubjectOrderDefinition FACH_COMPARATOR_RS;
    static final SubjectOrderDefinition FACH_COMPARATOR_GY;
    private static final Pattern APOSTROPH_GENITIV = Pattern.compile(".+[sßzx]|(ce)");

    static {
        try {
            FACH_COMPARATOR = SubjectOrderDefinition.load(NdsReportConstants.class.getResourceAsStream("/org/thespheres/betula/niedersachsen/resources/default_order.xml"));
            FACH_COMPARATOR_HS = SubjectOrderDefinition.load(NdsReportConstants.class.getResourceAsStream("/org/thespheres/betula/niedersachsen/resources/order_hs.xml"));
            FACH_COMPARATOR_RS = SubjectOrderDefinition.load(NdsReportConstants.class.getResourceAsStream("/org/thespheres/betula/niedersachsen/resources/order_rs.xml"));
            FACH_COMPARATOR_GY = SubjectOrderDefinition.load(NdsReportConstants.class.getResourceAsStream("/org/thespheres/betula/niedersachsen/resources/order_gy.xml"));
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static String getGenitiv(String vorname) {
        String genitiv = null;
        if (vorname != null) {
            if (APOSTROPH_GENITIV.matcher(vorname).matches()) {
                genitiv = vorname + "'";
            } else {
                genitiv = vorname + "s";
            }
        }
        return genitiv;
    }

    public static String getPossessivPronomen(String gender) {
        if (gender != null) {
            switch (gender.toLowerCase()) {
                case "f":
                    return "ihre";
                case "m":
                    return "seine";
            }
        }
        return null;
    }

    public static String getPossessivPronomenGenitiv(String gender) {
        if (gender != null) {
            switch (gender.toLowerCase()) {
                case "f":
                    return "ihrer";
                case "m":
                    return "seiner";
            }
        }
        return null;
    }
}
