/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.util;

import java.beans.PropertyChangeEvent;

/**
 *
 * @author boris.heithecker
 */
public final class CollectionElementPropertyChangeEvent extends PropertyChangeEvent {

    private final String key;

    public CollectionElementPropertyChangeEvent(Object source, String collection, String elementKey, Object oldValue, Object newValue) {
        super(source, collection, oldValue, newValue);
        this.key = elementKey;
    }

    public String getElementKey() {
        return key;
    }
}
