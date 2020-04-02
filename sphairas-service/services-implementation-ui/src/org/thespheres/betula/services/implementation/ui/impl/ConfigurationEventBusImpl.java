/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.impl;

import com.google.common.eventbus.EventBus;
import org.thespheres.betula.adminconfig.ConfigurationEventBus;

/**
 *
 * @author boris.heithecker
 */
class ConfigurationEventBusImpl extends ConfigurationEventBus {

    ConfigurationEventBusImpl(String provider) {
        super(provider);
    }

    @Override
    protected EventBus getEvents() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
