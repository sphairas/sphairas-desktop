/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.impl;

import java.util.List;
import org.thespheres.betula.assess.TargetDocument;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.MarkerConvention;
import org.thespheres.betula.document.UniqueMarkerSet;
import org.thespheres.betula.document.model.MarkerDecoration;
import org.thespheres.betula.services.LocalFileProperties;

public class MarkerDecorationImpl implements MarkerDecoration {

    private final LocalFileProperties properties;

    public MarkerDecorationImpl(LocalFileProperties prop) {
        this.properties = prop;
    }

    @Override
    public UniqueMarkerSet getDistinguishingDecoration(DocumentId id, TargetDocument targetDocument, String view) {
        if ("subject".equals(view)) {
            final MarkerConvention[] subjects = properties.getSubjectMarkerConventions();
            final UniqueMarkerSet ret = new UniqueMarkerSet(subjects, false);
            ret.initialize(targetDocument.markers());
            return ret;
        } else if ("realm".equals(view)) {
            final MarkerConvention[] realms = properties.getRealmMarkerConventions();
            final UniqueMarkerSet ret = new UniqueMarkerSet(realms, false);
            ret.initialize(targetDocument.markers());
            return ret;
        } 
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public List<MarkerConvention> getDecoration(DocumentId id, TargetDocument document) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
