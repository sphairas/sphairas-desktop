/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.impl;

import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.adminconfig.ConfigurationEventBus;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = ConfigurationEventBus.Registry.class)
public class ConfigurationEventBusRegistryImpl extends ConfigurationEventBus.Registry {

    @Override
    public ConfigurationEventBus find(final String provider) {
        final SyncedProviderInstance spi = SyncedProviderInstance.getInstances().get(provider);
        if (spi != null) {
            return spi.cfgbus;
        }
        return null;
    }

}
