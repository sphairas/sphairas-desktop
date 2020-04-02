/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.NoProviderException;
import org.thespheres.betula.services.ProviderRegistry;

/**
 *
 * @author boris.heithecker
 */
@Messages({"ConfigurationException.message.cause=Für den Anbieter {0} konnten folgende Resources nicht gefunden werden: {1}."})
public class ConfigurationException extends RuntimeException {

    private final String[] resources;
    private final String provider;

    public ConfigurationException(final String provider, final String... resources) {
        this.provider = provider;
        this.resources = resources;
    }

    public ConfigurationException(final LocalProperties lp, final String... resources) {
        this.provider = lp.getProperty("providerURL", lp.getName());
        this.resources = resources;
    }

    @Override
    public String getMessage() {
        String providerName;
        try {
            providerName = ProviderRegistry.getDefault().get(provider).getDisplayName();
        } catch (NoProviderException npex) {
            providerName = provider;
        }
        final String res = Arrays.stream(resources).collect(Collectors.joining(","));
        return NbBundle.getMessage(ConfigurationException.class, "ConfigurationException.message.cause", providerName, res);
    }

}
