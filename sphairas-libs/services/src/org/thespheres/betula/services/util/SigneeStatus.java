/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.util;

import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
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
@Messages({"SigneeStatus.getDisplayName=Unterzeichner-Status",
    "SigneeStatus.longLabel.active=Aktiv",
    "SigneeStatus.longLabel.inactive=Inaktiv"})
@ServiceProvider(service = MarkerConvention.class)
public class SigneeStatus extends AbstractMarkerConvention {

    public static final String[] TAGS = {"active", "inactive"};
    public static final String NAME = "betula.signee.status";

    public SigneeStatus() {
        super(NAME, TAGS);
    }

    @Override
    protected Marker create(String id) {
        return new StatusMarker(id);
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(SigneeStatus.class, "SigneeStatus.getDisplayName");
    }

    @Override
    public Marker parseMarker(String text) throws MarkerParsingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public final class StatusMarker extends AbstractMarker {

        private final String longLabel;

        private StatusMarker(final String id) {
            super(NAME, id, null);
            longLabel = NbBundle.getMessage(SigneeStatus.class, "SigneeStatus.longLabel." + id);
        }

        @Override
        public String getLongLabel(Object... args) {
            return longLabel;
        }
    }

}
