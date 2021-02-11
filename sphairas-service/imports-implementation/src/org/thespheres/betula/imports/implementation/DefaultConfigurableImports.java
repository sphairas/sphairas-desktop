/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.imports.implementation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.thespheres.betula.services.LocalFileProperties;
import org.thespheres.betula.ui.util.JAXBUtil;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.xmlimport.model.Product;
import org.thespheres.betula.xmlimport.model.XmlTargetImportSettings;
import org.thespheres.betula.xmlimport.parse.ImportScripts;

/**
 *
 * @author boris.heithecker
 */
public class DefaultConfigurableImports {

    public static final JAXBContext TARGETS_SETTINGS_JAXB;
    static final JAXBContext PROCESSOR_HINTS_JAXB;

    static {
        try {
            final Class[] cl = JAXBUtil.lookupJAXBTypes("XmlTargetImportSettings", XmlTargetImportSettings.class);
            TARGETS_SETTINGS_JAXB = JAXBContext.newInstance(cl);
        } catch (JAXBException ex) {
            throw new IllegalStateException(ex);
        }
        try {
            final Class[] cl = JAXBUtil.lookupJAXBTypes("XmlTargetProcessorHintsSettings", XmlTargetProcessorHintsSettings.class);
            PROCESSOR_HINTS_JAXB = JAXBContext.newInstance(cl);
        } catch (JAXBException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private DefaultConfigurableImports() {
    }

    public static DefaultConfigurableImportTarget createCommon(final String provider, final String productFile, final Product product, final URL base) throws IOException {
        final XmlTargetImportSettings settings = loadSettings(base);
        final XmlTargetProcessorHintsSettings hints = loadHints(base);
        final Properties common = loadCommonProperties(base);
        Properties prodProps = productFile != null ? loadProductProperties(base, productFile) : null;
        final Map<String, String> config = new HashMap<>();
        final LocalFileProperties lfp = LocalFileProperties.find(provider);
        if (lfp != null) {
            //            cc.addConfiguration(new MapConfiguration(lfp.getProperties()));
            config.putAll(lfp.getProperties());
        }
        common.forEach((Object k, Object v) -> config.put((String) k, (String) v));
        if (prodProps != null) {
            prodProps.forEach((Object k, Object v) -> config.put((String) k, (String) v));
        }
        final DefaultConfigurableImportTarget ret = new DefaultConfigurableImportTarget(provider, product, settings, hints);
        ret.initialize(config);
        try {
            final ImportScripts scripts = loadImportScripts(provider, config, base, "import.js");
            ret.setScripts(scripts);
        } catch (final IOException ioex) {
            PlatformUtil.getCodeNameBaseLogger(DefaultConfigurableImports.class).log(Level.INFO, "No import.js scripts for provider: {0}", provider);
        }
        return ret;
    }

    static XmlTargetImportSettings loadSettings(final URL base) throws IOException {
        final URL file = new URL(base, "defaultGrades.xml");
        try (final InputStream is = file.openStream()) {
            return (XmlTargetImportSettings) DefaultConfigurableImports.TARGETS_SETTINGS_JAXB.createUnmarshaller().unmarshal(is);
        } catch (FileNotFoundException | JAXBException ex) {
            throw new IOException(ex);
        }
    }

    static Properties loadCommonProperties(final URL base) throws IOException {
        final URL file = new URL(base, "common-import.properties");
        final Properties sprops = new Properties();
        try (final InputStream is = file.openStream()) {
            sprops.load(is);
        }
        return sprops;
    }

    static XmlTargetProcessorHintsSettings loadHints(final URL base) throws IOException {
        final URL file = new URL(base, "processorHints.xml");
        try (final InputStream is = file.openStream()) {
            return (XmlTargetProcessorHintsSettings) DefaultConfigurableImports.PROCESSOR_HINTS_JAXB.createUnmarshaller().unmarshal(is);
        } catch (FileNotFoundException | JAXBException ex) {
            throw new IOException(ex);
        }
    }

    static Properties loadProductProperties(final URL base, final String productFile) throws IOException {
        final URL file = new URL(base, productFile);
        final Properties sprops = new Properties();
        final URLConnection conn;
        try {
            conn = file.openConnection();
        } catch (IOException ioex) {
            return sprops;
        }
        try (final InputStream is = conn.getInputStream()) {
            sprops.load(is);
        }
        return sprops;
    }

    static ImportScripts loadImportScripts(final String provider, final Map<String, String> config, final URL base, final String file) throws IOException {
        final URL url = new URL(base, file);
        final URLConnection conn = url.openConnection();
        try (final InputStream is = conn.getInputStream()) {
            return ImportScripts.create(provider, config, is);
        }
    }
}
