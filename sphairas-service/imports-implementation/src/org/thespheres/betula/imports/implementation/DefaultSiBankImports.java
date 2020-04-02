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
import org.thespheres.betula.services.ServiceConstants;
import org.thespheres.betula.sibank.SiBankImportTarget;
import org.thespheres.betula.sibank.SiBankPlus;

/**
 *
 * @author boris.heithecker
 */
public class DefaultSiBankImports {

    private DefaultSiBankImports() {
    }

    public static DefaultConfigurableImportTarget create(String provider, URL base) throws IOException {
        final DefaultConfigurableImportTarget ret = DefaultConfigurableImports.createCommon(provider, "sibank.properties", SiBankPlus.getProduct(), base);
        final String davBase = ret.getDavBase();
        final Path sourceTargetLinksConfigFile = ServiceConstants.providerConfigBase(provider).resolve(SiBankImportTarget.LINKS_FILENAME);
        ret.setSourceTargetLinksConfigFile(sourceTargetLinksConfigFile.toString());
        if (!StringUtils.isBlank(davBase)) {
            ret.setSourceTargetLinksWebDavUrl(davBase + SiBankImportTarget.LINKS_FILENAME);
        }
        return ret;
    }

}
