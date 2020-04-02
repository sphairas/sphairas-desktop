/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.util;

import java.util.AbstractSet;
import java.util.EventObject;
import java.util.Iterator;
import java.util.Set;
import org.thespheres.betula.util.ChangeSet.SetChangeEvent.Action;

/**
 *
 * @author boris.heithecker
 * @param <T>
 */
public class ChangeSet<T> extends AbstractSet<T> {

    private final Set<T> delegate;
    private final ChangeSetSupport<T> cSupport;

    public ChangeSet(Set<T> delegate) {
        this.delegate = delegate;
        this.cSupport = new ChangeSetSupport(delegate);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public Iterator<T> iterator() {
        final Iterator<T> original = delegate.iterator();
        class NotifyingIterator implements Iterator<T> {

            private T last;

            @Override
            public boolean hasNext() {
                return original.hasNext();
            }

            @Override
            public T next() {
                last = original.next();
                return last;
            }

            @Override
            public void remove() {
                original.remove();
                cSupport.fireChange(last, Action.REMOVE);
            }

        }
        return new NotifyingIterator();
    }

    @Override
    public boolean add(T e) {
        boolean ret = delegate.add(e);
        if (ret) {
            cSupport.fireChange(e, Action.ADD);
        }
        return ret;
    }

    public void addChangeListener(Listener listener) {
        cSupport.addChangeListener(listener);
    }

    public void removeChangeListener(Listener listener) {
        cSupport.removeChangeListener(listener);
    }

    public static class SetChangeEvent<T> extends EventObject {

        private final T element;
        private final Action action;

        public enum Action {

            ADD, REMOVE
        }

        SetChangeEvent(Set source, T element, Action action) {
            super(source);
            this.element = element;
            this.action = action;
        }

        public T getElement() {
            return element;
        }

        public Action getAction() {
            return action;
        }

    }

    public static interface Listener<T> {

        void setChanged(SetChangeEvent<T> e);
    }
}
