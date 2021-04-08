/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services;

import org.openide.util.Lookup;

/**
 *
 * @author boris.heithecker
 */
public interface ProviderRegistry {

    public static ProviderRegistry getDefault() {
        return Lookup.getDefault().lookup(ProviderRegistry.class);
    }

    public ProviderInfo get(String providerUrl) throws NoProviderException;

}
