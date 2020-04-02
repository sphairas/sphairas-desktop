/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.validation.impl;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.tag.State;
import org.thespheres.betula.validation.ValidationResult;
import org.thespheres.betula.validation.ValidationResultSet;
import org.thespheres.betula.validation.ValidationNodeSet;

/**
 *
 * @author boris.heithecker
 * @param <M>
 * @param <R>
 * @param <K>
 * @param <Config>
 */
public abstract class AbstractValidationSet<M, R extends ValidationResult, K, Config> extends AbstractSet<R> implements ValidationResultSet<M, R> {

    protected final M model;
    protected final Config config;
    private final HashMap<K, List<R>> result = new HashMap<>();
    private final List<ValidationResultSet.ValidationListener<R>> listenerList = new ArrayList<>(10);
    private final RequestProcessor dispatcher = new RequestProcessor(ZensurensprungValidation.class.getName(), 1);
    protected final State[] initialization;

    protected AbstractValidationSet(M model, Config config) {
        this.model = model;
        this.config = config;
        initialization = new State[]{ValidationResultSet.NONE};
    }

    public abstract String getDisplayName(String modelDisplayName);

    public String getDescription() {
        return null;
    }

    public M getModel() {
        return model;
    }

    @Override
    public State getState() {
        State ret;
        synchronized (initialization) {
            ret = initialization[0];
        }
        return ret;
    }

    protected void updateState(final State state) {
        synchronized (initialization) {
//            State before = getState();
            initialization[0] = state;
//            pSupport.firePropertyChange(PROP_STATUS, before, getInitialization());
        }
    }

    @Override
    public ValidationResultSet<?, ?> getParentValidation() {
        return null;
    }

    @Override
    public ValidationNodeSet getNodesSet() {
        return null;
    }

    @Override
    public Iterator<R> iterator() {
        final Set<R> view;
        synchronized (result) {
            view = result.values().stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toSet());
        }
        final Iterator<R> it = view.iterator();
        class IteratorImpl implements Iterator<R> {

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public R next() {
                return it.next();
            }
        }
        return new IteratorImpl();
    }

    @Override
    public int size() {
        synchronized (result) {
            return result.size();
        }
    }

    protected final void addResult(final K k, final R r) {
        final boolean added;
        synchronized (result) {
            added = result.computeIfAbsent(k, key -> new ArrayList<>(1)).add(r);
        }
        if (added) {
            fireResultAdded(r);
        }
    }

    protected final List<R> getResults(final K k) {
        synchronized (result) {
            return result.getOrDefault(k, (List<R>) Collections.EMPTY_LIST).stream()
                    .collect(Collectors.toList());
        }
    }

    protected final void setResult(final K k, final R r) {
        final boolean added;
        synchronized (result) {
            added = result.compute(k, (key, l) -> {
                List<R> ret;
                if (l == null) {
                    ret = new ArrayList<>(1);
                } else {
                    l.clear();
                    ret = l;
                }
                return ret;
            }).add(r);
        }
        if (added) {
            fireResultAdded(r);
        }
    }

    protected final void removeResult(final K k, final long id) {
        R removed = null;
        synchronized (result) {
            final List<R> l = result.get(k);
            if (l != null) {
                for (Iterator<R> it = l.iterator(); it.hasNext();) {
                    final R r = it.next();
                    if (r.id() == id) {
                        removed = r;
                        it.remove();
                        break;
                    }
                }
            }
        }
        if (removed != null) {
            fireResultRemoved(removed);
        }
    }

    protected final void removeResults(final K k) {
        final List<R> previous;
        synchronized (result) {
            previous = result.remove(k);
        }
        if (previous != null) {
            previous.forEach(this::fireResultRemoved);
        }
    }

    private ValidationListener<R>[] createListenerSnapshot() {
        final ValidationResultSet.ValidationListener<R>[] view;
        synchronized (listenerList) {
            view = listenerList.stream()
                    .toArray(ValidationResultSet.ValidationListener[]::new);
        }
        return view;
    }

    protected boolean cancel(final ValidationListener<R> cancelledBy) {
        return false;
    }

    protected void fireStart(final int size) {
        final ValidationListener<R>[] view = createListenerSnapshot();
        dispatcher.post(() -> Arrays.stream(view)
                .forEach(l -> {
                    try {
                        l.onStart(size, () -> cancel(l));
                    } catch (Exception e) {
                        final String msg = "An exception has occurred during event dipatch of validation set";
                        Logger.getLogger(getClass().getCanonicalName()).log(Level.WARNING, msg, e);
                    }
                }));
    }

    protected void fireStop() {
        ValidationListener<R>[] view = createListenerSnapshot();
        dispatcher.post(() -> Arrays.stream(view)
                .forEach(l -> {
                    try {
                        l.onStop();
                    } catch (Exception e) {
                        final String msg = "An exception has occurred during event dipatch of validation set";
                        Logger.getLogger(getClass().getCanonicalName()).log(Level.WARNING, msg, e);
                    }
                }));
    }

    protected void fireResultAdded(final R r) {
        ValidationListener<R>[] view = createListenerSnapshot();
        dispatcher.post(() -> {
            Arrays.stream(view)
                    .forEach(l -> {
                        try {
                            l.resultAdded(r);
                        } catch (Exception e) {
                            final String msg = "An exception has occurred during event dipatch of validation set";
                            Logger.getLogger(getClass().getCanonicalName()).log(Level.WARNING, msg, e);
                        }
                    });
        });
    }

    protected void fireResultRemoved(final R r) {
        ValidationListener<R>[] view = createListenerSnapshot();
        dispatcher.post(() -> Arrays.stream(view).forEach(l -> {
            try {
                l.resultRemoved(r);
            } catch (Exception e) {
                final String msg = "An exception has occurred during event dipatch of validation set";
                Logger.getLogger(getClass().getCanonicalName()).log(Level.WARNING, msg, e);
            }
        }));
    }

    @Override
    public void addValidationListener(ValidationResultSet.ValidationListener<R> l) {
        synchronized (listenerList) {
            listenerList.add(l);
        }
    }

    @Override
    public void removeValidationListener(ValidationResultSet.ValidationListener<R> l) {
        synchronized (listenerList) {
            listenerList.remove(l);
        }
    }
}
