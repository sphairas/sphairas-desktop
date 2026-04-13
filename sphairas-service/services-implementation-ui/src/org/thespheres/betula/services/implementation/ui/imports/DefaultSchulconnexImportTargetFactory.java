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
import org.thespheres.betula.imports.implementation.DefaultSchulconnexImports;
import org.thespheres.betula.schulconnex.SchulconnexImportConfiguration;
import org.thespheres.betula.services.implementation.ui.impl.SyncedProviderInstance;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.xmlimport.ImportTargetFactory;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = ImportTargetFactory.class)
public class DefaultSchulconnexImportTargetFactory extends SchulconnexImportConfiguration.Factory {

    public static final String SCHULCONNEX_PROPERTIES_FILE = "schulconnex.properties";

    @Override
    protected SchulconnexImportConfiguration doCreateInstance(String provider) throws IOException {
        if (SyncedProviderInstance.getInstances().containsKey(provider) && hasSchulconnexProperties(provider)) {
            try {
                return DefaultSchulconnexImports.create(provider, SyncedProviderInstance.getInstances().get(provider).getBaseDir().toUri().toURL());
            } catch (Exception ex) {
                PlatformUtil.getCodeNameBaseLogger(DefaultSchulconnexImportTargetFactory.class).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        }
        return null;
    }

    @Override
    public List<ProviderRef> available(Class<SchulconnexImportConfiguration> subType) {
        return SyncedProviderInstance.getInstances().keySet().stream()
                .filter(DefaultSchulconnexImportTargetFactory::hasSchulconnexProperties)
                .map(ProviderRef::new)
                .collect(Collectors.toList());
    }

    static boolean hasSchulconnexProperties(final String provider) {
        final SyncedProviderInstance i = SyncedProviderInstance.getInstances().get(provider);
        final Path p = i.getBaseDir().resolve(SCHULCONNEX_PROPERTIES_FILE);
        return Files.exists(p);
    }

}
