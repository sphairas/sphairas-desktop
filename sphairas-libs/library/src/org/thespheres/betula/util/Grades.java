/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.thespheres.betula.assess.Grade;

/**
 *
 * @author boris.heithecker
 * @param <T>
 */
public class Grades<T extends Grade> {

    private final Map<T, Integer> map = new HashMap<>();
    private final PropertyChangeSupport pSupport = new PropertyChangeSupport(this);

    private void putImpl(T grade, int num) {
        if (grade != null) {
            Integer ov = map.put(grade, num);
            int old = ov != null ? ov : 0;
            pSupport.firePropertyChange(grade.getId(), old, num);
        }
    }

    public final void set(T grade, int num) {
        synchronized (map) {
            putImpl(grade, num);
        }
    }

    public final void inc(T grade) {
        synchronized (map) {
            int num = map.getOrDefault(grade, 0);
            putImpl(grade, ++num);//Nullpointer grade??
        }
    }

    public final void dec(T grade) {
        synchronized (map) {
            int num = get(grade);
            putImpl(grade, --num);
        }
    }

    public int get(T grade) {
        synchronized (map) {
            return map.getOrDefault(grade, 0);
        }
    }

    public Set<Grade> keys() {
        synchronized (map) {
            return new HashSet<>(map.keySet());
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        pSupport.removePropertyChangeListener(l);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        map.keySet().stream().forEach(g -> {
            sb.append("[")
                    .append(g.getId())
                    .append(":")
                    .append(map.get(g))
                    .append("] ");
        });
        if (sb.length() != 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }
}
