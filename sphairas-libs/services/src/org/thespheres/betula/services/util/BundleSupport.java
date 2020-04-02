/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.util;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.services.util.BundleSupport.Factory;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 * @param <M>
 */
public abstract class BundleSupport<M extends Marker> {

    protected final Pattern IDPATTERN = Pattern.compile("[äöüß\\p{Alnum}]+[\\.[äöüß\\p{Alnum}]+]*", 0);
    protected M[] elements;
    protected final ChangeSupport cSupport = new ChangeSupport(this);

    protected BundleSupport() {
    }

    static BundleSupport create(final String name, final String provider, final String resource, final Map<String, String> arg) {
        return Lookup.getDefault().lookup(Factory.class).create(name, provider, resource, arg);
    }

    protected abstract String getConvention();

    protected String getDisplayName() {
        return getConvention();
    }

    protected abstract void ensureLoaded();

    protected abstract void markForReload();

    Marker[] markers() {
        ensureLoaded();
        return Arrays.copyOf(elements, elements.length);
    }

    String[] subsets() {
        ensureLoaded();
        return Arrays.stream(elements)
                .map(m -> m.getSubset())
                .toArray(String[]::new);
    }

    Marker marker(final String subset, final String id) {
        ensureLoaded();
        return Arrays.stream(elements)
                .filter(m -> m.getId().equals(id) && Objects.equals(m.getSubset(), subset))
                .collect(CollectionUtil.requireSingleOrNull());
    }

    protected abstract void reload();

    void addChangeListener(ChangeListener listener) {
        cSupport.addChangeListener(listener);
    }

    void removeChangeListener(ChangeListener listener) {
        cSupport.removeChangeListener(listener);
    }

    public static abstract class Factory {

        protected abstract BundleSupport create(final String name, final String provider, final String resource, final Map<String, String> arg) throws IllegalStateException;
    }

}
