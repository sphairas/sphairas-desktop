/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tag;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.thespheres.betula.Convention;
import org.thespheres.betula.Tag;

public abstract class TagConvention<T extends Tag> implements Convention<T>, Iterable<T> {

    private final String name;
    private final Map<String, T> elements;

    protected TagConvention(String name, String[] tagIds) {
        this.name = name;
        this.elements = new HashMap<>(tagIds.length);
        for (String id : tagIds) {
            elements.put(id, null);
        }
    }

    @Override
    public final T find(String id) {
        if (!elements.containsKey(id)) {
            return null;
        }
        T ret = elements.get(id);
        if (ret == null) {
            ret = create(id);
            if (ret != null) {
                elements.put(id, ret);
            }
        }
        return ret;
    }

    @Override
    public final String getName() {
        return this.name;
    }

    protected abstract T create(String id);

    @Override
    public Iterator<T> iterator() {
        final Iterator<String> iterator = elements.keySet().iterator();
        return new Iterator<T>() {

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public T next() {
                String n = iterator.next();
                return find(n);
            }

            @Override
            public void remove() {
                iterator.remove();
            }

        };
    }
}
