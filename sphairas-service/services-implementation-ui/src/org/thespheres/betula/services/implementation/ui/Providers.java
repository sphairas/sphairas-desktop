/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui;

import java.util.Set;
import org.thespheres.betula.services.implementation.ui.impl.SyncedProviderInstance;

/**
 *
 * @author boris.heithecker
 */
public class Providers {

    private Providers() {
    }
    
    public static Set<String> getRegistered() {
        return SyncedProviderInstance.getInstances().keySet();
    }
}
