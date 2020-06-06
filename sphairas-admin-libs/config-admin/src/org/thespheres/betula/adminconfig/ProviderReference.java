/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminconfig;

import java.io.Serializable;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.services.ProviderInfo;

/**
 *
 * @author boris
 */
public interface ProviderReference extends Lookup.Provider, Serializable {

    public static final String MIME = "application/app-resources-ui";

    @Override
    public default Lookup getLookup() {
        return Lookups.fixed(this, getProviderInfo());
    }

    public ProviderInfo getProviderInfo();

}
