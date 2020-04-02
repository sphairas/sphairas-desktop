/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.impl;

import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.niedersachsen.NdsCommonConstants;
import org.thespheres.betula.services.LocalFileProperties;
import org.thespheres.betula.services.LocalProperties;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = LocalProperties.Provider.class)
public class NdsCommonConstantsProvider implements LocalFileProperties.Provider {

    @Override
    public LocalFileProperties find(String name) {
        return name.equals(NdsCommonConstants.getDefaultProperties().getName()) ? NdsCommonConstants.getDefaultProperties() : null;
    }

}
