/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.zeugnis.listen;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
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
    static class MarkerColumnKey extends ColumnKey implements Comparable<MarkerColumnKey> {

        final Set<Marker> marker;
        final String alt;

        public MarkerColumnKey(int tier, Set<Marker> marker, String alt) {
            super(tier);
            this.marker = marker;
            this.alt = alt;
        }

        Marker comparingMarker(Comparator<Marker> comp) {
            return marker.stream().min(comp).orElse(null);
        }

        /**
         * Compares keys by priority: marker keys first (ordered by position),
         * then alt keys (ordered alphabetically), then empty keys last.
         *
         * <p>Transitivity must be strictly maintained across all three categories.
         * In particular, the same "wins against" relationship must hold consistently:
         * if marker &lt; alt and marker &lt; empty, then the alt-vs-empty ordering must
         * not create a cycle. Example of a violation to avoid:
         * <pre>
         *   A (marker, pos=5) &lt; B (alt="xyz")   [marker always beats alt]
         *   B (alt="xyz")     &lt; C (marker, pos=3) [WRONG: would make B &lt; C &lt; A &lt; B]
         * </pre>
         * The fix ensures the alt branch always defers to marker keys by returning +1
         * when {@code o} has a non-null comparingMarker.
         */
        @Override
        public int compareTo(MarkerColumnKey o) {
            if (comparingMarker(StudentDetailsXml.ORDER) != null) {
                if (o.comparingMarker(StudentDetailsXml.ORDER) != null) {
                    return StudentDetailsXml.ORDER.positionOf(comparingMarker(StudentDetailsXml.ORDER)) - StudentDetailsXml.ORDER.positionOf(o.comparingMarker(StudentDetailsXml.ORDER));
                } else {
                    return -1;
                }
            } else if (alt == null) {
                if (o.comparingMarker(StudentDetailsXml.ORDER) == null) {
                    return 0;
                } else {
                    return 1;
                }
            }
            if (alt != null) {
                if (o.comparingMarker(StudentDetailsXml.ORDER) != null) {
                    return 1;
                }
                if (o.alt != null) {
                    return Collator.getInstance(Locale.getDefault()).compare(alt, o.alt);
                } else {
                    return -1;
                }
            } else {
                if (o.alt == null) {
                    return 0;
                } else {
                    return -1;
                }
            }
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 59 * hash + this.tier;
            hash = 59 * hash + Objects.hashCode(this.alt);
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
            if (!Objects.equals(this.alt, other.alt)) {
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
