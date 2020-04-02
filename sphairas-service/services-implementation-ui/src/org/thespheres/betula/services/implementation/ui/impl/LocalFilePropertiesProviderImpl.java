/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.impl;

import com.google.common.eventbus.Subscribe;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.services.AppPropertyNames;
import org.thespheres.betula.services.LocalFileProperties;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.adminconfig.ProviderSyncEvent;
import org.thespheres.betula.ui.util.PlatformUtil;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = LocalProperties.Provider.class)
public class LocalFilePropertiesProviderImpl implements LocalFileProperties.Provider {

    static Map<String, FilePropertiesImpl> INSTANCES = new HashMap<>();

    @Override
    public LocalFileProperties find(String name) {
        if (!SyncedProviderInstance.getInstances().containsKey(name)) {
            return null;
        }
        return SyncedProviderInstance.getInstances().get(name).findLocalFileProperties();
    }

    static class FilePropertiesImpl extends LocalFileProperties implements Runnable {

        private final Path location;

        FilePropertiesImpl(String name, Path path, String parent) throws IOException {
            super(name, Files.newInputStream(path, StandardOpenOption.CREATE));
            this.location = path;
            if (parent != null) {
                setParent(LocalFileProperties.find(parent));
            }
        }

        @Override
        protected String getPropertyInsecure(final String name) {
            final String val = super.getPropertyInsecure(name);
            if (val == null && AppPropertyNames.LP_PROVIDER.equals(name)) {
                return getName();
            }
            return val;
        }

        @Subscribe
        public void fileChanged(final ProviderSyncEvent event) {
            if (event.getResource().equals("default.properties")) {
                event.runLater(this);
            }
        }

        @Override
        public void run() {
            try {
                FilePropertiesImpl.this.load(Files.newInputStream(location, StandardOpenOption.CREATE));
            } catch (IOException ex) {
                PlatformUtil.getCodeNameBaseLogger(LocalFilePropertiesProviderImpl.class).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
            FilePropertiesImpl.this.cSupport.fireChange();
        }
    }

}
