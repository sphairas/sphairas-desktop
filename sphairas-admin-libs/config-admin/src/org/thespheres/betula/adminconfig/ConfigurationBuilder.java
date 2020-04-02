/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminconfig;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.openide.util.EditableProperties;
import org.openide.util.Lookup;
import org.thespheres.betula.adminconfig.layerxml.LayerFileSystem;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
public interface ConfigurationBuilder {

    public void buildLayer(BiConsumer<ConfigurationBuildTask, LayerFileSystem> agent);

    public void buildLocalProperties(BiConsumer<ConfigurationBuildTask, EditableProperties> agent);

    public void buildResources(String resources, Consumer<ConfigurationBuildTask> agent);

    public void buildResources(String resources, Consumer<ConfigurationBuildTask> agent, String providedLock);

    public static ConfigurationBuilder find(final String provider) {
        return Lookup.getDefault().lookupAll(ConfigurationBuilder.Factory.class).stream()
                .map(f -> f.create(provider))
                .collect(CollectionUtil.requireSingleOrNull());
    }

    public static abstract class Factory {

        public abstract ConfigurationBuilder create(final String provider);
    }
}
