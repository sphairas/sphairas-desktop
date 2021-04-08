/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.impl;

import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.services.NoProviderException;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.ProviderRegistry;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = ProviderRegistry.class)
public class ProviderRegistryImpl implements ProviderRegistry {

    @Override
    public ProviderInfo get(String providerUrl) throws NoProviderException {
        return findEntry(providerUrl);
    }

    protected XmlProviderInfoEntry findEntry(String providerUrl) throws NoProviderException {
        return Lookups.forPath("Provider").lookupAll(XmlProviderInfoEntry.class).stream()
                .filter(f -> f.getURL().equals(providerUrl))
                .map(XmlProviderInfoEntry.class::cast)
                .collect(CollectionUtil.requireSingleton())
                .orElseThrow(() -> new NoProviderException(ProviderInfo.class, providerUrl));
    }

}
