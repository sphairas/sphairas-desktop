/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.util;

import org.thespheres.betula.services.ui.util.WriteLockCapability.WriteLock;

/**
 *
 * @author boris.heithecker
 */
class WriteLockImpl extends WriteLock {
    
    private final WriteLockCapabilitySupport parent;
    
    WriteLockImpl(WriteLockCapabilitySupport parent) {
        this.parent = parent;
    }
    
    @Override
    public boolean isLockValid() {
        return true;
    }
    
    @Override
    public void releaseLock() {
        parent.unlock(this);
    }
    
}
