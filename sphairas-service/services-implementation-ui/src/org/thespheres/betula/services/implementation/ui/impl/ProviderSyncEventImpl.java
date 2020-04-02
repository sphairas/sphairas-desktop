/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.impl;

import org.thespheres.betula.adminconfig.ProviderSyncEvent;

/**
 *
 * @author boris.heithecker
 */
class ProviderSyncEventImpl implements ProviderSyncEvent {

    private final String resource;
    private final SyncedProviderInstance instance;

    ProviderSyncEventImpl(final String resource, final SyncedProviderInstance spi) {
        this.resource = resource;
        this.instance = spi;
    }

    @Override
    public void runLater(final Runnable run) {
        instance.eventsrp.post(run);
    }

    @Override
    public String getResource() {
        return resource;
    }

}
