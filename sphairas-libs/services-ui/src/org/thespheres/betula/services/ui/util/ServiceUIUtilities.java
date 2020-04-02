/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.util;

import org.thespheres.betula.document.model.DocumentsModel;
import org.thespheres.betula.services.LocalFileProperties;
import org.thespheres.betula.services.ProviderInfo;

/**
 *
 * @author boris.heithecker
 */
public class ServiceUIUtilities {

    public static DocumentsModel findDocumentsModelFromProvider(final ProviderInfo provider) {
        final LocalFileProperties lfp = LocalFileProperties.find(provider);
        final DocumentsModel dm = new DocumentsModel();
        dm.initialize(lfp.getProperties());
        return dm;
    }
}
