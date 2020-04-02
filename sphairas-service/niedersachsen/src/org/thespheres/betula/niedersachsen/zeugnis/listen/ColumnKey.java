/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.zeugnis.listen;

import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import org.thespheres.betula.TermId;
import org.thespheres.betula.document.Marker;

/**
 *
 * @author boris.heithecker
 */
abstract class ColumnKey {

    final int tier;

    protected ColumnKey(int tier) {
        this.tier = tier;
    }

    int getTier() {
        return tier;
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    /**
     *
     * @author boris.heithecker
     */
    static class MarkerColumnKey extends ColumnKey {

        final Set<Marker> marker;

        public MarkerColumnKey(int tier, Set<Marker> marker) {
            super(tier);
            this.marker = marker;
        }

        Marker comparingMarker(Comparator<Marker> comp) {
            return marker.stream().min(comp).get();
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 59 * hash + this.tier;
            return 59 * hash + Objects.hashCode(this.marker);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final MarkerColumnKey other = (MarkerColumnKey) obj;
            if (this.tier != other.tier) {
                return false;
            }
            return Objects.equals(this.marker, other.marker);
        }
    }

    static class TermColumnKey extends ColumnKey {

        final TermId term;

        public TermColumnKey(int tier, TermId term) {
            super(tier);
            this.term = term;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 59 * hash + this.tier;
            return 59 * hash + Objects.hashCode(this.term);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TermColumnKey other = (TermColumnKey) obj;
            if (this.tier != other.tier) {
                return false;
            }
            return Objects.equals(this.term, other.term);
        }
    }
}
