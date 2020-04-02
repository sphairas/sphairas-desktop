/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.project.ServiceProjectTemplate;
import org.thespheres.betula.services.implementation.ui.project.PatternProvider;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = PatternProvider.Provider.class)
public class PatternProviderImpl implements PatternProvider.Provider {
    
    @Override
    public <S extends ServiceProjectTemplate> S findTemplate(String provider, Class<S> type) {
        if (PatternProvider.class.isAssignableFrom(type) && SyncedProviderInstance.getInstances().containsKey(provider)) {
            return (S) new PatternProvider(provider);
        }
        return null;
    }
    
    @Override
    public <S extends ServiceProjectTemplate> List<S> findTemplates(Class<S> type) {
        if (PatternProvider.class.isAssignableFrom(type)) {
            return SyncedProviderInstance.getInstances().keySet().stream()
                    .map(PatternProvider::new)
                    .map(type::cast)
                    .collect(Collectors.toList());
        }
        return Collections.EMPTY_LIST;
    }
    
}
