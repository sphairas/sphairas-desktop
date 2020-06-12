/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document.util;

import java.io.Serializable;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.apache.commons.lang3.StringUtils;
import org.thespheres.betula.document.AbstractMarker;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerFactory;
import org.thespheres.betula.tag.TagAdapter;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "marker", namespace = "http://www.thespheres.org/xsd/betula/betula.xsd")
@XmlType(name = "markerAdapterType", namespace = "http://www.thespheres.org/xsd/betula/betula.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class MarkerAdapter extends TagAdapter implements Serializable {

    public static final long serialVersionUID = 1L;
    @XmlAttribute(name = "subset")
    protected String subset = null;

    public MarkerAdapter() {
    }

    public MarkerAdapter(Marker marker) {
        super(marker);
        this.subset = marker.getSubset();
    }

    public MarkerAdapter(String convention, String id, String subset) {
        super(convention, id);
        this.subset = StringUtils.trimToNull(subset);
    }

    public String getSubset() {
        return subset;
    }

    public Marker getMarker() {
        return getMarker(false);
    }

    public Marker getReplacer() {
        return new AbstractMarker(getConvention(), getId(), getSubset());
    }

    public Marker getMarker(final boolean replace) {
        if (getConvention().equals("null") && getId().equals("null") && getSubset() == null) {
            return Marker.NULL;
        }
        final Marker marker;
        try {
            marker = MarkerFactory.find(getConvention(), getId(), getSubset());
        } catch (final Exception ex) {
            if (replace) {
                final String msg = "";
                Logger.getLogger(MarkerAdapter.class.getName()).log(Level.WARNING, msg, ex);
                return getReplacer();
            }
            throw ex;
        }
        if (marker == null && replace) {
            return getReplacer();
        }
        return marker;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.convention);
        hash = 29 * hash + Objects.hashCode(this.id);
        return 29 * hash + Objects.hashCode(this.subset);
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
        final MarkerAdapter other = (MarkerAdapter) obj;
        if (!Objects.equals(this.convention, other.getConvention())) {
            return false;
        }
        if (!Objects.equals(this.id, other.getId())) {
            return false;
        }
        return Objects.equals(this.subset, other.getSubset());
    }

    public static class XmlMarkerAdapter extends XmlAdapter<MarkerAdapter, Marker> {

        public XmlMarkerAdapter() {
        }

        @Override
        public Marker unmarshal(final MarkerAdapter v) throws Exception {
            return v != null ? v.getMarker() : null;
        }

        @Override
        public MarkerAdapter marshal(final Marker v) throws Exception {
            return v != null ? new MarkerAdapter(v) : null;
        }
    }
}
