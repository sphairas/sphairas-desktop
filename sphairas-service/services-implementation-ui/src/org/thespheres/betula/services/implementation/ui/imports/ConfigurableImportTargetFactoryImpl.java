/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.imports;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.imports.implementation.DefaultConfigurableImports;
import org.thespheres.betula.services.implementation.ui.impl.SyncedProviderInstance;
import org.thespheres.betula.xmlimport.ImportTargetFactory;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = ImportTargetFactory.class)
public class ConfigurableImportTargetFactoryImpl extends ConfigurableImportTarget.Factory {

    @Override
    protected ConfigurableImportTarget doCreateInstance(String provider) throws IOException {
        if (SyncedProviderInstance.getInstances().containsKey(provider)) {
            return DefaultConfigurableImports.createCommon(provider, null, getProduct(), SyncedProviderInstance.getInstances().get(provider).getBaseDir().toUri().toURL());
        }
        return null;
    }

    @Override
    public List<ProviderRef> available(Class<ConfigurableImportTarget> subType) {
        return SyncedProviderInstance.getInstances().keySet().stream()
                .map(ProviderRef::new)
                .collect(Collectors.toList());
    }
}
