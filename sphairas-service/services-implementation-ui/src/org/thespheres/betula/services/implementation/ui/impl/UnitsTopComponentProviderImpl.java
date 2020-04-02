/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.admin.units.UnitsTopComponentModel;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = UnitsTopComponentModel.Provider.class)
public class UnitsTopComponentProviderImpl implements UnitsTopComponentModel.Provider {

    private final static Map<String, UnitsTopComponentModel> INSTANCE = new HashMap<>();

    public UnitsTopComponentProviderImpl() {
    }

    @Override
    public List<UnitsTopComponentModel> findAll() {
        synchronized (INSTANCE) {
            return SyncedProviderInstance.getInstances().keySet().stream()
                    .map(p -> INSTANCE.computeIfAbsent(p, UnitsTopComponentModel::new))
                    .collect(Collectors.toList());
        }
    }

}
