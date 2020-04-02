/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.util;

import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author boris.heithecker
 */
public abstract class MultiContextAction extends AbstractAction implements ContextAwareAction {

    private final Class<?>[] types;
    protected final List<Class<?>> multiTypes = new ArrayList<>();

    protected MultiContextAction(Class... types) {
        this.types = types;
    }

    @Override
    public final Action createContextAwareInstance(final Lookup actionContext) {
        final MultiContextSensitiveAction csa = createMultiContextSensitiveAction();
        sync(csa, actionContext);
        return csa;
    }

    private void sync(final MultiContextSensitiveAction mcsa, final Lookup context) {
        final SyncListener listener = new SyncListener(mcsa, context);
        Arrays.stream(types)
                .map(context::lookupResult)
                .map(ResultHolder::new)
                .forEach(listener::add);
        multiTypes.stream()
                .map(context::lookupResult)
                .map(r -> new ResultHolder(r, false))
                .forEach(listener::add);
        listener.resultChanged(null);
    }

    @Override
    public final void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Never to be called.");
    }

    protected abstract MultiContextSensitiveAction createMultiContextSensitiveAction();

    private final class ResultHolder<T> {

        final Lookup.Result<T> result;
        final boolean singleInstance;

        ResultHolder(Lookup.Result<T> result, boolean singleInstance) {
            this.result = result;
            this.singleInstance = singleInstance;
        }

        ResultHolder(final Lookup.Result<T> result) {
            this(result, true);
        }

        Collection<? extends T> allInstances() {
            final Collection<? extends T> ret = result.allInstances().stream()
                    .distinct()
                    .collect(Collectors.toList());
            return singleInstance ? (ret.size() <= 1 ? ret : Collections.EMPTY_LIST) : ret;
        }
    }

    private final class SyncListener implements LookupListener {

        private final WeakReference<MultiContextSensitiveAction> reference;
        private final List<ResultHolder<?>> results = new ArrayList<>(types.length);

        SyncListener(MultiContextSensitiveAction ref, Lookup ctx) {
            this.reference = new WeakReference<>(ref);
        }

        @Override
        public void resultChanged(LookupEvent ev) {
            MultiContextSensitiveAction action = reference.get();
            if (action != null) {
                boolean ena = results.stream()
                        .map(ResultHolder::allInstances)
                        .noneMatch(Collection::isEmpty);
                final Lookup instances = ena ? instances() : Lookup.EMPTY;
                action.updateEnabled(ena, instances);
                action.updateName();
            } else {
                results.forEach(r -> r.result.removeLookupListener(this));
                results.clear();
            }
        }

        void add(final ResultHolder<?> res) {
            results.add(res);
            res.result.addLookupListener(this);
        }

        private Lookup instances() {
            final Object[] all = results.stream()
                    .map(ResultHolder::allInstances)
                    .flatMap(Collection::stream)
                    .toArray(Object[]::new);
            return Lookups.fixed(all);
        }

    }
}
