/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.util.dav;

import java.io.IOException;
import java.net.URI;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.services.ui.util.HttpUtilities;

/**
 *
 * @author boris.heithecker
 */
public class ResourcesUtil {

    private static CertificateFactory CF;

    static {
        try {
            CF = CertificateFactory.getInstance("X.509");
        } catch (CertificateException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Messages("Certificate.fetchProviderCertificate.urlHint=http://{0}:8080/web/dav/public/server.crt")
    public static Certificate fetchProviderCertificate(final URI uri) throws IOException {
        final Certificate ret = HttpUtilities.get(null, uri, (lm, is) -> {
            try {
                return CF.generateCertificate(is);
            } catch (CertificateException ex) {
                throw new IOException(ex);
            }
        }, null, false);
        return ret;
    }
}
