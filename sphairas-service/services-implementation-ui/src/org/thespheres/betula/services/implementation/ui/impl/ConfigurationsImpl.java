/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.thespheres.betula.adminconfig.AbstractConfigurations;

/**
 *
 * @author boris
 */
class ConfigurationsImpl extends AbstractConfigurations {

    final SyncedProviderInstance instance;

    ConfigurationsImpl(final SyncedProviderInstance instance) {
        this.instance = instance;
    }

    @Override
    protected InputStream getResource(final String res) throws IOException {
        instance.updater.getNumRuns(1l);
        final Path p = instance.getBaseDir().resolve(res);
        if (!Files.exists(p)) {
            throw new IOException("Resource " + res + " does not exist in " + instance.getProvider());
        }
        return Files.newInputStream(p);
    }

    @Override
    public String getLastModified(final String resource) {
        return instance.updater.getLastModified(resource);
    }

}
