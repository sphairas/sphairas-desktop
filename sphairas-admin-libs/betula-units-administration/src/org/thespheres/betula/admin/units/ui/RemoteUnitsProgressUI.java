/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.admin.units.RemoteUnitsModel;

/**
 *
 * @author boris.heithecker
 */
@Messages({"RemoteUnitsProgressUI.displayName.multiple={0,choice,1#Ein Datensatz wird geladen|1<{0} Datensätze werden geladen}",
    "RemoteUnitsProgressUI.displayName.single={0} wird geladen."})
public class RemoteUnitsProgressUI {

    private static RemoteUnitsProgressUI INSTANCE;
    private ProgressHandle progress;
    private final Set<Listener> listeners = new HashSet<>();
    private final long[] count = new long[]{1};

    private RemoteUnitsProgressUI() {
    }

    public static RemoteUnitsProgressUI getDefault() {
        if (INSTANCE == null) {
            INSTANCE = new RemoteUnitsProgressUI();
        }
        return INSTANCE;
    }

    public PropertyChangeListener createListener(RemoteUnitsModel impl) {
        final Listener l;
        synchronized (listeners) {
            l = new Listener(count[0]++, impl);
            listeners.add(l);
        }
        return l;
    }

    synchronized void updateProgressHandle(final RemoteUnitsModel source) {
        final List<Listener> active = active();
        if (!active.isEmpty() && progress == null) {
            final String name = findName(active);
            progress = ProgressHandleFactory.createHandle(name, () -> source.cancelLoading(), null);
            progress.start();
        } else if (progress != null && active.isEmpty()) {
            progress.finish();
            progress = null;
        } else if (progress != null) {
            final String name = findName(active);
            progress.setDisplayName(name);
        }
    }

    private String findName(final List<Listener> active) {
        if (active.size() == 1) {
            final RemoteUnitsModel m = active.get(0).model.get();
            if (m != null) {
                final String node = m.getUnitOpenSupport().getNodeDelegate().getDisplayName();
                return NbBundle.getMessage(RemoteUnitsProgressUI.class, "RemoteUnitsProgressUI.displayName.single", node);
            }
        }
        return NbBundle.getMessage(RemoteUnitsProgressUI.class, "RemoteUnitsProgressUI.displayName.multiple", active.size());
    }

    private List<Listener> active() {
        final List<Listener> size;
        synchronized (listeners) {
            size = listeners.stream()
                    .filter(Listener::initializing)
                    .collect(Collectors.toList());
        }
        return size;
    }

    class Listener implements PropertyChangeListener {

        private final long id;
        private boolean init;
        private final WeakReference<RemoteUnitsModel> model;

        private Listener(long id, RemoteUnitsModel impl) {
            this.id = id;
            this.model = new WeakReference<>(impl);
        }

        private boolean initializing() {
            synchronized (this) {
                return init;
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (RemoteUnitsModel.PROP_INITIALIZING.equals(evt.getPropertyName())) {
                synchronized (this) {
                    init = (boolean) evt.getNewValue();
                }
            }
            updateProgressHandle((RemoteUnitsModel) evt.getSource());
        }

    }
}
