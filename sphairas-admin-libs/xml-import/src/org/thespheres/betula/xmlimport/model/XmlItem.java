/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.model;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collector;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlValue;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class XmlItem {

    @XmlIDREF
    @XmlAttribute(name = "product")
    protected Product origin;
    @XmlAttribute(name = "position")
    protected Integer position;
    @XmlElement(name = "label")
    private String sourceLabel;
    @XmlAnyElement(lax = true)
    private List<Object> any;

    protected XmlItem() {
    }

    protected XmlItem(Product origin) {
        this.origin = origin;
    }

    public Product getProduct() {
        return origin;
    }

    public Integer getPosition() {
        return position;
    }

    public String getSourceLabel() {
        return sourceLabel;
    }

    public final <P> P getItemProperty(Class<P> clz) {
        if (any != null) {
            return any.stream()
                    .filter(o -> clz.isAssignableFrom(o.getClass()))
                    .map(clz::cast)
                    .collect(CollectionUtil.singleOrNull());
        } else {
            return null;
        }
    }

    public final <P, C extends Collection<P>> C getItemProperties(Class<P> clz, Collector<P, ?, C> collector) {
        final List<Object> l = any != null ? any : Collections.EMPTY_LIST;
        return l.stream()
                .filter(o -> clz.isAssignableFrom(o.getClass()))
                .map(clz::cast)
                .collect(collector);
    }

    public static class SourceElement {

        @XmlAttribute(name = "source-definition")
        private String definition;
        @XmlValue
        private String value;

        public String getSourceDefinition() {
            return definition;
        }

        public String getValue() {
            return value;
        }

    }

    public static class SourceDateTime {

        @XmlAttribute(name = "source-format")
        private String format;
        @XmlValue
        private String date;
        public static final DateTimeFormatter SQL = new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd HH:mm:ss")
                .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
                .toFormatter();

        public String getFormat() {
            return format;
        }

        public String getSourceDate() {
            return date;
        }

    }
}
