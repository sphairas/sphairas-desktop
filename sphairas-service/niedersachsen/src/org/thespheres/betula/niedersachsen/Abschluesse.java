/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen;

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
import org.thespheres.betula.tag.AbstractMarkerConvention;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = MarkerConvention.class)
public class Abschluesse extends AbstractMarkerConvention {

    public static final ResourceBundle BUNDLE = NbBundle.getBundle(Abschluesse.class);
    public static final String CONVENTION_NAME = "niedersachsen.abschluesse";
    private static final Pattern IDPATTERN = Pattern.compile("abschluesse.[äöüß\\p{Alpha}\\.]+", 0);

    public Abschluesse() {
        super(CONVENTION_NAME, Util.idsFromBundle(BUNDLE, IDPATTERN, "abschluesse.".length()));
    }

    @Override
    public String getDisplayName() {
        return "Schulabschlüsse Nds.";
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
        try {
            dn = BUNDLE.getString("abschluesse." + id);
        } catch (java.util.MissingResourceException e) {
        }
        return new Abschluss(id, dn);
    }

    private final static class Abschluss extends AbstractMarker implements Serializable {

        private final String label;

        private Abschluss(String id, String displayLabel) {
            super(CONVENTION_NAME, id, null);
            this.label = displayLabel;
        }

        @Override
        public String getLongLabel(Object... formattingArgs) {
            return label;
        }

    }
}
