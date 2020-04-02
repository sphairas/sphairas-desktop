/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen;

import java.io.IOException;
import org.thespheres.betula.services.LocalFileProperties;

/**
 *
 * @author boris.heithecker
 */
class NdsDefaultProperties extends LocalFileProperties {

    private static final NdsDefaultProperties INSTANCE;

    static {
        try {
            INSTANCE = new NdsDefaultProperties();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private NdsDefaultProperties() throws IOException {
        super("niedersachsen", "default.properties");
    }

    static LocalFileProperties get() {
        return INSTANCE;
    }
}
