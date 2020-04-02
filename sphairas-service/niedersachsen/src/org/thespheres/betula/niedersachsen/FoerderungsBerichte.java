/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen;

import org.thespheres.betula.tag.AbstractMarkerConvention;
import java.io.Serializable;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.document.AbstractMarker;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerConvention;
import org.thespheres.betula.document.MarkerParsingException;
import org.thespheres.betula.niedersachsen.impl.Util;
import org.thespheres.betula.services.IllegalAuthorityException;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = MarkerConvention.class)
public class FoerderungsBerichte extends AbstractMarkerConvention {

    private final static String bundleBase = "foerder.bericht.";
    public static final ResourceBundle bundle = NbBundle.getBundle(FoerderungsBerichte.class);
    public static final String CONVENTION_NAME = "niedersachsen.unterricht.foerder.berichte";
    private static final Pattern idpattern = Pattern.compile(bundleBase + "[äöüß\\p{Alpha}]+", 0);

    public FoerderungsBerichte() {
        super(CONVENTION_NAME, Util.idsFromBundle(bundle, idpattern, bundleBase.length()));
    }

    public static final boolean isHauptfach(Marker fach) throws IllegalAuthorityException {
        if (!(fach instanceof BerichtMarker)) {
            throw new IllegalAuthorityException();
        } 
        return false;
    }

    @Override
    public String getDisplayName() {
        return "Berichte (Förderbereich) Nds.";
    }

    @Override
    public Marker parseMarker(String text) throws MarkerParsingException {
        for (String id : getIds()) {
            Marker m = find(id);
            if (text.equalsIgnoreCase(m.getLongLabel()) || text.equalsIgnoreCase(m.getShortLabel())) {
                return m;
            }
        }
        return null;
    }

    @Override
    protected Marker create(String id) {
        String dn = null;
        String sl = null;
        try {
            dn = bundle.getString(bundleBase + id);
            sl = bundle.getString(bundleBase + id + ".short");
        } catch (java.util.MissingResourceException e) {
        }
        return new BerichtMarker(id, dn, sl);
    }

    private final static class BerichtMarker extends AbstractMarker implements Serializable {

        private final String label;
        private final String shortLabel;

        private BerichtMarker(String id, String displayLabel, String sl) {
            super(CONVENTION_NAME, id, null);
            this.label = displayLabel;
            this.shortLabel = sl;
        }

        @Override
        public String getLongLabel(Object... formattingArgs) {
            return label;
        }

        @Override
        public String getShortLabel() {
            return shortLabel;
        }

    }
}
