/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.zeugnis;

import java.text.Collator;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 *
 * @author boris.heithecker
 */
public class DeReportBuilderUtil {

    private static final Pattern APOSTROPH_GENITIV = Pattern.compile(".+[sßzx]|(ce)");
    public static final Collator COLLATOR = Collator.getInstance(Locale.GERMANY);

    private DeReportBuilderUtil() {
    }

    public static String getGenitiv(final String vorname) {
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

    public static String getPossessivPronomen(final String gender) {
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
}
