/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.zeugnis;

import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.Tag;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.util.MarkerAdapter;
import org.thespheres.betula.niedersachsen.zeugnis.TermReportNoteSetTemplate.Element;
import org.thespheres.betula.tag.AbstractTag;
import org.thespheres.betula.tag.TagAdapter;
import org.thespheres.betula.tag.TagAdapter.XmlTagAdapter;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "zeugnis-bemerkungs-vorlage")
@XmlAccessorType(XmlAccessType.FIELD)
public class TermReportNoteSetTemplate implements Serializable {

    @XmlElement(name = "vorlagen-id", required = true)
    private String id;
    @XmlElement(name = "anzeigename")
    private String displayName;
    @XmlElement(name = "beschreibung")
    private String description;
    @XmlElement(name = "element")
    private final List<Element> elements = new ArrayList<>();

    public TermReportNoteSetTemplate() {
    }

    public TermReportNoteSetTemplate(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Element> getElements() {
        return elements;
    }

    public void addElement(int index, Marker[] markers, int defaultValue, String displayName) {
        addElement(index, markers, false, defaultValue, displayName);
    }

    public void addElement(int index, Marker[] markers, String displayName) {
        addElement(index, markers, true, 0, displayName);
    }

    private void addElement(int index, Marker[] markers, boolean multiple, int defaultValue, String displayName) {
        synchronized (elements) {
            if (index < 0 || index > elements.size() || markers == null || markers.length == 0) {
                throw new IllegalArgumentException();
            }
            if (defaultValue >= markers.length) {
                throw new IllegalArgumentException();
            }
            Element el = new Element(markers, multiple, defaultValue, displayName);
            elements.add(index, el);
        }
    }

    public void removeElement(int position) {

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Element implements Serializable {

        @XmlAttribute(name = "multiple")
        private boolean multiple = true;
        @XmlAttribute(name = "default")
        private int defaultIndex;
        @XmlAttribute(name = "nillable")
        private boolean nillable;
        @XmlElement(name = "element-anzeigename")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        private String elementDisplayName;
        @XmlElement(name = "marker")
        private ArrayList<MarkerItem> xmlMarkers;
        @XmlTransient
        private final ArrayList<MarkerItem> markers = new ArrayList<>();
        @XmlAttribute(name = "hidden")
        private Boolean elementHidden;
        @XmlElement(name = "display-value")
        @XmlJavaTypeAdapter(XmlTagAdapterExt.class)
        private final List<Tag> displayValue = new ArrayList<>();
        @Deprecated
        private transient List<String> selectedMultiple = new ArrayList<>();
        @Deprecated
        private transient String selected;
        @Deprecated
        @XmlTransient
        private final VetoableChangeSupport pSupport = new VetoableChangeSupport(this);

        public Element() {
        }

        private Element(Marker[] m, boolean multiple, int defaultIndex, String elementDisplayName) {
//            this.position = position;
            this.multiple = multiple;
            this.defaultIndex = defaultIndex;
            this.elementDisplayName = elementDisplayName;
            if (nillable) {
                markers.add(new MarkerItem(Marker.NULL));
            }
            Arrays.stream(m)
                    .map(MarkerItem::new)
                    .forEach(markers::add);
        }

        public MarkerItem addItem(final int index, final Marker m) {
            final MarkerItem ret = new MarkerItem(m);
            this.markers.add(index, ret);
            return ret;
        }

        public boolean removeItem(final MarkerItem item) {
            return this.markers.remove(item);
        }

        public void beforeMarshal(final Marshaller marshaller) {
            final ArrayList<MarkerItem> l = new ArrayList<>();
            for (int i = nillable ? 1 : 0; i < markers.size(); i++) {
                l.add(new MarkerItem(markers.get(i)));
            }
            xmlMarkers = l;
        }

        public void afterMarshal(final Marshaller marshaller) {
            xmlMarkers = null;
        }

        public void afterUnmarshal(Unmarshaller u, Object parent) {
            if (xmlMarkers != null) {
                xmlMarkers.stream()
                        .forEach(markers::add);
                xmlMarkers = null;
            }
            if (nillable && !multiple) {
                markers.add(0, new MarkerItem(Marker.NULL));
                selected = markers.get(0).getId();
            }
            if (!nillable && !multiple) {
                try {
                    selected = getMarkers().get(getDefaultElement()).getId();
                } catch (IndexOutOfBoundsException e) {
                    final String msg = "Default index " + Integer.toString(getDefaultElement()) + " cannot be selected in " + elementDisplayName;
                    Logger.getLogger(TermReportNoteSetTemplate.class.getCanonicalName()).log(Level.FINE, msg, e);
                }
            }
        }

        public boolean isMultiple() {
            return multiple;
        }

        public boolean isNillable() {
            return nillable;
        }

        public int getDefaultElement() {
            return defaultIndex;
        }

        public String getElementDisplayName() {
            return elementDisplayName;
        }

        public boolean isHidden() {
            return elementHidden != null && elementHidden;
        }

        public void setHidden(boolean hidden) {
            this.elementHidden = hidden ? Boolean.TRUE : null;
        }

        public List<MarkerItem> getMarkers() {
            return markers;
        }

        public boolean containsMarker(final Marker m) {
            return getMarkers().stream()
                    .map(MarkerAdapter::getMarker)
                    .filter(Objects::nonNull)
                    .anyMatch(m::equals);
        }

        @Deprecated
        public String getSelectedItem() {
            return selected;
        }

        @Deprecated
        public List<String> getSelected() {
            return selectedMultiple;
        }

        @Deprecated
        public void setSelectedItem(String value) {
            String old = selected;
            try {
                selected = value;
                pSupport.fireVetoableChange("selectedItem", old, value);
            } catch (PropertyVetoException ex) {
                selected = old;
            }
        }

        @Deprecated
        public void setSelected(List<String> value) {
            if (value == null) {
                value = Collections.EMPTY_LIST;
            }
            List<String> old = getSelected();
            try {
                selectedMultiple = value;
                pSupport.fireVetoableChange("selected", old, value);
            } catch (PropertyVetoException ex) {
                selectedMultiple = old;
            }
        }

        public Marker forId(final String id) {
            return getMarkers().stream()
                    .filter(m -> m.getId().equals(id))
                    .map(m -> m.getMarker())
                    .findAny()
                    .orElse(null);
        }

        public List<Tag> getDisplayHints() {
            return displayValue;
        }

        @Deprecated
        public void addVetoableChangeListener(VetoableChangeListener l) {
            pSupport.addVetoableChangeListener(l);
        }

        @Deprecated
        public void removeVetoableChangeListener(VetoableChangeListener l) {
            pSupport.removeVetoableChangeListener(l);
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class MarkerItem extends MarkerAdapter implements Serializable {

        public static final long serialVersionUID = 1L;
        @XmlElement(name = "display-value")
        @XmlJavaTypeAdapter(XmlTagAdapter.class)
        private final List<Tag> displayHint = new ArrayList<>();
        @XmlAttribute(name = "hidden")
        private Boolean hidden;

        public MarkerItem() {
        }

        @SuppressWarnings({"OverridableMethodCallInConstructor"})
        protected MarkerItem(final MarkerItem copy) {
            super(copy.getMarker());
            setAction(copy.getAction());
            setHidden(copy.isHidden());
            copy.getDisplayHint().stream()
                    .forEach(displayHint::add);
        }

        private MarkerItem(final Marker orig) {
            super(orig);
        }

        public boolean isHidden() {
            return hidden != null && hidden;
        }

        public void setHidden(boolean hidden) {
            this.hidden = hidden ? Boolean.TRUE : null;
        }

        public List<Tag> getDisplayHint() {
            return displayHint;
        }

    }

    public static class XmlTagAdapterExt extends XmlTagAdapter {

        @Override
        public Tag unmarshal(TagAdapter v) throws Exception {
            final Tag ret = super.unmarshal(v);
            if (ret != null) {
                return ret;
            }
            return new AbstractTag(v.getConvention(), v.getId());
        }

    }

}
