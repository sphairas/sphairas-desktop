/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.vcard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.ical.VCard;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "vCard-collection")
@XmlAccessorType(XmlAccessType.FIELD)
public class VCardStudentsCollection {

    @XmlElement(name = "entry")
    private final List<Item> items = new ArrayList<>();

    public void put(StudentId student, VCard card) {
        synchronized (items) {
            final Item found = items.stream()
                    .filter(i -> i.getStudent().equals(student))
                    .collect(CollectionUtil.requireSingleOrNull());
            if (found != null) {
                found.setVCard(card);
            } else {
                items.add(new Item(student, card));
            }
        }
    }

    public Map<StudentId, VCard> getAll() {
        synchronized (items) {
            return items.stream()
                    .collect(Collectors.toMap(Item::getStudent, Item::getVCard));
        }
    }

    public int size() {
        synchronized (items) {
            return items.size();
        }
    }

    static class Item {

        @XmlElement(name = "student")
        private StudentId student;
        @XmlElement(name = "vCard")
        @XmlJavaTypeAdapter(VCardAdapter.class)
        private VCard card;

        public Item() {
        }

        Item(StudentId student, VCard card) {
            this.student = student;
            this.card = card;
        }

        StudentId getStudent() {
            return student;
        }

        VCard getVCard() {
            return card;
        }

        void setVCard(VCard card) {
            this.card = card;
        }

    }

}
