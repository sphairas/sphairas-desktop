/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminconfig;

import java.io.IOException;
import org.openide.util.Lookup;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris
 */
public interface Configurations {

    public <T> Configuration<T> readConfiguration(final String name, final Class<T> type) throws IOException;
    
    public String getLastModified(final String resource);

    public static Configurations find(final String provider) {
        return Lookup.getDefault().lookupAll(Configurations.Factory.class).stream()
                .map(f -> f.find(provider))
                .collect(CollectionUtil.requireSingleOrNull());
    }

    public static abstract class Factory {

        public abstract Configurations find(final String provider);
    }
}
