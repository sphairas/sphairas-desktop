/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units;

import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.ProviderRegistry;

/**
 *
 * @author boris.heithecker
 */
public class ConfigurationTopComponentModel {

    protected NamingResolver namingResolver;
    protected final String provider;

    protected ConfigurationTopComponentModel(String provider) {
        this.provider = provider;
    }

    public String getProvider() {
        return provider;
    }

    public ProviderInfo getProviderInfo() {
        return ProviderRegistry.getDefault().get(provider);
    }

    protected NamingResolver getNamingResolver() {
        if (namingResolver == null) {
            namingResolver = NamingResolver.find(getProviderInfo().getURL());
        }
        return namingResolver;
    }

}
