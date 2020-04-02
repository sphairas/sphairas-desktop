/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tag;

import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerConvention;

/**
 *
 * @author boris.heithecker
 */
public abstract class AbstractMarkerConvention extends TagConvention<Marker> implements MarkerConvention {

    private Marker[] allMarkers;
    private final String[] ids;

    protected AbstractMarkerConvention(String name, String[] tagIds) {
        super(name, tagIds);
        ids = tagIds;
    }

    protected String[] getIds() {
        return ids;
    }

    @Override
    public Marker[] getAllMarkers() {
        if (allMarkers == null) {
            allMarkers = new Marker[getIds().length];
            for (int i = 0; i < getIds().length; i++) {
                allMarkers[i] = find(getIds()[i]);
            }
        }
        return allMarkers;
    }

    @Override
    public Marker find(String id, String subset) {
        if (subset != null) {
            return null;
        }
        return find(id);
    }

    @Override
    public String[] getAllSubsets() {
        return new String[]{};
    }

    @Override
    public Marker[] getAllMarkers(String subset) {
        if (subset != null) {
            throw new IllegalArgumentException("Subset " + subset + " not contained in " + getName());
        }
        return getAllMarkers();
    }

}
