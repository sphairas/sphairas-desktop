/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tableimport.util;

import java.util.StringJoiner;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.ical.VCard;

/**
 *
 * @author boris.heithecker
 */
public class VCardUtil {

    @Messages({"VCardUtil.oneLine.GENDER=Geschlecht: {0}",
        "VCardUtil.oneLine.BDAY=Geburtsdatum: {0}",
        "VCardUtil.oneLine.BIRTHPLACE=Geburtsort: {0}"})
    public static String oneLine(final VCard c) {
        final StringJoiner sj = new StringJoiner("; ");
        sj.add(c.getFN());
        c.getAnyPropertyValue(VCard.GENDER)
                .map(v -> NbBundle.getMessage(VCardUtil.class, "VCardUtil.oneLine.GENDER", v))
                .ifPresent(sj::add);
        c.getAnyPropertyValue(VCard.BDAY)
                .map(v -> NbBundle.getMessage(VCardUtil.class, "VCardUtil.oneLine.BDAY", v))
                .ifPresent(sj::add);
        c.getAnyPropertyValue(VCard.BIRTHPLACE)
                .map(v -> NbBundle.getMessage(VCardUtil.class, "VCardUtil.oneLine.BIRTHPLACE", v))
                .ifPresent(sj::add);
        return sj.toString();
    }

}
