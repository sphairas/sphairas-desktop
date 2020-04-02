/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen;

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
public class Unterricht extends AbstractMarkerConvention {

    public static final String[] IDS = {"wpk", "ag"};
    public static final String NAME = "niedersachsen.unterricht.art";

    public Unterricht() {
        super(NAME, IDS);
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(Unterricht.class, "niedersachsen.unterricht.art.display");
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
        final String ll = NbBundle.getMessage(Unterricht.class, "niedersachsen.unterricht.label." + id);
        final String sl = NbBundle.getMessage(Unterricht.class, "niedersachsen.unterricht." + id);
        return new UnterrichtsartMarker(id, sl, ll);
    }

    private final class UnterrichtsartMarker extends AbstractMarker {

        private final String shortLabel;
        private final String longLabel;

        private UnterrichtsartMarker(final String id, final String shortLabel, final String longLabel) {
            super(NAME, id, null);
            this.shortLabel = shortLabel;
            this.longLabel = longLabel;
        }

        @Override
        public String getShortLabel() {
            return shortLabel;
        }

        @Override
        public String getLongLabel(Object... formattingArgs) {
            return longLabel;
        }

    }
}
