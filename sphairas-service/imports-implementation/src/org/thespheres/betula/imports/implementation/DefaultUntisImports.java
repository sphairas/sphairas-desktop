/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.imports.implementation;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import org.apache.commons.lang3.StringUtils;
import org.thespheres.betula.gpuntis.Untis;
import org.thespheres.betula.gpuntis.UntisImportConfiguration;
import org.thespheres.betula.services.ServiceConstants;

/**
 *
 * @author boris.heithecker
 */
public class DefaultUntisImports {

    private DefaultUntisImports() {
    }

    public static DefaultConfigurableImportTarget create(final String provider, final URL base) throws IOException {
        DefaultConfigurableImportTarget ret = DefaultConfigurableImports.createCommon(provider, "untis.properties", Untis.getProduct(), base);
        final String davBase = ret.getDavBase();
//        final String sourceTargetLinksConfigFile = ProviderRegistry.getDefault().configBase(ret.getProviderInfo().getURL()) + UntisImportConfiguration.LINKS_FILENAME;
        final Path sourceTargetLinksConfigFile = ServiceConstants.providerConfigBase(provider).resolve(UntisImportConfiguration.LINKS_FILENAME);
        ret.setSourceTargetLinksConfigFile(sourceTargetLinksConfigFile.toString());
        if (!StringUtils.isBlank(davBase)) {
            ret.setSourceTargetLinksWebDavUrl(davBase + UntisImportConfiguration.LINKS_FILENAME);
            ret.setUntisXmlDocumentUploadHref(davBase + UntisImportConfiguration.UNTIS_FILENAME);
        }
        return ret;
    }

}
