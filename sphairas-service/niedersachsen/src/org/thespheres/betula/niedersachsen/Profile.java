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

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = MarkerConvention.class)
public class Profile extends AbstractMarkerConvention {

    public static final ResourceBundle bundle = NbBundle.getBundle(Profile.class);
    public static final String CONVENTION_NAME = "niedersachsen.realschule.profile";
    private static final Pattern idpattern = Pattern.compile("profil.[äöüß\\p{Alpha}]+", 0);

    public Profile() {
        super(CONVENTION_NAME, Util.idsFromBundle(bundle, idpattern, 7));
    }

    @Override
    public String getDisplayName() {
        return "Schwerpunkte Realschule Nds.";
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
            dn = bundle.getString("profil." + id);
            sl = bundle.getString("profil." + id + ".short");
        } catch (java.util.MissingResourceException e) {
        }
        return new ProfilMarker(id, dn, sl);
    }

    private final static class ProfilMarker extends AbstractMarker implements Serializable {

        private final String label;
        private String shortLabel;

        private ProfilMarker(String id, String displayLabel, String sl) {
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
