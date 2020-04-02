/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author boris.heithecker
 */
//If AbstractGrade is not Serializable, will not work correctly with ejb remote serialization
public class AbstractMarker implements Marker, Serializable {

    protected String markerConvention;
    protected String markerId;
    protected String subset;

    public AbstractMarker(String convention, String id, String subset) {
        this.markerConvention = convention;
        this.markerId = id;
        this.subset = subset;
    }

    @Override
    public String getConvention() {
        return markerConvention;
    }

    @Override
    public String getSubset() {
        return subset;
    }

    @Override
    public String getId() {
        return markerId;
    }

    @Override
    public String getLongLabel(Object... formattingArgs) {
        return "{" + getConvention() + "}" + getId();
    }

    @Override
    public String getShortLabel() {
        return getLongLabel();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.markerConvention);
        hash = 23 * hash + Objects.hashCode(this.markerId);
        return 23 * hash + Objects.hashCode(this.subset);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Marker)) {
            return false;
        }
        final Marker other = (Marker) obj;
        if (!Objects.equals(this.markerConvention, other.getConvention())) {
            return false;
        }
        if (!Objects.equals(this.markerId, other.getId())) {
            return false;
        }
        return Objects.equals(this.subset, other.getSubset());
    }

    public Object writeReplace() {
        return new Replacer(markerConvention, markerId, subset);
    }

    @Override
    public String toString() {
        return "{" + markerConvention + "}" + subset + ":" + markerId;
    }

    public static class Replacer implements Serializable {

        private final String convention;
        private final String id;
        private final String subset;

        public Replacer(String convention, String id, String subset) {
            this.convention = convention;
            this.id = id;
            this.subset = subset;
        }

        public Object readResolve() {
            Marker ret = null;
            if (id != null && convention != null) {
                ret = MarkerFactory.find(convention, id, subset);
            }
            if (ret == null) {
                ret = new AbstractMarker(convention, id, subset);
            }
            return ret;
        }
    }
}
