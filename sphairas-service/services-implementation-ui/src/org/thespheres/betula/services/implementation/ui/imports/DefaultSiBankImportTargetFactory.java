/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.imports;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.imports.implementation.DefaultSiBankImports;
import org.thespheres.betula.services.implementation.ui.impl.SyncedProviderInstance;
import org.thespheres.betula.sibank.SiBankImportTarget;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.xmlimport.ImportTargetFactory;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = ImportTargetFactory.class)
public class DefaultSiBankImportTargetFactory extends SiBankImportTarget.Factory {

    public static final String SIBANK_PROPERTIES_FILE = "sibank.properties";

    @Override
    protected SiBankImportTarget doCreateInstance(String provider) throws IOException {
        if (SyncedProviderInstance.getInstances().containsKey(provider) && hasSiBankProperties(provider)) {
            try {
                return DefaultSiBankImports.create(provider, SyncedProviderInstance.getInstances().get(provider).getBaseDir().toUri().toURL());
            } catch (Exception ex) {
                PlatformUtil.getCodeNameBaseLogger(DefaultSiBankImportTargetFactory.class).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        }
        return null;
    }

    @Override
    public List<ProviderRef> available(Class<SiBankImportTarget> subType) {
        return SyncedProviderInstance.getInstances().keySet().stream()
                .filter(DefaultSiBankImportTargetFactory::hasSiBankProperties)
                .map(ProviderRef::new)
                .collect(Collectors.toList());
    }

    static boolean hasSiBankProperties(final String provider) {
        final SyncedProviderInstance i = SyncedProviderInstance.getInstances().get(provider);
        final Path p = i.getBaseDir().resolve(SIBANK_PROPERTIES_FILE);
        return Files.exists(p);
    }

}
