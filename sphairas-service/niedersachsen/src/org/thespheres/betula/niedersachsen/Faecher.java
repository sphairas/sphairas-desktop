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
public class Faecher extends AbstractMarkerConvention {

    public static final ResourceBundle BUNDLE = NbBundle.getBundle(Faecher.class);
    public static final String CONVENTION_NAME = "niedersachsen.unterricht.faecher";
    private final static String[] HAUPTFAECHER = BUNDLE.getString("hauptfaecher").split(",");
    private static final Pattern ID_PATTERN = Pattern.compile("fach.[äöüß\\p{Alpha}]+[äöüß\\p{Alpha}_]+", 0);

    public Faecher() {
        super(CONVENTION_NAME, Util.idsFromBundle(BUNDLE, ID_PATTERN, 5));
    }

    public static final boolean isHauptfach(Marker fach) throws IllegalAuthorityException {
        if (!(fach instanceof FachMarker)) {
            throw new IllegalAuthorityException();
        } else {
            for (String hf : HAUPTFAECHER) {
                if (hf.equals(fach.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getDisplayName() {
        return "Fächerkanon Nds.";
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
            dn = BUNDLE.getString("fach." + id);
            sl = BUNDLE.getString("fach." + id + ".short");
        } catch (java.util.MissingResourceException e) {
        }
        return new FachMarker(id, dn, sl);
    }

    private final static class FachMarker extends AbstractMarker implements Serializable {

        private final String label;
        private final String shortLabel;

        private FachMarker(String id, String displayLabel, String sl) {
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
