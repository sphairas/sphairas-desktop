/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.event.ChangeListener;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerConvention;
import org.thespheres.betula.document.MarkerParsingException;

/**
 *
 * @author boris.heithecker
 */
public abstract class AbstractReloadableMarkerConvention implements MarkerConvention.Mutable {

    protected final BundleSupport support;

    protected AbstractReloadableMarkerConvention(final String provider, final String resource) {
        this(null, provider, resource);
    }

    protected AbstractReloadableMarkerConvention(final String name, final String provider, final String resource) {
        this(name, provider, resource, null);
    }

    protected AbstractReloadableMarkerConvention(final String name, final String provider, final String resource, final Map<String, String> args) {
        support = BundleSupport.create(name, provider, resource, args);
    }

    @Override
    public final String getName() {
        return support.getConvention();
    }

    @Override
    public Marker find(String id, String subset) {
        return support.marker(subset, id);
    }

    @Override
    public final Marker find(final String id) {
        return support.marker(null, id);
    }

    @Override
    public Marker[] getAllMarkers() {
        return support.markers();
    }

    @Override
    public String[] getAllSubsets() {
        return support.subsets();
    }

    @Override
    public Marker[] getAllMarkers(String subset) {
        if (subset != null) {
            throw new IllegalArgumentException("Subset " + subset + " not contained in " + getName());
        }
        return getAllMarkers();
    }

    @Override
    public Iterator<Marker> iterator() {
        final Iterator<Marker> iterator = Arrays.asList(support.markers()).iterator();
        return new Iterator<Marker>() {

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Marker next() {
                return iterator.next();
            }

            @Override
            public void remove() {
                iterator.remove();
            }

        };
    }

    @Override
    public Marker parseMarker(String text) throws MarkerParsingException {
        return Arrays.stream(support.markers())
                .filter(e -> text.equalsIgnoreCase(e.getLongLabel()) || text.equalsIgnoreCase(e.getShortLabel()))
                .findAny().orElseThrow(() -> new MarkerParsingException(getName(), text));
    }

    public void markForReload() {
        support.markForReload();
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        support.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        support.removeChangeListener(listener);
    }

    public static AbstractReloadableMarkerConvention create(final Map<String, ?> args) {
        final String name = (String) args.get("name");
        final String displayName = (String) args.get("display-name");
        final String provider = (String) args.get("provider");
        final String webProvider = (String) args.get("web-provider");
        final String resource = (String) args.get("resource");
        final Map<String, String> other = args.entrySet().stream()
                .filter(e -> !"instanceCreate".equals(e.getKey())) //recursive method invocation
                .filter(e -> e.getValue() instanceof String)
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (String) e.getValue()));
        class MarkerConventionImpl extends AbstractReloadableMarkerConvention {

            MarkerConventionImpl() {
                super(name, webProvider != null ? webProvider : provider, resource, other);
            }

            @Override
            public String getDisplayName() {
                return displayName != null ? displayName : support.getDisplayName();
            }

        }
        return new MarkerConventionImpl();
    }

}
