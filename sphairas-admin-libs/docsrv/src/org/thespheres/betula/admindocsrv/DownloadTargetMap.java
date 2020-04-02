/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admindocsrv;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "files")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class DownloadTargetMap {

    public static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd' 'HH:mm:ss");
    @XmlElement(name = "file")
    private final List<Item> items = new ArrayList<>();

    public List<Item> getItems() {
        return items;
    }

    Item find(final String fileName) {
        return items.stream()
                .filter(i -> fileName.equals(i.getFile()))
                .collect(CollectionUtil.requireSingleOrNull());
    }

    List<String> forOwner(final String owner) {
        return items.stream()
                .filter(i -> owner.equals(i.getOwner()))
                .map(Item::getFile)
                .collect(Collectors.toList());
    }

    boolean set(String fileName, String owner, LocalDateTime lfdt) {
        final Item existing = find(fileName);
        boolean changed = false;
        if (existing != null) {
            existing.time = lfdt.format(DTF);
            if (!Objects.equals(existing.owner, owner)) {
                existing.owner = owner;
                changed = true;
            }
        } else {
            items.add(new Item(fileName, owner, lfdt));
            changed = true;
        }
        return changed;
    }

    boolean remove(final String file) {
        final Iterator<Item> iterator = items.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getFile().equals(file)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    @XmlAccessorType(value = XmlAccessType.FIELD)
    public static class Item {

        @XmlAttribute(name = "file")
        private String file;
        @XmlAttribute(name = "owner")
        private String owner;
        @XmlAttribute(name = "time")
        private String time;

        public Item() {
        }

        Item(String file, String owner, LocalDateTime lfdt) {
            this.file = file;
            this.owner = owner;
            this.time = lfdt.format(DTF);
        }

        public String getFile() {
            return file;
        }

        public String getOwner() {
            return owner;
        }

        public LocalDateTime getTime() {
            return LocalDateTime.parse(time, DTF);
        }

    }
}
