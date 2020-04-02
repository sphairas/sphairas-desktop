/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.impl;

import org.thespheres.betula.tag.AbstractMarkerConvention;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.document.AbstractMarker;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerConvention;
import org.thespheres.betula.document.MarkerParsingException;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = MarkerConvention.class)
public class NdsStandardBemerkungen extends AbstractMarkerConvention {

    public static final ResourceBundle BUNDLE = NbBundle.getBundle("org.thespheres.betula.niedersachsen.impl.NdsStandardBemerkungen");
    public static final String CONVENTION_NAME = "niedersachsen.zeugnis.bemerkungen";

    public NdsStandardBemerkungen() {
        super(CONVENTION_NAME, BUNDLE.keySet().stream()
                .toArray(String[]::new));
    }

    @Override
    public String getDisplayName() {
        return "Standard-Zeugnisbemerkungen für Niedersachsen";
    }

    @Override
    public Marker parseMarker(String text) throws MarkerParsingException {
        for (final String id : getIds()) {
            final Marker m = find(id);
            if (text.equalsIgnoreCase(m.getLongLabel()) || text.equalsIgnoreCase(m.getShortLabel())) {
                return m;
            }
        }
        return null;
    }

    @Override
    protected Marker create(String id) {
        try {
            String dn = BUNDLE.getString(id);
            return new NdsBemerkungsMarker(id, dn);
        } catch (MissingResourceException e) {
            throw new IllegalStateException(e);
        }
    }

    private final static class NdsBemerkungsMarker extends AbstractMarker implements Serializable {

        private final String label;

        private NdsBemerkungsMarker(String id, String displayLabel) {
            super(CONVENTION_NAME, id, null);
            this.label = displayLabel;
        }

        @Override
        public String getLongLabel(Object... formattingArgs) {
            return MessageFormat.format(label, formattingArgs);
        }

    }
}
