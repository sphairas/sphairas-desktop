/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis.impl;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.gpuntis.UntisImportConfiguration;
import org.thespheres.betula.gpuntis.xml.Document;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.ServiceConstants;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.ui.util.HttpUtilities;
import org.thespheres.betula.services.ui.util.dav.URLs;

/**
 *
 * @author boris.heithecker
 */
@Messages({"UploadXml.missingHref.exception=Kein Upload-Pfad konfiguiert.",
    "UploadXml.error.httpResponse=Beim Hochladen der Datei ist ein Fehler aufgetreten: http-Status {0} ({1})"})
public class UploadXml {

    public static void upload(final UntisImportConfiguration context, final Document untis) throws IOException {
        final String resource = context.getUntisXmlDocumentResource();
        if (resource == null) {
            String msg = NbBundle.getMessage(UploadXml.class, "UploadXml.missingHref.exception");
            throw new IOException(msg);
        }
        final String provider = context.getProviderInfo().getURL();
        final byte[] out = UntisXmlDataObject.saveData(untis);
        saveUntisDocument(out, provider, resource);
        uploadUntisDocument(out, provider, resource);

    }

    static void saveUntisDocument(final byte[] out, final String provider, final String resource) throws IOException {
        final Path base = ServiceConstants.providerConfigBase(provider);
        final Path p = base.resolve(resource);
        Files.write(p, out);
    }

    static void uploadUntisDocument(final byte[] out, final String provider, final String resource) throws IOException {
        final String davBase = URLs.adminResourcesDavBase(LocalProperties.find(provider));
        final WebProvider service = WebProvider.find(provider, WebProvider.class);
        final URI uri = URI.create(davBase + resource);
        HttpUtilities.put(service, uri, out, null, null);
    }

}
