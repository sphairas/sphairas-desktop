/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.impl;

import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.WebProvider.WebProviders;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = WebProviders.class)
public class WebProvidersImpl implements WebProviders {

    @Override
    public <W extends WebProvider> W find(final String name, final Class<W> subType) {
        if (!SyncedProviderInstance.getInstances().containsKey(name)) {
            return null;
        }
        return SyncedProviderInstance.getInstances().get(name).findWebProvider(subType);
    }

}
