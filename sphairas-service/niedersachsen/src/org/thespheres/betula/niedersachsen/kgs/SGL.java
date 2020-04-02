/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.kgs;

import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.document.AbstractMarker;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerConvention;
import org.thespheres.betula.document.MarkerParsingException;
import org.thespheres.betula.tag.AbstractMarkerConvention;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = MarkerConvention.class)
public class SGL extends AbstractMarkerConvention {

    public static final String[] ID = {"hs", "rs", "gy"};
    public static final String NAME = "kgs.schulzweige";

    public SGL() {
        super(NAME, ID);
    }

    @Override
    public String getDisplayName() {
        return "KGS Schulzweige";
    }

    @Override
    public Marker find(String id, String subset) {
        if (subset != null) {
            return null;
        }
        return find(id);
    }

    @Override
    public Marker parseMarker(String text) throws MarkerParsingException {
        for (String id : getIds()) {
            Marker m = find(id);
            if (id.equals(text)) {
                return m;
            } else if (text.equalsIgnoreCase(m.getLongLabel()) || text.equalsIgnoreCase(m.getShortLabel())) {
                return m;
            }
            //KGS Tarmstedt case
            if (id.equals(text.substring(4).toLowerCase())) {
                return m;
            }
        }
        throw new MarkerParsingException(getName(), text);
    }

    @Override
    protected Marker create(String id) {
        String sl = null;
        String ll = null;
        try {
            sl = NbBundle.getMessage(SGL.class, "kgs.schulzweige." + id);
            ll = NbBundle.getMessage(SGL.class, "kgs.schulzweige." + id + ".long");
        } catch (java.util.MissingResourceException e) {
        }
        return new SGLMarker(id, sl, ll);
    }

    private final class SGLMarker extends AbstractMarker {

        private final String shortLabel;
        private final String longlabel;

        private SGLMarker(String id, String shortLabel, String longlabel) {
            super(NAME, id, null);
            this.shortLabel = shortLabel;
            this.longlabel = longlabel;
        }

        @Override
        public String getLongLabel(Object... formattingArgs) {
            return longlabel;
        }

        @Override
        public String getShortLabel() {
            return shortLabel;
        }

    }
}
