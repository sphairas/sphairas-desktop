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

    public static final long serialVersionUID = 1L;
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

    public void addElement(int index, Marker[] markers, int defaultValue, boolean nillable, String displayName) {
        addElement(index, markers, false, nillable, defaultValue, displayName);
    }

    public void addElement(int index, Marker[] markers, String displayName) {
        addElement(index, markers, true, true, 0, displayName);
    }

    private void addElement(int index, Marker[] markers, boolean multiple, boolean nillable, int defaultValue, String displayName) {
        synchronized (elements) {
            if (index < 0 || index > elements.size() || markers == null) {
                throw new IllegalArgumentException();
            }
            if (!multiple && defaultValue >= markers.length) {
                throw new IllegalArgumentException();
            }
            if (multiple && !nillable || multiple && defaultValue != 0) {
                throw new IllegalArgumentException();
            }
            final Element el = new Element(markers, multiple, nillable, defaultValue, displayName);
            elements.add(index, el);
        }
    }

    public void removeElement(final Element el) {
        final int i = this.elements.indexOf(el);
        if (i != -1) {
            this.elements.remove(i);
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Element implements Serializable {
        
        public static final long serialVersionUID = 1L;
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

        private Element(Marker[] m, boolean multiple, boolean nillable, int defaultIndex, String elementDisplayName) {
//            this.position = position;
            this.multiple = multiple;
            this.defaultIndex = defaultIndex;
            this.elementDisplayName = elementDisplayName;
            this.nillable = nillable;
            Arrays.stream(m)
                    .map(ma -> new MarkerItem(ma, this))
                    .forEach(markers::add);
            updateNillable();
        }

        private void updateNillable() {
            final boolean hasNull = markers.size() > 0 && Marker.isNull(markers.get(0).getMarker());
            if (nillable && !hasNull) {
                markers.add(0, new MarkerItem(Marker.NULL, this));
            } else if (!nillable && hasNull) {
                markers.remove(0);
            }
        }

        public MarkerItem addItem(final int index, final Marker m) {
            final MarkerItem ret = new MarkerItem(m, this);
            this.markers.add(index, ret);
            return ret;
        }

        public MarkerItem addItem(final Marker m) {
            final MarkerItem ret = new MarkerItem(m, this);
            this.markers.add(ret);
            return ret;
        }

        public boolean removeItem(final MarkerItem item) {
            final int i = this.markers.indexOf(item);
            if (i != -1) {
                if (this.defaultIndex == i) {
                    this.defaultIndex = 0;
                }
                return this.markers.remove(item);
            }
            return false;
        }

        public void beforeMarshal(final Marshaller marshaller) {
            final ArrayList<MarkerItem> l = new ArrayList<>();
            for (int i = nillable ? 1 : 0; i < markers.size(); i++) {
                l.add(new MarkerItem(markers.get(i), this));
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
                markers.add(0, new MarkerItem(Marker.NULL, this));
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

        public void setMultiple(final boolean multiple) {
            this.multiple = multiple;
        }

        public boolean isNillable() {
            return nillable;
        }

        public void setNillable(final boolean nillable) {
            this.nillable = nillable;
//            this.multiple = false;
            updateNillable();
        }

        @Deprecated
        public int getDefaultElement() {
            return defaultIndex;
        }

        public int getDefaultIndex() {
            return defaultIndex;
        }

        public MarkerItem getDefaultItem() {
            return (this.multiple || this.markers.size() < defaultIndex) ? null : this.markers.get(defaultIndex);
        }

        void setDefaultIndex(final int index) {
            this.defaultIndex = index;
        }

        public String getElementDisplayName() {
            return elementDisplayName;
        }

        public void setElementDisplayName(final String elementDisplayName) {
            this.elementDisplayName = elementDisplayName;
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
        @XmlTransient
        private Element parent;

        public MarkerItem() {
        }

        @SuppressWarnings({"OverridableMethodCallInConstructor"})
        protected MarkerItem(final MarkerItem copy, final Element parent) {
            super(copy.getMarker());
            this.parent = parent;
            setAction(copy.getAction());
            setHidden(copy.isHidden());
            copy.getDisplayHint().stream()
                    .forEach(displayHint::add);
        }

        private MarkerItem(final Marker orig, final Element parent) {
            super(orig);
            this.parent = parent;
        }

        public void afterUnmarshal(final Unmarshaller u, final Object parent) {
            this.parent = (Element) parent;
        }

        @Override
        public Marker getMarker() {
            return super.getMarker(true);
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

        public boolean isDefaultItem() {
            return !this.parent.isMultiple() && this.parent.markers.indexOf(this) == parent.defaultIndex;
        }

        public void setDefaultItem() {
            final int i = this.parent.markers.indexOf(this);
            if (i != -1) {
                this.parent.setDefaultIndex(i);
            }
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
