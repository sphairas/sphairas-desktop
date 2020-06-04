/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collection;
import java.util.logging.Level;
import org.openide.filesystems.Repository.LayerProvider;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = LayerProvider.class)
public class LayerProv extends LayerProvider {

    private final static RequestProcessor RP = new RequestProcessor(LayerProv.class);

    @Override
    protected void registerLayers(Collection<? super URL> context) {
        SyncedProviderInstance.getInstances().values().stream()
//                .peek(spi -> spi.updater())
                .map(spi -> spi.getBaseDir().resolve("layer.xml"))
                .filter(lp -> (Files.exists(lp)))
                .forEachOrdered(lp -> {
                    final URL url;
                    try {
                        url = lp.toUri().toURL();
                        context.add(url);
                        PlatformUtil.getCodeNameBaseLogger(LayerProv.class).log(Level.INFO, "Added layer {0}.", url);
                    } catch (MalformedURLException ex) {
                    }
                });
    }

    static RequestProcessor.Task fireUpdate() {
        return RP.post(() -> Lookup.getDefault().lookupAll(LayerProvider.class).stream()
                .filter(LayerProv.class::isInstance)
                .map(LayerProv.class::cast)
                .collect(CollectionUtil.requireSingleton())
                .orElseThrow(IllegalStateException::new)
                .refresh());
    }

}
