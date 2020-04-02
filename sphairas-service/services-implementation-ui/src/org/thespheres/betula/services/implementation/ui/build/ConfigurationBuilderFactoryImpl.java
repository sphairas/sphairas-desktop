/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.build;

import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.adminconfig.ConfigurationBuilder;
import org.thespheres.betula.services.implementation.ui.impl.SyncedProviderInstance;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = ConfigurationBuilder.Factory.class)
public class ConfigurationBuilderFactoryImpl extends ConfigurationBuilder.Factory {

    @Override
    public ConfigurationBuilder create(String provider) {
        final SyncedProviderInstance spi = SyncedProviderInstance.getInstances().get(provider);
        if (spi != null) {
            return new ConfigurationBuilderImpl(spi);
        }
        return null;
    }

}
