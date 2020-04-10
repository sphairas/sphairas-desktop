/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.impl;

import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.adminconfig.Configurations;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = Configurations.Factory.class)
public class ConfigurationsFactoryImpl extends Configurations.Factory {

    @Override
    public Configurations find(final String provider) {
        final SyncedProviderInstance spi = SyncedProviderInstance.getInstances().get(provider);
        if (spi != null) {
            return spi.config;
        }
        return null;
    }

}
