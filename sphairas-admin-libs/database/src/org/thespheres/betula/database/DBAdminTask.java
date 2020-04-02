/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import org.thespheres.betula.TermId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "db-admin-task")
//@XmlType(propOrder = {"path", "files", "messages", "version"})
@XmlAccessorType(XmlAccessType.FIELD)
public class DBAdminTask {

    @XmlAttribute(name = "name")
    private String name;
    @XmlAttribute(name = "version")
    private String version;
    @XmlElementWrapper(name = "args")
    @XmlElement(name = "arg")
    private final List<Arg> args = new ArrayList<>();

    public DBAdminTask() {
    }

    public DBAdminTask(String name) {
        this.name = name;
        this.version = "1.0";
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public List<Arg> getArgs() {
        return args;
    }

    public <V> V getArg(final String key, final Class<V> type) {
        return args.stream()
                .filter(a -> a.getKey().equals(key))
                .collect(CollectionUtil.singleton())
                .map(Arg::getValue)
                .filter(type::isInstance)
                .map(type::cast)
                .orElse(null);
    }

    public <V> V getArg(final String key, final Class<V> type, V defaultValue) {
        return args.stream()
                .filter(a -> a.getKey().equals(key))
                .collect(CollectionUtil.singleton())
                .map(Arg::getValue)
                .filter(type::isInstance)
                .map(type::cast)
                .orElse(defaultValue);
    }

    public <V, X extends Exception> V getArg(final String key, final Class<V> type, Supplier<? extends X> exception) throws X {
        return args.stream()
                .filter(a -> a.getKey().equals(key))
                .collect(CollectionUtil.singleton())
                .map(Arg::getValue)
                .filter(type::isInstance)
                .map(type::cast)
                .orElseThrow(exception);
    }

    public <V> List<V> getArgs(final String key, final Class<V> type) {
        return args.stream()
                .filter(a -> a.getKey().equals(key))
                .collect(CollectionUtil.singleton())
                .map(Arg::getValues)
                .map(Arrays::stream)
                .orElse(Stream.empty())
                .filter(type::isInstance)
                .map(type::cast)
                .collect(Collectors.toList());
    }

    public void setArg(final String key, final Object... value) {
        final Iterator<Arg> it = args.iterator();
        while (it.hasNext()) {
            final Arg arg = it.next();
            if (arg.getKey().equals(key)) {
                it.remove();
            }
        }
        args.add(new Arg(key, value));
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Arg {

        @XmlAttribute(name = "key")
        private String key;

//        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlElements(value = {
            @XmlElement(name = "string-value", type = String.class),
            @XmlElement(name = "int-value", type = Integer.class),
            @XmlElement(name = "term-value", type = TermId.class),
            @XmlElement(name = "document-value", type = DocumentId.class)
        })
        private Object[] value;

        public Arg() {
        }

        Arg(String key, Object[] value) {
            this.key = key;
            this.value = value;
        }

        String getKey() {
            return key;
        }

        Object getValue() {
            return (value != null && value.length == 1) ? value[0] : null;
        }

        Object[] getValues() {
            return value;
        }
    }
}
