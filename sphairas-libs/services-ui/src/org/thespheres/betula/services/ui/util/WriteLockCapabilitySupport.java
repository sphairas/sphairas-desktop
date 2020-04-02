/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.util;

import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author boris.heithecker
 */
public class WriteLockCapabilitySupport implements WriteLockCapability {

    private final VetoableChangeSupport cSupport;
    private final AtomicReference<WriteLock> lock = new AtomicReference<>(null);

    public WriteLockCapabilitySupport() {
        this(null);
    }

    public WriteLockCapabilitySupport(Object source) {
        this.cSupport = source == null ? new VetoableChangeSupport(this) : new VetoableChangeSupport(source);
    }

    @Override
    public boolean isWriteLocked() {
        synchronized (lock) {
            return lock.get() != null;
        }
    }

    @Override
    public WriteLock writeLock() {
        if (isWriteLocked()) {
            return null;
        }
        synchronized (lock) {
            try {
                cSupport.fireVetoableChange(WriteLockCapability.PROP_LOCK_REQUEST, false, true);
            } catch (PropertyVetoException ex) {
                return null;
            }
            lock.set(createLock());
        }
        try {
            cSupport.fireVetoableChange(WriteLockCapability.PROP_LOCK_STATE, false, true);
        } catch (PropertyVetoException ex) {
            return null;
        }
        return lock.get();
    }

    protected WriteLock createLock() {
        return new WriteLockImpl(this);
    }

    protected boolean unlock(final WriteLock current) {
        final boolean ret;
        synchronized (lock) {
            ret = lock.compareAndSet(current, null);
        }
        if (ret) {
            try {
                cSupport.fireVetoableChange(WriteLockCapability.PROP_LOCK_STATE, true, false);
            } catch (PropertyVetoException ex) {
            }
        }
        return ret;
    }

    @Override
    public void addVetoableChangeListener(VetoableChangeListener listener) {
        cSupport.addVetoableChangeListener(listener);
    }

    @Override
    public void removeVetoableChangeListener(VetoableChangeListener listener) {
        cSupport.removeVetoableChangeListener(listener);
    }

}
