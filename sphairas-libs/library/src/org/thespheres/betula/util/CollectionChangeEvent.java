/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.util;

import java.util.EventObject;
import java.util.Optional;

/**
 *
 * @author boris.heithecker
 */
public class CollectionChangeEvent extends EventObject {

    private final String collection;
    private final Object item;

    public enum Type {
        ADD,
        REMOVE,
        REORDER
    }
    private final Type type;

    public CollectionChangeEvent(Object source, String collectionName, Object item, Type type) {
        super(source);
        this.type = type;
        this.collection = collectionName;
        this.item = item;
    }

    public Type getType() {
        return type;
    }

    public String getCollectionName() {
        return collection;
    }

    public <T> Optional<T> getItemAs(Class<T> clz) {
        try {
            return Optional.ofNullable(item != null ? clz.cast(item) : null);
        } catch (ClassCastException e) {
            return Optional.empty();
        }
    }
}
