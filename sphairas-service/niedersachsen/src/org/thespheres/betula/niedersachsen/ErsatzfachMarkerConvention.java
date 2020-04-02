/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen;

import java.io.IOException;
import java.io.InputStream;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.document.MarkerConvention;
import org.thespheres.betula.services.util.DelegatingMarkerConvention;
import org.thespheres.betula.services.util.XmlMarkerConventionSupport;
import org.thespheres.betula.xmldefinitions.XmlMarkerConventionDefinition;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = MarkerConvention.class)
public class ErsatzfachMarkerConvention extends DelegatingMarkerConvention {

    public ErsatzfachMarkerConvention() throws IOException {
        try (final InputStream is = ErsatzfachMarkerConvention.class.getResourceAsStream("Ersatzfach.xml")) {
            final XmlMarkerConventionDefinition def = XmlMarkerConventionSupport.load(is);
            setDelegate(def);
        }
    }

}
