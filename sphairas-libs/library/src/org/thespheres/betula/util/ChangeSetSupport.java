/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.util;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import org.thespheres.betula.util.ChangeSet.Listener;
import org.thespheres.betula.util.ChangeSet.SetChangeEvent;
import org.thespheres.betula.util.ChangeSet.SetChangeEvent.Action;

/**
 *
 * @author boris.heithecker
 * @param <T>
 */
public class ChangeSetSupport<T> {

    final List<Listener> listeners = new CopyOnWriteArrayList<>();
    private final Set source;

    public ChangeSetSupport(Set source) {
        this.source = source;
    }

    public void addChangeListener(Listener listener) {
        if (listener == null) {
            return;
        }
        listeners.add(listener);
    }

    public void removeChangeListener(Listener listener) {
        if (listener == null) {
            return;
        }
        listeners.remove(listener);
    }

    public void fireChange(T element, Action ac) {
        if (listeners.isEmpty()) {
            return;
        }
        fireChange(new SetChangeEvent(source, element, ac));
    }

    private void fireChange(SetChangeEvent<T> event) {
        assert event != null;
        listeners.stream().forEach(listener -> listener.setChanged(event));
    }
}
