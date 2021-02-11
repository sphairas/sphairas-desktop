/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tableimport.csv;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.tableimport.csv.XmlCsvFile.Column;
import org.thespheres.betula.tableimport.csv.XmlCsvFile.Line;
import org.thespheres.betula.tableimport.csv.XmlCsvFile.Value;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.util.Utilities;

/**
 *
 * @author boris.heithecker
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlCsvFileGroupingKey {

    @XmlID
    @XmlAttribute(name = "grouping-id", required = true)
    private String name;
    @XmlTransient
    private Map<String, Optional<String>> keyValuePairs;
    @XmlElement(name = "key-value")
    private XmlCsvFileGroupingKeyElementValue[] elements;

    public XmlCsvFileGroupingKey() {
        keyValuePairs = new HashMap<>();
    }

    private XmlCsvFileGroupingKey(Map<String, Optional<String>> m) {
        this.keyValuePairs = m;
    }

    public String getName() {
        return name;
    }

    private void setIndex(final int index) {
        this.name = Utilities.createId(index);
    }

    public static void group(final XmlCsvFile file) {
        final String[] assignedKeys = Arrays.stream(file.getColumns())
                .filter(XmlCsvFile.Column::isGroupingColumn)
                .map(Column::getAssignedKey)
                .toArray(String[]::new);
        if (assignedKeys.length == 0) {
            file.setKeys(null);
            return;
        }
        final AtomicInteger count = new AtomicInteger(0);
        final Map<XmlCsvFileGroupingKey, List<Line>> ret = Arrays.stream(file.getLines())
                .collect(Collectors.groupingBy(l -> createKey(l, assignedKeys)));
        final XmlCsvFileGroupingKey[] key = ret.keySet().stream()
                .toArray(XmlCsvFileGroupingKey[]::new);
        ret.forEach((k, ll) -> {
            k.setIndex(count.getAndIncrement());
            ll.forEach(l -> l.setGroupingKey(k));
        });
        file.setKeys(key);
    }

    private static XmlCsvFileGroupingKey createKey(final Line l, final String[] assignedKeys) {
        final Map<String, Optional<String>> m = Arrays.stream(assignedKeys)
                .collect(Collectors.toMap(Function.identity(), ak -> {
                    return Arrays.stream(l.getValues())
                            .filter(v -> Objects.equals(v.getColumn().getAssignedKey(), ak))
                            .map(Value::getValue)
                            .collect(CollectionUtil.singleton());
                }));
        return new XmlCsvFileGroupingKey(m);
    }

    public void beforeMarshal(Marshaller marshaller) {
        elements = keyValuePairs.entrySet().stream()
                .map(XmlCsvFileGroupingKeyElementValue::new)
                .toArray(XmlCsvFileGroupingKeyElementValue[]::new);
    }

    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        if (elements != null) {
            Arrays.stream(elements)
                    .forEach(e -> keyValuePairs.put(e.assignedKey, e.value == null || "null".equals(e.value) ? Optional.empty() : Optional.of(e.value)));
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return 71 * hash + Objects.hashCode(this.keyValuePairs);
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
        final XmlCsvFileGroupingKey other = (XmlCsvFileGroupingKey) obj;
        return Objects.equals(this.keyValuePairs, other.keyValuePairs);
    }

    @XmlAccessorType(value = XmlAccessType.FIELD)
    static class XmlCsvFileGroupingKeyElementValue {

        @XmlAttribute(name = "assigned-key", required = true)
        private String assignedKey;
        @XmlValue
        @XmlJavaTypeAdapter(value = CollapsedStringAdapter.class)
        private String value;

        public XmlCsvFileGroupingKeyElementValue() {
        }

        private XmlCsvFileGroupingKeyElementValue(Map.Entry<String, Optional<String>> e) {
            this.assignedKey = e.getKey();
            this.value = e.getValue().orElse("null");
        }

    }
}
