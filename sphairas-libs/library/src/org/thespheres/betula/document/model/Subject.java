/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document.model;

import java.util.Objects;
import org.thespheres.betula.document.Marker;

/**
 *
 * @author boris.heithecker
 */
public class Subject {

    private final Marker subjectMarker;
    private final Marker realmMarker;

    public Subject(Marker subjectMarker, Marker realmMarker) {
        this.subjectMarker = subjectMarker;
        this.realmMarker = realmMarker;
    }

    public Marker getSubjectMarker() {
        return subjectMarker;
    }

    public Marker getRealmMarker() {
        return realmMarker;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.subjectMarker);
        return 89 * hash + Objects.hashCode(this.realmMarker);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Subject other = (Subject) obj;
        if (!Objects.equals(this.subjectMarker, other.subjectMarker)) {
            return false;
        }
        return Objects.equals(this.realmMarker, other.realmMarker);
    }

}
