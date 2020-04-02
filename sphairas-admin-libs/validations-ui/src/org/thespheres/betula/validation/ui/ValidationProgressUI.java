/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.validation.ui;

import java.awt.Color;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.swing.Action;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.awt.Actions;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.windows.IOColorLines;
import org.openide.windows.InputOutput;
import org.thespheres.betula.validation.ValidationResult;
import org.thespheres.betula.validation.ValidationResultSet;
import org.thespheres.betula.validation.ui.impl.ShowValidationsAction;

/**
 *
 * @author boris.heithecker
 */
@Messages({"ValidationProgressUI.displayName={0,choice,1#{1}|1<{0} Validierungen}"})
public class ValidationProgressUI {

    public static final String PROP_LOG_VALIDATIONS = "ValidationProgressUI.log.in.output";
    private static ValidationProgressUI INSTANCE;
    private ProgressHandle progress;
    private final Set<Listener<?>> listeners = new HashSet<>();
    private final long[] count = new long[]{1};

    private ValidationProgressUI() {
    }

    public static ValidationProgressUI getDefault() {
        if (INSTANCE == null) {
            INSTANCE = new ValidationProgressUI();
        }
        return INSTANCE;
    }

    public <R extends ValidationResult> ValidationResultSet.ValidationListener<?> createListener(final ValidationResultSet<?, R> impl, final Supplier<String> displayName) {
        final Listener l;
        synchronized (listeners) {
            l = new Listener(count[0]++, impl, displayName);
            listeners.add(l);
        }
        return l;
    }

    public List<ValidationItem> getItemsSnapshot() {
        final List<ValidationItem> ret;
        synchronized (listeners) {
            ret = listeners.stream()
                    .filter(Listener::isValidating)
                    .map(ValidationItem::new)
                    .collect(Collectors.toList());
        }
        return ret;
    }

    synchronized void updateProgressHandle() {
        final List<String> l = active();
        if (!l.isEmpty() && progress == null) {
            final String name = NbBundle.getMessage(ValidationProgressUI.class, "ValidationProgressUI.displayName", l.size(), l.get(0));
            final Action ac = Actions.forID("Tools", "org.thespheres.betula.validation.ui.impl.ShowValidationsAction");
            progress = ProgressHandleFactory.createHandle(name, () -> cancelAll(), ac);
            progress.start();
        } else if (progress != null && l.isEmpty()) {
            progress.finish();
            progress = null;
        } else if (progress != null) {
            final String name = NbBundle.getMessage(ValidationProgressUI.class, "ValidationProgressUI.displayName", l.size(), l.get(0));
            progress.setDisplayName(name);
        }

    }

    private boolean cancelAll() {
        final List<Listener<?>> snapShot;
        synchronized (listeners) {
            snapShot = listeners.stream()
                    .filter(Listener::isValidating)
                    .collect(Collectors.toList());
        }
        return snapShot.stream()
                .map(l -> l.cancel())
                .reduce(Boolean.TRUE, (b, n) -> b && n);
    }

    private List<String> active() {
        final List<String> ret;
        synchronized (listeners) {
            ret = listeners.stream()
                    .filter(Listener::isValidating)
                    .map(l -> l.displayName.get())
                    .collect(Collectors.toList());
        }
        return ret;
    }

    static boolean logValidations() {
        return NbPreferences.forModule(ValidationProgressUI.class).getBoolean(PROP_LOG_VALIDATIONS, false);
    }

    public static class ValidationItem {

        private final WeakReference<Listener<?>> instance;

        ValidationItem(Listener l) {
            this.instance = new WeakReference(l);
        }

        public String getDisplayName() {
            return Optional.ofNullable(instance.get())
                    .map(l -> l.displayName.get())
                    .orElse("null");
        }

    }

    class Listener<R extends ValidationResult> implements ValidationResultSet.ValidationListener<R> {

        private final long id;
        private boolean validating;
        final Supplier<String> displayName;
        private final WeakReference<ValidationResultSet<?, R>> set;
        private final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
        private Cancellable cancellable;

        private Listener(long id, final ValidationResultSet<?, R> impl, final Supplier<String> displayName) {
            this.id = id;
            this.set = new WeakReference<>(impl);
            this.displayName = displayName;
        }

        private boolean isValidating() {
            synchronized (this) {
                return validating;
            }
        }

        boolean cancel() {
            final Cancellable cancel;
            synchronized (this) {
                cancel = cancellable;
            }
            return cancel.cancel();
        }

        @Override
        public void onStart(int size, Cancellable cancel) {
            final LocalTime time = LocalTime.now();
            synchronized (this) {
                validating = true;
                cancellable = cancel;
            }
            updateProgressHandle();
            if (logValidations()) {
                final String message = "[" + time.format(TIME_FORMAT) + "] Started: " + displayName.get() + " (" + size + " items)";
                InputOutput io = ShowValidationsAction.getIO();
                try {
                    IOColorLines.println(io, message, Color.BLUE);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        @Override
        public void onStop() {
            synchronized (this) {
                validating = false;
            }
            updateProgressHandle();
            if (logValidations()) {
                final String message = "Stopped: " + displayName.get();
                InputOutput io = ShowValidationsAction.getIO();
                try {
                    IOColorLines.println(io, message, Color.LIGHT_GRAY);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        @Override
        public void resultAdded(R result) {
            final String msg = result.getMessage();
            if (msg != null && logValidations()) {
                ShowValidationsAction.getIO().getOut().println(msg);
            }
        }

        @Override
        public void resultRemoved(R result) {
        }

    }
}
