/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.util;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author boris.heithecker
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class PropertyMapAdapter extends XmlAdapter<PropertyMapAdapter, Map<String, String>> {

    @XmlElement(name = "property")
    private PropertyEntry[] entries;

    public PropertyMapAdapter() {
        this.entries = new PropertyEntry[0];
    }

    PropertyMapAdapter(final Map<String, String> props) {
        entries = props.entrySet().stream()
                .map(e -> new PropertyEntry(e.getKey(), e.getValue()))
                .toArray(PropertyEntry[]::new);
    }

    @Override
    public Map<String, String> unmarshal(final PropertyMapAdapter v) throws Exception {
        return Arrays.stream(v.entries)
                .collect(Collectors.toMap(e -> e.key, e -> e.val));
    }

    @Override
    public PropertyMapAdapter marshal(final Map<String, String> v) throws Exception {
        return new PropertyMapAdapter(v);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class PropertyEntry {

        @XmlAttribute(name = "name", required = true)
        private String key;
        @XmlValue
        private String val;

        public PropertyEntry() {
        }

        PropertyEntry(String key, String value) {
            this.key = key;
            this.val = value;
        }

    }

}
