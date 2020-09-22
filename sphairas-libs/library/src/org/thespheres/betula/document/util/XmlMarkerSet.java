/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document.util;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.apache.commons.lang3.StringUtils;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.Marker;

/**
 *
 * @author boris.heithecker
 */
@XmlType(name = "markerSetType", namespace = "http://www.thespheres.org/xsd/betula/betula.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlMarkerSet extends AbstractSet<Marker> implements Serializable {

    private static final long serialVersionUID = 1L;
//    @XmlElementWrapper(name = "markers")
    @XmlElement(name = "marker")
    private final HashSet<MarkerAdapter> elements = new HashSet<>();
    @XmlAttribute(name = "replace-unknown-markers")
    private Boolean replaceUnknownMarkers = false;

    public XmlMarkerSet() {
    }

    public XmlMarkerSet(boolean secureReturn) {
        this.replaceUnknownMarkers = secureReturn ? Boolean.TRUE : null;
    }

    public boolean getReplaceUnknownMarkers() {
        return replaceUnknownMarkers = replaceUnknownMarkers != null ? replaceUnknownMarkers : false;
    }

    private XmlMarkerSet(final MarkerAdapter[] v) {
        elements.addAll(Arrays.asList(v));
    }

    @Override
    public Iterator<Marker> iterator() {
        final Iterator<MarkerAdapter> it = elements.iterator();
        final boolean replacerIfNull = getReplaceUnknownMarkers();
        return new Iterator<Marker>() {

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Marker next() {
                final MarkerAdapter n = it.next();
                Marker ret = n.getMarker();
                if (ret == null && replacerIfNull) {
                    return n.getReplacer();
                }
                return ret;
            }

            @Override
            public void remove() {
                it.remove();
            }

        };
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public boolean add(final Marker marker) {
        final MarkerAdapter e = new MarkerAdapter(marker);
        return elements.add(e);
    }

    public boolean add(final Marker marker, final Action ac) {
        final MarkerAdapter e = new MarkerAdapter(marker);
        e.setAction(ac);
        return elements.add(e);
    }

    public boolean add(final String convention, final String id, final String subset, final Action ac) {
        if (StringUtils.isBlank(convention) || StringUtils.isBlank(id)) {
            return false;
        }
        final MarkerAdapter e = new MarkerAdapter(convention, id, subset);
        if (ac != null) {
            e.setAction(ac);
        }
        return elements.add(e);
    }

    public Set<MarkerAdapter> getAdapterSet() {
        return Collections.unmodifiableSet(elements);
    }

    public static class Adapter extends XmlAdapter<XmlMarkerSet, XmlMarkerSet> {

        public Adapter() {
        }

        @Override
        public XmlMarkerSet unmarshal(XmlMarkerSet v) throws Exception {
            return v;
        }

        @Override
        public XmlMarkerSet marshal(XmlMarkerSet v) throws Exception {
            return v;
        }
    }

}
