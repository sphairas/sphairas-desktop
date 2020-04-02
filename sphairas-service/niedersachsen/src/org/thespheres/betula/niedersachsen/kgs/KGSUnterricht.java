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
public class KGSUnterricht extends AbstractMarkerConvention {

    public static final String[] IDS = {"wpk", "ag"};
    public static final String NAME = "kgs.unterricht";

    public KGSUnterricht() {
        super(NAME, IDS);
    }

    @Override
    public String getDisplayName() {
        return "KGS Unterricht";
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected Marker create(String id) {
        String sl = null;
        try {
            sl = NbBundle.getMessage(KGSUnterricht.class, "kgs.unterricht." + id);
        } catch (java.util.MissingResourceException e) {
        }
        return new KursartMarker(id, sl);
    }

    private final class KursartMarker extends AbstractMarker {

        private final String shortLabel;

        private KursartMarker(String id, String shortLabel) {
            super(NAME, id, null);
            this.shortLabel = shortLabel;
        }

        @Override
        public String getShortLabel() {
            return shortLabel;
        }

        @Override
        public String getLongLabel(Object... formattingArgs) {
            return getShortLabel();
        }

    }
}
