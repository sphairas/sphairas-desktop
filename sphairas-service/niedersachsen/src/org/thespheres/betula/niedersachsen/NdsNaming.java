package org.thespheres.betula.niedersachsen;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.HashMap;
import org.apache.commons.lang3.StringUtils;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerFactory;
import org.thespheres.betula.services.implementation.naming.Naming;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.SchemeProvider;
import org.thespheres.betula.services.scheme.spi.TermSchedule;

/**
 *
 * @author boris.heithecker
 */
class NdsNaming extends Naming {

    private final boolean permitAlternativeSubjects;

//Naming
    private NdsNaming(String provider, TermSchedule ts, String firstElement, int base, int baseAbi, boolean permitAlternativeSubjects) {
        super(provider, ts, firstElement, base, baseAbi);
        this.permitAlternativeSubjects = permitAlternativeSubjects;
    }

    static NdsNaming create(String provider, String firstElement, int base, int baseAbi, boolean permitAlternativeSubjects) {
        final TermSchedule ts = SchemeProvider.find(LSchB.PROVIDER_INFO.getURL()).getScheme(TermSchedule.DEFAULT_SCHEME, TermSchedule.class);
        if (ts == null) {
            throw new IllegalStateException("No TermSchedule could be found.");
        }
        return new NdsNaming(provider, ts, firstElement, base, baseAbi, permitAlternativeSubjects);
    }

    @Override
    protected boolean findFachMarker(String type, NamingResolver.Result unresolved, HashMap<String, String> elements) {
        Marker fach;
        if (type.equals("andere")) {
            fach = MarkerFactory.find(FoerderungsBerichte.CONVENTION_NAME, "andere", null);
        } else if (type.startsWith("profil")) {
            fach = MarkerFactory.find(Profile.CONVENTION_NAME, type.substring(6), null);
        } else {
            fach = MarkerFactory.find(Faecher.CONVENTION_NAME, type, null);
            if (fach == null) {
                fach = MarkerFactory.find(Profile.CONVENTION_NAME, type, null);
            }
        }
        if (fach != null) {
            elements.put(NdsNaming.FACH, fach.getLongLabel());
            elements.put(NdsNaming.FACH_KURZ, fach.getShortLabel());
            return true;
        } else {
            final String[] sub = type.split("\\.");
            final String id = sub[sub.length - 1];
            final String subset;
            if (sub.length > 1) {
                subset = sub[sub.length - 2];
            } else {
                subset = null;
            }
            final String cnvname;
            if (sub.length > 2) {
                cnvname = sub[sub.length - 3];
            } else {
                cnvname = null;
            }
            if (cnvname != null) {
                final String fachName = StringUtils.capitalize(cnvname) + " (" + subset + ", " + id + ")";
                elements.put(NdsNaming.FACH, fachName);
                elements.put(NdsNaming.FACH_KURZ, type);
                return true;
            }
            if (permitAlternativeSubjects) {
                elements.put(NdsNaming.FACH, StringUtils.capitalize(type));
                return true;
            }
        }
        return false;
    }

}
