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
import org.thespheres.betula.admin.units.SigneesTopComponentModel;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = SigneesTopComponentModel.Provider.class)
public class SigneesTopComponentProviderImpl implements SigneesTopComponentModel.Provider {

    private final static Map<String, SigneesTopComponentModel> INSTANCE = new HashMap<>();

    public SigneesTopComponentProviderImpl() {
    }

    @Override
    public List<SigneesTopComponentModel> findAll() {
        synchronized (INSTANCE) {
            return SyncedProviderInstance.getInstances().keySet().stream()
                    .map(p -> INSTANCE.computeIfAbsent(p, SigneesTopComponentModel::new))
                    .collect(Collectors.toList());
        }
    }

}
