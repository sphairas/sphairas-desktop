/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import org.openide.util.Lookup;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.util.Ordered;

/**
 *
 * @author boris.heithecker
 * @param <P>
 */
public abstract class XmlAssessmentProviderData<P extends AssessmentProvider> implements Ordered {

    @XmlElementWrapper(name = "properties")
    @XmlElement(name = "property")
    private final List<XmlDataProperty> properties = new ArrayList<>();

    public Boolean getBooleanProperty(String name) {
        return properties.stream()
                .filter(p -> p.getName().equals(name))
                .collect(CollectionUtil.requireSingleton())
                .map(p -> p.getBooleanValue())
                .orElse(null);
    }

    public void putBooleanProperty(String name, Boolean value) {
        XmlDataProperty prop = findProperty(name);
        if (prop == null) {
            properties.add(new XmlDataProperty(name, value));
        } else if (value == null) {
            properties.remove(prop);
        } else {
            prop.bool = value;
        }
    }

    private XmlDataProperty findProperty(String name) {
        final XmlDataProperty prop = properties.stream()
                .filter(p -> p.getName().equals(name))
                .collect(CollectionUtil.requireSingleOrNull());
        return prop;
    }

    public String getStringProperty(String name) {
        return properties.stream()
                .filter(p -> p.getName().equals(name))
                .collect(CollectionUtil.requireSingleton())
                .map(p -> p.getStringValue())
                .orElse(null);
    }

    public void putStringProperty(String name, String value) {
        XmlDataProperty prop = findProperty(name);
        if (prop == null) {
            properties.add(new XmlDataProperty(name, value));
        } else if (value == null) {
            properties.remove(prop);
        } else {
            prop.text = value;
        }
    }

    public Integer getIntegerProperty(String name) {
        return properties.stream()
                .filter(p -> p.getName().equals(name))
                .collect(CollectionUtil.requireSingleton())
                .map(p -> p.getIntegerValue())
                .orElse(null);
    }

    public void putIntegerProperty(String name, Integer value) {
        XmlDataProperty prop = findProperty(name);
        if (prop == null) {
            properties.add(new XmlDataProperty(name, value));
        } else if (value == null) {
            properties.remove(prop);
        } else {
            prop.integer = value;
        }
    }

    public abstract P createAssessmentProvider(Lookup context);

    public abstract void setPosition(int pos);

    @XmlAccessorType(XmlAccessType.FIELD)
    static final class XmlDataProperty {

        @XmlAttribute(name = "property-name", required = true)
        private String name;
        @XmlAttribute(name = "boolean-value")
        private Boolean bool;
        @XmlAttribute(name = "string-value")
        private String text;
        @XmlAttribute(name = "integer-value")
        private Integer integer;

        public XmlDataProperty(String name, boolean bool) {
            this(name, bool, null, null);
        }

        public XmlDataProperty(String name, String text) {
            this(name, null, text, null);
        }

        public XmlDataProperty(String name, int number) {
            this(name, null, null, number);
        }

        private XmlDataProperty(String name, Boolean bool, String text, Integer integer) {
            this.name = name;
            this.bool = bool;
            this.text = text;
            this.integer = integer;
        }

        public String getName() {
            return name;
        }

        public Boolean getBooleanValue() {
            return bool;
        }

        public String getStringValue() {
            return text;
        }

        public Integer getIntegerValue() {
            return integer;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + Objects.hashCode(this.name);
            return hash;
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
            final XmlDataProperty other = (XmlDataProperty) obj;
            return Objects.equals(this.name, other.name);
        }

    }
}
