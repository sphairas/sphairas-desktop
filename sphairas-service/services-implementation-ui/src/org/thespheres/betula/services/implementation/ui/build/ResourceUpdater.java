/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.build;

import java.io.IOException;
import java.util.function.Consumer;
import org.thespheres.betula.adminconfig.ConfigurationBuildTask;
import org.thespheres.betula.services.implementation.ui.impl.SyncedProviderInstance;

/**
 *
 * @author boris.heithecker
 */
class ResourceUpdater extends AbstractResourceUpdater {

    private final Consumer<ConfigurationBuildTask> agent;
    private final boolean lockProvided;

    ResourceUpdater(SyncedProviderInstance instance, String resource, Consumer<ConfigurationBuildTask> agent, String providedLock) {
        super(instance, resource, providedLock);
        this.agent = agent;
        this.lockProvided = providedLock != null;
    }

    @Override
    protected void runUpdates() throws IOException {
        if (!lockProvided) {
            lock(60);
        }
        boolean updated = instance.enqueue(-1);
        if (!updated) {
            throw new IOException("Cannot update resource because provider synchronization is disabled.");
        }
        try {
            agent.accept(this);
        } catch (ExceptionHolder eh) {
            throw new IOException(eh.original);
        } catch (Exception e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (IOException) e.getCause();
            } else {
                throw new IOException(e);
            }
        }
        if (!lockProvided) {
            unlock();
        }
    }

}
