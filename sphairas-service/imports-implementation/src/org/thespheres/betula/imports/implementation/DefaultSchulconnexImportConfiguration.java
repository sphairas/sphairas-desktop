/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.imports.implementation;

import java.util.Map;
import org.thespheres.betula.schulconnex.SchulconnexImportConfiguration;
import org.thespheres.betula.xmlimport.model.Product;
import org.thespheres.betula.xmlimport.model.XmlTargetImportSettings;

/**
 * Schulconnex-specific import target configuration. Extends
 * DefaultConfigurableImportTarget with Schulconnex-specific credential and
 * endpoint method implementations to keep experimental Schulconnex code
 * isolated from the main framework.
 *
 * @author boris.heithecker
 */
public class DefaultSchulconnexImportConfiguration extends DefaultConfigurableImportTarget implements SchulconnexImportConfiguration {

    public DefaultSchulconnexImportConfiguration(String provider, Product prod, XmlTargetImportSettings settings, XmlTargetProcessorHintsSettings hints) {
        super(provider, prod, settings, hints);
    }

    @Override
    public void initialize(final Map<String, String> properties) {
        super.initialize(properties);
        // Schulconnex-specific initialization goes here if needed
    }

    @Override
    public String getSchulconnexClientId() {
        return properties.get(SchulconnexImportConfiguration.SCHULCONNEX_CLIENTID);
    }

    @Override
    public String getSchulconnexClientSecret() {
        return System.getProperty(SchulconnexImportConfiguration.SCHULCONNEX_CLIENTSECRET);
    }

    @Override
    public String getSchulconnexApiEndpoint() {
        return properties.get(SchulconnexImportConfiguration.SCHULCONNEX_API);
    }

    @Override
    public String getSchulconnexTokenEndpoint() {
        return properties.get(SchulconnexImportConfiguration.SCHULCONNEX_TOKEN_ENDPOINT);
    }

}
