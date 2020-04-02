/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.build;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.openide.util.EditableProperties;
import org.thespheres.betula.adminconfig.ConfigurationBuilder;
import org.thespheres.betula.adminconfig.layerxml.LayerFileSystem;
import org.thespheres.betula.services.implementation.ui.impl.SyncedProviderInstance;
import org.thespheres.betula.adminconfig.ConfigurationBuildTask;

/**
 *
 * @author boris.heithecker
 */
class ConfigurationBuilderImpl implements ConfigurationBuilder {

    private final SyncedProviderInstance instance;

    ConfigurationBuilderImpl(SyncedProviderInstance instance) {
        this.instance = instance;
    }

    @Override
    public void buildLayer(BiConsumer<ConfigurationBuildTask, LayerFileSystem> agent) {
        final LayerUpdater lu = new LayerUpdater(instance, agent);
        instance.submit(lu);
    }

    @Override
    public void buildLocalProperties(BiConsumer<ConfigurationBuildTask, EditableProperties> agent) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void buildResources(String resources, Consumer<ConfigurationBuildTask> agent) {
        buildResources(resources, agent, null);
    }

    @Override
    public void buildResources(String resources, Consumer<ConfigurationBuildTask> agent, String providedLock) {
        final ResourceUpdater ru = new ResourceUpdater(instance, resources, agent, providedLock);
        instance.submit(ru);
    }

}
