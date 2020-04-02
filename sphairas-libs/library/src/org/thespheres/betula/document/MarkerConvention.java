/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document;

import java.util.Collections;
import java.util.Map;
import javax.swing.event.ChangeListener;
import org.thespheres.betula.Convention;

/**
 *
 * @author boris.heithecker
 */
public interface MarkerConvention extends Iterable<Marker>, Convention<Marker> {

    public Marker[] getAllMarkers();

    public Marker[] getAllMarkers(String subset);

    public String[] getAllSubsets();

    public Marker find(String id, String subset);

    public Marker parseMarker(String text) throws MarkerParsingException;

    public default Map<String, String> getHints() {
        return Collections.EMPTY_MAP;
    }

    public interface Mutable extends MarkerConvention {

        public void addChangeListener(ChangeListener l);

        public void removeChangeListener(ChangeListener l);
    }

}
