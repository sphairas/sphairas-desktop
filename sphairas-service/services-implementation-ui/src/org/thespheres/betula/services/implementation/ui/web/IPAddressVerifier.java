/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.web;

import javax.net.ssl.SSLException;
import org.apache.http.conn.ssl.AbstractVerifier;

/**
 *
 * @author boris.heithecker
 */
@Deprecated //Remove and user SAN certificate entries
class IPAddressVerifier extends AbstractVerifier {

    private final String ipAddress;
    private final String hostName;

    IPAddressVerifier(String ipAddress, String hostName) {
        this.ipAddress = ipAddress;
        this.hostName = hostName;
    }

    @Override
    public void verify(final String host, final String[] cns, final String[] subjectAlts) throws SSLException {
        final boolean match = ipAddress.equals(host)
                && cns.length > 0
                && cns[0].equals(hostName);
        if (!match) {
            throw new SSLException("Host name mismatch");
        }
    }

}
