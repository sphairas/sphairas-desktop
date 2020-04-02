/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.project.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.thespheres.betula.services.LocalFileProperties;

/**
 *
 * @author boris.heithecker
 */
class LocalFilePropertiesImpl extends LocalFileProperties {

    private final Listener listener = new Listener();
    private final FileObject file;

    LocalFilePropertiesImpl(String name, FileObject properties) throws IOException {
        super(name, properties.getInputStream());
        file = properties;
        properties.addFileChangeListener(listener);
    }

    @Override
    protected Path getOverridesDir() {
        //No overrides
        return null;
    }

    private final class Listener extends FileChangeAdapter implements Runnable {

        @Override
        public void fileChanged(FileEvent fe) {
            fe.runWhenDeliveryOver(this);
        }

        @Override
        public void run() {
            try {
                LocalFilePropertiesImpl.this.load(file.getInputStream());
            } catch (IOException ex) {
                Logger.getLogger(LocalFilePropertiesImpl.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
            LocalFilePropertiesImpl.this.cSupport.fireChange();
        }

    }
}
