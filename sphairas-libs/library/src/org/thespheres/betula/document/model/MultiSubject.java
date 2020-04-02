/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.thespheres.betula.document.Marker;

/**
 *
 * @author boris.heithecker
 */
public class MultiSubject {
    
    private final Set<Marker> subjectMarker = new HashSet<>();
    private final Map<String, Marker> discrimator = new HashMap<>();
    
    public MultiSubject(final Marker realmMarker) {
        this.discrimator.put("realm", realmMarker);
    }
    
    public MultiSubject(final Marker realmMarker, final Collection<Marker> subjects) {
        this(realmMarker);
        subjects.forEach(subjectMarker::add);
    }
    
    public Set<Marker> getSubjectMarkerSet() {
        return subjectMarker;
    }
    
    public boolean isSingleSubject() {
        return subjectMarker.size() == 1;
    }
    
    public Marker getSingleSubject() {
        return isSingleSubject() ? subjectMarker.stream().findAny().get() : null;
    }
    
    public Marker getRealmMarker() {
        return discrimator.get("realm");
    }
    
    public Map<String, Marker> getDiscrimatorMap() {
        return discrimator;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.subjectMarker);
        return 89 * hash + Objects.hashCode(this.discrimator);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MultiSubject other = (MultiSubject) obj;
        if (!Objects.equals(this.subjectMarker, other.subjectMarker)) {
            return false;
        }
        return Objects.equals(this.discrimator, other.discrimator);
    }
    
}
