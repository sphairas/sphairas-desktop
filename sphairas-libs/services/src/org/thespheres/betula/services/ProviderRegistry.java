/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services;

import java.util.prefs.Preferences;
import org.openide.util.Lookup;

/**
 *
 * @author boris.heithecker
 */
public interface ProviderRegistry {

    public static ProviderRegistry getDefault() {
        return Lookup.getDefault().lookup(ProviderRegistry.class);
    }

//    public void register(final ProviderInfo info, final String codeNameBase) throws ProviderAlreadyRegistredException;
    public ProviderInfo get(String providerUrl) throws NoProviderException;

    public Preferences findPreferences(String provider) throws NoProviderException;

    public String getCodeNameBase(String provider) throws NoProviderException;

}
