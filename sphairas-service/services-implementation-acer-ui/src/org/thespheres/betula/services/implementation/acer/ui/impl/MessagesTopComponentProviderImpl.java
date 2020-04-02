/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.acer.ui.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.acer.remote.ui.MessagesTopComponentModel;
import org.thespheres.betula.services.implementation.ui.Providers;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = MessagesTopComponentModel.Provider.class)
public class MessagesTopComponentProviderImpl implements MessagesTopComponentModel.Provider {

    private final static Map<String, MessagesTopComponentModel> INSTANCE = new HashMap<>();

    public MessagesTopComponentProviderImpl() {
    }

    @Override
    public List<MessagesTopComponentModel> findAll() {
        synchronized (INSTANCE) {
            return Providers.getRegistered().stream()
                    .map(p -> INSTANCE.computeIfAbsent(p, MessagesTopComponentModel::new))
                    .collect(Collectors.toList());
        }
    }

}
