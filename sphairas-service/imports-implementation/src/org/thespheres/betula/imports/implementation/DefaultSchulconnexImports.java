/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.imports.implementation;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import org.thespheres.betula.schulconnex.Schulconnex;
import org.thespheres.betula.services.LocalFileProperties;
import org.thespheres.betula.xmlimport.model.XmlTargetImportSettings;

/**
 *
 * @author boris.heithecker
 */
public class DefaultSchulconnexImports {

    private DefaultSchulconnexImports() {
    }

    public static DefaultSchulconnexImportConfiguration create(final String provider, final URL base) throws IOException {
        final XmlTargetImportSettings settings = DefaultConfigurableImports.loadSettings(base);
        final XmlTargetProcessorHintsSettings hints = DefaultConfigurableImports.loadHints(base);

        final Properties common = DefaultConfigurableImports.loadCommonProperties(base);
        final Properties schulconnexProps = DefaultConfigurableImports.loadProductProperties(base, "schulconnex.properties");
        final Map<String, String> config = new HashMap<>();
        final LocalFileProperties lfp = LocalFileProperties.find(provider);
        if (lfp != null) {
            //            cc.addConfiguration(new MapConfiguration(lfp.getProperties()));
            config.putAll(lfp.getProperties());
        }
        common.forEach((Object k, Object v) -> config.put((String) k, (String) v));
        schulconnexProps.forEach((Object k, Object v) -> config.put((String) k, (String) v));

        final DefaultSchulconnexImportConfiguration ret = new DefaultSchulconnexImportConfiguration(provider, Schulconnex.getProduct(), settings, hints);
        ret.initialize(config);
        return ret;
    }

}
