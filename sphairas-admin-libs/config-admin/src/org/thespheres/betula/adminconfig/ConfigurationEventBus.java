/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminconfig;

import com.google.common.eventbus.EventBus;
import java.util.Objects;
import org.openide.util.Lookup;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
public abstract class ConfigurationEventBus {

    private final String provider;

    protected ConfigurationEventBus(final String provider) {
        this.provider = provider;
    }

    public String getProvider() {
        return provider;
    }

    public void register(Object object) {
        getEvents().register(object);
    }

    public void unregister(Object object) {
        getEvents().unregister(object);
    }

    protected abstract EventBus getEvents();

    public static ConfigurationEventBus find(final String provider) {
        return Lookup.getDefault().lookupAll(ConfigurationEventBus.Registry.class).stream()
                .map(f -> f.find(provider))
                .filter(Objects::nonNull)
                .collect(CollectionUtil.requireSingleOrNull());
    }

    public static abstract class Registry {

        public abstract ConfigurationEventBus find(final String provider);
    }
}
