/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
public interface LocalProperties {

    public String getName();

    public Map<String, String> getProperties();

    public String getProperty(String name);

    default public String getProperty(String key, String defaultValue) {
        final String val = getProperty(key);
        return (val == null) ? defaultValue : val;
    }

    public static LocalProperties find(String name) {
        return LocalProperties.find(Lookup.getDefault(), name);
    }

    public static LocalProperties find(Lookup from, final String name) {
        return LocalProperties.find(from, name, LocalProperties.class);
    }

    public static <C extends LocalProperties> C find(Lookup from, String name, Class<C> subType) {
        return find(from, name, subType, true);
    }

    static <C extends LocalProperties> C find(Lookup from, String name, Class<C> subType, boolean throwNoProviderException) {
        final C found = from.lookupAll(subType).stream()
                .map(subType::cast)
                .filter(lfp -> lfp.getName().equals(name))
                .collect(CollectionUtil.requireSingleOrNull());
        if (found != null) {
            return found;
        }
        final C foundProvider = from.lookupAll(LocalProperties.Provider.class).stream()
                .map(p -> p.find(name))
                .filter(Objects::nonNull)
                .filter(subType::isInstance)
                .map(subType::cast)
                .collect(CollectionUtil.requireSingleOrNull());
        if (foundProvider != null) {
            return foundProvider;
        }
        final Optional<C> res = Lookups.forPath("LocalProperties").lookupAll(subType).stream()
                .map(subType::cast)
                .filter(lp -> lp.getName().equals(name))
                .collect(CollectionUtil.requireSingleton());
        return throwNoProviderException ? res.orElseThrow(() -> new NoProviderException(subType, name)) : res.orElse(null);
    }

    public static interface Provider {

        public LocalFileProperties find(String name);

    }
}
