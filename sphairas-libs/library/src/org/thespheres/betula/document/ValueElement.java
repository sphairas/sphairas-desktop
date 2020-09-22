/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.util.MarkerAdapter;
import org.thespheres.betula.util.GradeAdapter;

/**
 *
 * @author boris.heithecker
 * @param <V>
 */
@XmlType(name = "valueEntryType",
        propOrder = {"action", "value", "description"})
@XmlAccessorType(XmlAccessType.FIELD)
public class ValueElement<V> implements Serializable {

    private static final long serialVersionUID = 1L;
    @XmlElementWrapper(name = "descriptions")
    @XmlElement(name = "description")
    protected List<Description> description;
    @XmlAttribute(name = "action")
    private Action action;
    @XmlElements({
        @XmlElement(name = "text", type = String.class),
        @XmlElement(name = "int-value", type = Integer.class),
        @XmlElement(name = "double-value", type = Double.class),
        @XmlElement(name = "grade", type = GradeAdapter.class, namespace = "http://www.thespheres.org/xsd/betula/betula.xsd"),
        @XmlElement(name = "marker", type = MarkerAdapter.class, namespace = "http://www.thespheres.org/xsd/betula/betula.xsd")
    })
    private V value;

    public ValueElement() {
    }

    public ValueElement(Action action) {
        this(null, action);
    }

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    public ValueElement(V value, Action action) {
        this.action = action;
        setValue(value);
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action method) {
        this.action = method;
    }

    public V getValue() {
        if (value instanceof MarkerAdapter) {
            return (V) ((MarkerAdapter) value).getMarker();
        } else if (value instanceof GradeAdapter) {
            return (V) ((GradeAdapter) value).getGrade();
        }
        return value;
    }

    public Object rawValue() {
        return value;
    }

    public void setValue(V value) {
        setValueImpl(value);
    }

    protected final Object setValueImpl(V val) throws IllegalArgumentException {
        if (val != null && !(val instanceof String
                || val instanceof Integer
                || val instanceof Double
                || val instanceof Grade
                || val instanceof Marker)) {
            throw new IllegalArgumentException();
        }
        if (val instanceof Grade) {
            val = (V) new GradeAdapter((Grade) val);
        } else if (val instanceof Marker) {
            val = (V) new MarkerAdapter((Marker) val);
        }
        final Object oldValue = value;
        this.value = val;
        return oldValue;
    }

    public List<Description> getDescription() {
        if (description == null) {
            description = new ArrayList<>();
        }
        return description;
    }

    public void beforeMarshal(Marshaller marshaller) throws JAXBException {
        if (description != null && description.isEmpty()) {
            description = null;
        }
    }
}
