/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmldefinitions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.StreamSupport;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerConvention;
import org.thespheres.betula.document.MarkerParsingException;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.util.PropertyMapAdapter;

/**
 *
 * @author boris.heithecker
 */
@XmlSeeAlso(XmlMarkerDefinition.class)
@XmlRootElement(name = "marker-convention-definition")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlMarkerConventionDefinition extends AbstractXmlConvention<Marker> implements MarkerConvention, Serializable {

    @XmlElement(name = "description")
    private final List<XmlDescription> description = new ArrayList<>();
    @XmlElement(name = "subset")
    private final List<XmlMarkerSubsetDefinition> subsets = new ArrayList<>();
    @XmlElement(name = "properties")
    @XmlJavaTypeAdapter(PropertyMapAdapter.class)
    private final Map<String, String> names = new HashMap<>();

    //JAXB only!
    public XmlMarkerConventionDefinition() {
    }

    public XmlMarkerConventionDefinition(String name) {
        super(name);
        this.subsets.add(new XmlMarkerSubsetDefinition(this, "null"));
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<XmlDescription> getDescription() {
        return description;
    }

    public List<XmlMarkerSubsetDefinition> getMarkerSubsets() {
        return subsets;
    }

    public XmlMarkerSubsetDefinition getMarkerSubset(String subset) {
        return subsets.stream()
                .filter(s -> Objects.equals(s.getSubset(), subset))
                .collect(CollectionUtil.requireSingleOrNull());
    }

    @Override
    public Marker find(String id) {
        return find(id, null);
    }

    @Override
    public final Marker find(final String id, final String subset) {
        return StreamSupport.stream(this.spliterator(), false)
                .filter(m -> Objects.equals(m.getSubset(), subset) && m.getId().equals(id))
                .collect(CollectionUtil.requireSingleOrNull());
    }

    @Override
    public Marker[] getAllMarkers() {
        return StreamSupport.stream(this.spliterator(), false)
                .toArray(Marker[]::new);
    }

    @Override
    public String[] getAllSubsets() {
        return subsets.stream()
                .map(XmlMarkerSubsetDefinition::getSubset)
                .filter(Objects::nonNull)
                .toArray(String[]::new);
    }

    @Override
    public Marker[] getAllMarkers(final String subset) {
        final XmlMarkerSubsetDefinition xmlSet = subsets.stream()
                .filter(s -> Objects.equals(s.getSubset(), subset))
                .collect(CollectionUtil.requireSingleton())
                .orElseThrow(() -> new IllegalArgumentException("Subset " + subset + " not contained in " + getName()));
        return xmlSet.getMarkerDefinitions().stream()
                .toArray(Marker[]::new);
    }

    @Override
    public Iterator<Marker> iterator() {
        class MarkerIterator implements Iterator<Marker> {

            final Iterator<XmlMarkerSubsetDefinition> subsetsIterator = subsets.iterator();
            Iterator<XmlMarkerDefinition> markerIterator;

            MarkerIterator() {
                updateMarkerIterator();
            }

            private void updateMarkerIterator() {
                if (markerIterator == null || !markerIterator.hasNext()) {
                    if (subsetsIterator.hasNext()) {
                        markerIterator = subsetsIterator.next().definitions.iterator();
                    }
                }
            }

            @Override
            public boolean hasNext() {
                updateMarkerIterator();
                return markerIterator.hasNext();
            }

            @Override
            public Marker next() {
                updateMarkerIterator();
                return markerIterator.next();
            }

            @Override
            public void remove() {
                updateMarkerIterator();
                markerIterator.remove();
            }

        }
        return new MarkerIterator();
    }

    @Override
    public Marker parseMarker(String text) throws MarkerParsingException {
        return StreamSupport.stream(this.spliterator(), false)
                .filter(e -> text.equalsIgnoreCase(e.getLongLabel()) || text.equalsIgnoreCase(e.getShortLabel()))
                .findAny().orElseThrow(() -> new MarkerParsingException(getName(), text));

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class XmlMarkerSubsetDefinition implements Serializable {

        @XmlTransient
        XmlMarkerConventionDefinition parent;
        @XmlAttribute(name = "name", required = true)
        private String subset;
        @XmlElement(name = "display-category")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        private String category;
        @XmlElement(name = "description")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        private String subsetDescription;
        @XmlElement(name = "properties")
        @XmlJavaTypeAdapter(PropertyMapAdapter.class)
        private final Map<String, String> names = new HashMap<>();
        @XmlElementWrapper(name = "marker-definitions")
        @XmlElementRef
        private List<XmlMarkerDefinition> definitions = new ArrayList<>();

        //JAXB only!
        public XmlMarkerSubsetDefinition() {
        }

        public XmlMarkerSubsetDefinition(XmlMarkerConventionDefinition parent, String subset) {
            this.parent = parent;
            this.subset = subset;
        }

        public String getSubset() {
            return "null".equals(subset) ? null : subset;
        }

        public XmlMarkerConventionDefinition getConvention() {
            return parent;
        }

        public List<XmlMarkerDefinition> getMarkerDefinitions() {
            return definitions;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getDescription() {
            return subsetDescription;
        }

        public void setDescription(String description) {
            this.subsetDescription = description;
        }

        void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
            this.parent = (XmlMarkerConventionDefinition) parent;
        }
    }
}
