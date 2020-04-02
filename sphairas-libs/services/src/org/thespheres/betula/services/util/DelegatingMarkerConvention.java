/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.util;

import java.util.Iterator;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerConvention;
import org.thespheres.betula.document.MarkerParsingException;

/**
 *
 * @author boris.heithecker
 */
public abstract class DelegatingMarkerConvention implements MarkerConvention {

    private MarkerConvention delegate;

    protected DelegatingMarkerConvention() {
    }

    protected MarkerConvention getDelegate() {
        if (this.delegate == null) {
            throw new IllegalStateException("Delegate not set.");
        }
        return delegate;
    }

    protected final void setDelegate(final MarkerConvention delegate) {
        if (this.delegate != null) {
            throw new IllegalStateException("Delegate alread set.");
        }
        this.delegate = delegate;
    }

    @Override
    public String getName() {
        return getDelegate().getName();
    }

    @Override
    public String getDisplayName() {
        return getDelegate().getDisplayName();
    }

    @Override
    public Marker[] getAllMarkers() {
        return getDelegate().getAllMarkers();
    }

    @Override
    public Marker[] getAllMarkers(String subset) {
        return getDelegate().getAllMarkers(subset);
    }

    @Override
    public String[] getAllSubsets() {
        return getDelegate().getAllSubsets();
    }

    @Override
    public Marker find(String id) {
        return getDelegate().find(id);
    }

    @Override
    public Marker find(String id, String subset) {
        return getDelegate().find(id, subset);
    }

    @Override
    public Iterator<Marker> iterator() {
        return getDelegate().iterator();
    }

    @Override
    public Marker parseMarker(String text) throws MarkerParsingException {
        return getDelegate().parseMarker(text);
    }

}
