/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admincontainer.util;

import java.io.IOException;
import org.thespheres.betula.admin.units.AbstractUnitOpenSupport;
import org.thespheres.betula.services.NoProviderException;
import org.thespheres.betula.xmlimport.model.Product;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;

/**
 *
 * @author boris.heithecker
 */
public class TargetsUtil {

    private TargetsUtil() {
    }

    public static ConfigurableImportTarget findCommonImportTarget(final AbstractUnitOpenSupport auos) throws IOException {
        final String url = auos.findBetulaProjectProperties().getProperty("providerURL");
        if (url == null) {
            return null;
        }
        return findCommonImportTarget(url);
    }

    public static ConfigurableImportTarget findCommonImportTarget(final String url) throws IOException {
        try {
            return ConfigurableImportTarget.Factory.find(url, ConfigurableImportTarget.class, Product.NO);
        } catch (NoProviderException npex) {
            throw new IOException(npex);
        }
    }

}
