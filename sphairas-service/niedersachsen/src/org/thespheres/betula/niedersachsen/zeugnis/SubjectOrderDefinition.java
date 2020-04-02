/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.zeugnis;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.util.MarkerAdapter;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "faecher")
@XmlAccessorType(XmlAccessType.FIELD)
public class SubjectOrderDefinition implements Comparator<Marker> {

    @XmlAttribute(name = "id")
    private String id;

    @XmlElement(name = "fach")
    private final SortedSet<Item> items = new TreeSet<>();

    public static SubjectOrderDefinition load(InputStream is) throws IOException {
        try {
            JAXBContext ctx = JAXBContext.newInstance(SubjectOrderDefinition.class);
            return (SubjectOrderDefinition) ctx.createUnmarshaller().unmarshal(is);
        } catch (JAXBException ex) {
            throw new IOException(ex);
        }
    }

    public int positionOf(final Marker fach) {
        return items.stream()
                .filter(i -> i.subjectEquals(fach)).findAny()
                .map(Item::position)
                .orElse(Integer.MAX_VALUE);
    }

    public List<SubjectOrderDefinition.Item> getSubjects() {
        return items.stream()
                .collect(Collectors.toList());
    }

    public SubjectOrderDefinition.Item findSubject(final Marker fach) {
        return items.stream()
                .filter(i -> i.subjectEquals(fach))
                .collect(CollectionUtil.singleOrNull());
    }

    @Override
    public int compare(Marker o1, Marker o2) {
        return positionOf(o1) - positionOf(o2);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Item implements Comparable<Item> {

        @XmlAttribute(name = "position", required = true)
        private int position;
        @XmlJavaTypeAdapter(MarkerAdapter.XmlMarkerAdapter.class)
        @XmlElement(name = "marker")
        private Marker subject;

        @Override
        public int compareTo(Item o) {
            return position - o.position;
        }

        private boolean subjectEquals(Marker other) {
            return this.subject.equals(other);
        }

        private int position() {
            return position;
        }

        public Marker getSubject() {
            return subject;
        }

    }
}
