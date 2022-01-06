/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
public class UniqueMarkerSet extends AbstractSet<Marker> {

    private final Set<Marker> uniqueMarkers = new HashSet<>();
    private final Set<String> exclusiveConventions = new HashSet<>();
    private final boolean permitOthers;
    protected final ChangeSupport cSupport = new ChangeSupport(this);

    public UniqueMarkerSet() {
        this.permitOthers = true;
    }

    public UniqueMarkerSet(String[] exclusive, boolean permitOthers) {
        this.permitOthers = permitOthers;
        Arrays.stream(exclusive)
                .forEach(exclusiveConventions::add);

    }

    public UniqueMarkerSet(MarkerConvention[] exclusive, boolean permitOthers) {
        this.permitOthers = permitOthers;
        Arrays.stream(exclusive)
                .map(MarkerConvention::getName)
                .forEach(exclusiveConventions::add);

    }

    public UniqueMarkerSet(Collection<MarkerConvention> exclusive, boolean permitOthers) {
        this.permitOthers = permitOthers;
        exclusive.stream()
                .map(MarkerConvention::getName)
                .forEach(exclusiveConventions::add);

    }

    public void initialize(final Marker[] from) {
        final boolean changed = Arrays.stream(from)
                .distinct()
                .filter(Objects::nonNull)
                .collect(Collectors.reducing(false, m -> addImpl(m, false), (b, n) -> b | n));
        if (changed) {
            cSupport.fireChange();
        }
    }

    @Override
    public boolean add(final Marker e) {
        return addImpl(e, true);
    }

    private boolean addImpl(final Marker e, final boolean fire) {
        if (e == null) {
            return uniqueMarkers.remove(e);
        }
        final boolean exclusive = exclusiveConventions.contains(e.getConvention());
        if (!exclusive && !permitOthers) {
            return false;
        }
        Iterator<Marker> it = uniqueMarkers.iterator();
        while (it.hasNext()) {
            final Marker m = it.next();
            if (m.getConvention().equals(e.getConvention()) || (exclusive && exclusiveConventions.contains(m.getConvention()))) {
                if (m.equals(e)) {
                    return false;
                }
                it.remove();
                break;
            }
        }
        final boolean ret = uniqueMarkers.add(e);
        if (ret && fire) {
            cSupport.fireChange();
        }
        return ret;
    }

    public Marker get(String convention) {
        return uniqueMarkers.stream()
                .filter(m -> m.getConvention().equals(convention))
                .collect(CollectionUtil.requireSingleOrNull());
    }

    public Set<Marker> get(String... conventions) {
        final String[] arr = conventions != null ? conventions : new String[0];
        return Arrays.stream(arr)
                .map(this::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public Marker getUnique(String... conventions) {
        final String[] arr = conventions != null ? conventions : new String[0];
        return Arrays.stream(arr)
                .map(this::get)
                .filter(Objects::nonNull)
                .collect(CollectionUtil.singleOrNull());
    }

    @Override
    public Iterator<Marker> iterator() {
        final Iterator<Marker> delegate = uniqueMarkers.iterator();
        class IteratorDelegate implements Iterator<Marker> {

            @Override
            public boolean hasNext() {
                return delegate.hasNext();
            }

            @Override
            public Marker next() {
                return delegate.next();
            }

            @Override
            public void remove() {
                delegate.remove();
                cSupport.fireChange();
            }

        }
        return new IteratorDelegate();
    }

    @Override
    public int size() {
        return uniqueMarkers.size();
    }

    public List<MarkerConvention> getExclusiveConventions() {
        return exclusiveConventions.stream()
                .map(MarkerFactory::findConvention)
                .collect(Collectors.toList());
    }

    public void addChangeListener(ChangeListener listener) {
        cSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        cSupport.removeChangeListener(listener);
    }

    public static Marker findUniqueMarker(MarkerConvention[] conventions, Collection<Marker> source) {
        return Arrays.stream(conventions)
                .flatMap(mc -> source.stream().filter(m -> m.getConvention().equals(mc.getName())))
                .collect(CollectionUtil.singleOrNull());
    }

    public static Marker findUniqueMarker(String[] conventions, Collection<Marker> source) {
        return Arrays.stream(conventions)
                .flatMap(mc -> source.stream().filter(m -> m.getConvention().equals(mc)))
                .collect(CollectionUtil.singleOrNull());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.uniqueMarkers);
        hash = 53 * hash + (this.permitOthers ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UniqueMarkerSet other = (UniqueMarkerSet) obj;
        if (!Objects.equals(this.uniqueMarkers, other.uniqueMarkers)) {
            return false;
        }
        return this.permitOthers == other.permitOthers;
    }

}
