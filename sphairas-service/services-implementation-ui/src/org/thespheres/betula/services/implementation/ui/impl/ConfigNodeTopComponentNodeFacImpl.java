/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.adminconfig.ConfigNodeTopComponentNodeList;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = ConfigNodeTopComponentNodeList.Provider.class)
public class ConfigNodeTopComponentNodeFacImpl extends ConfigNodeTopComponentNodeList.Provider {

    @Override
    public List<ConfigNodeTopComponentNodeList> nodeLists() {
        return SyncedProviderInstance.getInstances().values().stream()
                .flatMap(p -> p.getConfigNodeTopComponentNodes().stream())
                .collect(Collectors.toList());
    }

    static List<ConfigNodeTopComponentNodeList.Factory> factories() {
        return Lookups.forPath("ConfigNodeTopComponentNodeFactory").lookupAll(ConfigNodeTopComponentNodeList.Factory.class).stream()
                .map(ConfigNodeTopComponentNodeList.Factory.class::cast)
                .collect(Collectors.toList());
    }

}
