/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.zeugnis;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbBundle;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.niedersachsen.ASVAssessmentConvention;
import org.thespheres.betula.niedersachsen.NdsZeugnisFormular;

/**
 *
 * @author boris.heithecker
 */
public class ReportProvisionsUtil {

    public static boolean requireAvSvReason(final Grade g) {
        if (g != null) {
            final String cnv = g.getConvention();
            if (ASVAssessmentConvention.AV_NAME.equals(cnv) || ASVAssessmentConvention.SV_NAME.equals(cnv)) {
                return g.getId().equals("d") || g.getId().equals("e");
            }
        }
        return false;
    }

    public static boolean excludeAGField(int level, Marker sgl) {
        return level >= 1 && level <= 4;
    }

    public static String[] textFields(final int level, final Marker sgl) {
        if (level == 1 || level == 2) {
            return new String[]{"interessen_faehigkeiten_fertigkeiten"};
        } else if (level == 3 || level == 4) {
            return new String[]{"arbeitsgemeinschaften_foerdermassnahmen", "besondereninteressen_faehigkeiten"};
        }
        return new String[0];
    }

    public static String getTextFieldLabel(final String id) {
        final String key = "primaryUnits.textfield.label." + id;
        final String ret = NbBundle.getMessage(ReportProvisionsUtil.class, key);
        return ret != null ? ret : id;
    }

    public static int getTextFieldPosition(final String id) {
        final String tp = NbBundle.getMessage(ReportProvisionsUtil.class, "primaryUnits.textfield.label." + id + ".position");
        int ret = Integer.MAX_VALUE;
        if (tp != null) {
            try {
                ret = Integer.parseInt(tp);
            } catch (NumberFormatException nfex) {
                final String msg = "Could not parse " + tp;
                Logger.getLogger(ReportProvisionsUtil.class.getName()).log(Level.WARNING, msg, nfex);
            }
        }
        return ret;
    }

    //TODO: überarbeiten
    public static String getHeaderPflichtUnterricht(final Marker sgl, final String stufe) {
        if (stufe != null) {
            try {
                final int l = Integer.parseInt(stufe);
                if (l < 5) {
                    return null;
                }
            } catch (NumberFormatException numberFormatException) {
            }
        }
//        return sgl != null ? NdsZeugnisFormular.PFLICHTUNTERRICHT : NdsZeugnisFormular.LEHRGAENGE;
        return NdsZeugnisFormular.PFLICHTUNTERRICHT;
    }

    public static String getGradeCaptionsOnLayoutConventionName() {
        return "de.notensystem";
    }
}
