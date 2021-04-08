/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.impl;

import java.util.Objects;
import java.util.logging.Level;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.NoProviderException;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.ProviderRegistry;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = ProviderRegistry.class, supersedes = {"org.thespheres.betula.services.impl.ProviderRegistryImpl"})
public class SyncedProviderRegistryImpl implements ProviderRegistry {

    @Override
    public ProviderInfo get(final String provider) throws NoProviderException {
        return findEntry(provider);
    }

    protected ProviderInfo findEntry(final String provider) throws NoProviderException {
        if (provider == null) {
            throw new NoProviderException(ProviderInfo.class, "null");
        }
        final SyncedProviderInstance spi = SyncedProviderInstance.getInstances().get(provider);
        if (spi != null) {
            class ProviderImpl implements ProviderInfo {

                @Override
                public String getURL() {
                    return provider;
                }

                @Override
                public String getDisplayName() {
                    final LocalProperties prop = spi.findLocalFileProperties();
                    return prop.getProperty("provider.displayName", provider);
                }

                @Override
                public int hashCode() {
                    int hash = 3;
                    return 89 * hash + Objects.hashCode(provider);
                }

                @Override
                public boolean equals(Object obj) {
                    if (this == obj) {
                        return true;
                    }
                    if (obj == null) {
                        return false;
                    }
                    if (!(obj instanceof ProviderInfo)) {
                        return false;
                    }
                    final ProviderInfo other = (ProviderInfo) obj;
                    return Objects.equals(provider, other.getURL());
                }
            }
            return new ProviderImpl();
        }
        PlatformUtil.getCodeNameBaseLogger(SyncedProviderRegistryImpl.class).log(Level.INFO, "No SyncedProviderInstance {0}", provider);
        return Lookups.forPath("Provider").lookupAll(ProviderInfo.class).stream()
                .filter(f -> f.getURL().equals(provider))
                .collect(CollectionUtil.requireSingleton())
                .orElseThrow(() -> new NoProviderException(ProviderInfo.class, provider));
    }

}
