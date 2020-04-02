/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.util;

import java.beans.VetoableChangeListener;
import java.util.Random;

/**
 *
 * @author boris.heithecker
 */
public interface WriteLockCapability {

    public static final String PROP_LOCK_REQUEST = "lock-request";
    public static final String PROP_LOCK_STATE = "lock-state";

    public boolean isWriteLocked();

    public WriteLock writeLock();

    public void addVetoableChangeListener(VetoableChangeListener l);

    public void removeVetoableChangeListener(VetoableChangeListener l);

    public static abstract class WriteLock {

        private final static Random GEN = new Random();
        private final long id;

        protected WriteLock() {
            id = GEN.nextLong();
        }

        public long getId() {
            return id;
        }

        public abstract boolean isLockValid();

        public abstract void releaseLock();

        @Override
        public int hashCode() {
            int hash = 7;
            return 89 * hash + (int) (this.id ^ (this.id >>> 32));
        }

        @Override
        public final boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final WriteLock other = (WriteLock) obj;
            return this.id == other.id;
        }

    }

}
