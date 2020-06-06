/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.commons.lang3.StringUtils;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
@XmlType(name = "xmlContentType",
        namespace = "http://www.thespheres.org/xsd/betula/container.xsd",
        propOrder = {"xmlContent", "markerSet"})
@XmlAccessorType(XmlAccessType.FIELD)
abstract class XmlContent implements Serializable {

    private static final long serialVersionUID = 1L;
    @XmlElements({
        @XmlElement(name = "text-content", type = XmlTextContentElement.class),
        @XmlElement(name = "element-content", type = XmlElementContentElement.class)})
    private List<XmlContentElement> xmlContent;
    @XmlJavaTypeAdapter(value = XmlMarkerSet.Adapter.class)
    @XmlElement(name = "markers")
    private XmlMarkerSet markerSet;

    protected XmlContent() {
        super();
    }

    private List<XmlContentElement> getXmlContent() {
        if (xmlContent == null) {
            xmlContent = new ArrayList<>();
        }
        return xmlContent;
    }

    private Object getContentNode(String key) {
        for (XmlContentElement e : getXmlContent()) {
            if (e.getKey().equals(key)) {
                return e.getNode();
            }
        }
        return null;
    }

    public String getContentString(String key) {
        final Object v = getContentNode(key);
        return v instanceof String ? (String) v : null;
    }

    public org.w3c.dom.Element getContentElement(String key) {
        Object v = getContentNode(key);
        return v instanceof org.w3c.dom.Element ? (org.w3c.dom.Element) v : null;
    }

    void setContent(final String key, final String text) {
        if (StringUtils.isBlank(text)) {
            removeContent(key);
        } else {
            XmlContentElement el = findElement(key);
            if (!(el instanceof XmlTextContentElement)) {
                if (el != null) {
                    getXmlContent().remove(el);
                }
                el = new XmlTextContentElement(key);
                getXmlContent().add(el);
            }
            el.setNode(text);
        }
    }

    void setContent(final String key, final org.w3c.dom.Element node) {
        if (node == null) {
            removeContent(key);
        } else {
            XmlContentElement el = findElement(key);
            if (!(el instanceof XmlElementContentElement)) {
                if (el != null) {
                    getXmlContent().remove(el);
                }
                el = new XmlElementContentElement(key);
                getXmlContent().add(el);
            }
            el.setNode(node);
        }
    }

    private void removeContent(final String key) {
        final XmlContentElement el = findElement(key);
        if (el != null) {
            getXmlContent().remove(el);
        }
    }

    private XmlContentElement findElement(final String key) throws IllegalArgumentException {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("Empty key.");
        }
        XmlContentElement el = getXmlContent().stream()
                .filter(e -> e.getKey().equals(key))
                .collect(CollectionUtil.requireSingleOrNull());
        return el;
    }

    public Set<Marker> getMarkerSet() {
        if (markerSet == null) {
            markerSet = new XmlMarkerSet(true);
        }
        return markerSet;
    }

    public Marker[] markers() {
        final XmlMarkerSet ms = markerSet;
        if (ms == null) {
            return new Marker[0];
        }
        synchronized (ms) {
            return ms.stream().toArray(Marker[]::new);
        }
    }

    public void beforeMarshal(Marshaller marshaller) throws JAXBException {
        if (markerSet != null && markerSet.isEmpty()) {
            markerSet = null;
        }
    }

}
