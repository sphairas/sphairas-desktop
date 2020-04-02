/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;

/**
 *
 * @author boris.heithecker
 * @param <C>
 */
public final class SurvivingResult<C> extends Lookup.Result<C> implements LookupListener {
    
    private final Lookup.Result<C> delegate;
    //        private final Lookup.Result<Provider> nodes;
    private final Collection<LookupListener> listeners;
    private Collection<? extends Lookup.Item<C>> allItems;
    private Collection<? extends C> allInstances;
    private Set<Class<? extends C>> allClasses;
    private final boolean survive;

    //        public NeverEmptyResult(Result<T> delegate, Result<Provider> nodes) {
    public SurvivingResult(Lookup.Result<C> delegate, boolean survive) {
        this.delegate = delegate;
        this.survive = survive;
        //            this.nodes = nodes;
        this.listeners = new CopyOnWriteArrayList<>();
        // add weak listeners so this can be GCed when listeners are empty
        this.delegate.addLookupListener(WeakListeners.create(LookupListener.class, this, this.delegate));
        //            this.nodes.addLookupListener(WeakListeners.create(LookupListener.class, this, this.nodes));
        initValues();
    }

    @Override
    public void addLookupListener(LookupListener l) {
        listeners.add(l);
    }

    @Override
    public void removeLookupListener(LookupListener l) {
        listeners.remove(l);
    }

    private boolean acceptResult(Collection res) {
        return !survive || !res.isEmpty();
    }

    @Override
    public Collection<? extends Lookup.Item<C>> allItems() {
        Collection<? extends Lookup.Item<C>> res = delegate.allItems();
        synchronized (this) {
            if (acceptResult(res)) {
                allItems = res;
            }
            return allItems;
        }
    }

    @Override
    public Collection<? extends C> allInstances() {
        Collection<? extends C> res = delegate.allInstances();
        synchronized (this) {
            if (acceptResult(res)) {
                allInstances = res;
            }
            return allInstances;
        }
    }

    @Override
    public Set<Class<? extends C>> allClasses() {
        Set<Class<? extends C>> res = delegate.allClasses();
        synchronized (this) {
            if (acceptResult(res)) {
                allClasses = res;
            }
            return allClasses;
        }
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        //            if (ev.getSource() == nodes) {
        //                Collection<? extends Item<Provider>> arr = nodes.allItems();
        //                if (arr.size() == 1 && arr.iterator().next().getInstance() == null) {
        //                    return;
        //                }
        //                initValues();
        //                return;
        //            }
        final LookupEvent mev = new LookupEvent(this);
        for (LookupListener ll : listeners) {
            ll.resultChanged(mev);
        }
    }

    private synchronized void initValues() {
        allItems = Collections.emptyList();
        allInstances = Collections.emptyList();
        allClasses = Collections.emptySet();
    }
    
}
