/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.util;

import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.thespheres.betula.services.WorkingDate;

/**
 *
 * @author boris.heithecker
 * @param <C>
 */
public abstract class WorkingDateSensitiveAction<C> extends AbstractAction implements ContextAwareAction {

    protected Class<C> cookieClass;
    private Listener<C> listener;
    private boolean multiple;
    protected Lookup context;

    protected WorkingDateSensitiveAction() {
    }

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    protected WorkingDateSensitiveAction(Lookup context, Class<C> clz, boolean multiple, boolean surviveFocusChange) {
        this.cookieClass = clz;
        this.multiple = multiple;
        this.context = context;
        this.listener = new Listener(this, new SurvivingResult(context.lookupResult(clz), surviveFocusChange));
        Lookup.getDefault().lookup(WorkingDate.class).addChangeListener(listener);
        updateName();
    }

    protected final void updateName() {
            putValue(Action.NAME, getName());
    }

    protected abstract String getName();

// Do not call this from constructor, fields may not be initialized
    protected final void updateEnabled() {
        Collection<? extends C> all = listener.result.allInstances();
        boolean ena = !all.isEmpty();
        ena = ena && !multiple ? all.size() == 1 : true;
        setEnabled(ena);
        onContextChange(instances());
    }

    protected void onContextChange(List<C> all) {
    }

    @Override
    public final void actionPerformed(ActionEvent e) {
        actionPerformed(instances());
    }

    private synchronized List<C> instances() {
        return listener.result.allInstances().stream()
                .map(cookieClass::cast)
                .collect(Collectors.toList());
    }

    protected abstract void actionPerformed(List<C> context);

    private final static class Listener<C> implements ChangeListener, LookupListener {

        private final WeakReference<WorkingDateSensitiveAction> reference;
        private final Lookup.Result<C> result;

        @SuppressWarnings("LeakingThisInConstructor")
        private Listener(WorkingDateSensitiveAction ref, Lookup.Result<C> result) {
            this.reference = new WeakReference<>(ref);
            this.result = result;
            this.result.addLookupListener(this);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            final WorkingDateSensitiveAction action = reference.get();
            if (action != null) {
                action.updateName();
            } else {
                Lookup.getDefault().lookup(WorkingDate.class).removeChangeListener(this);
            }
        }

        @Override
        public void resultChanged(LookupEvent ev) {
            final WorkingDateSensitiveAction action = reference.get();
            if (action != null) {
                action.updateEnabled();
                action.updateName();
            } else {
                result.removeLookupListener(this);
            }
        }

    }

}
