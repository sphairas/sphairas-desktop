/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ws.api;

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509KeyManager;

/**
 *
 * @author boris.heithecker
 */
public class AliasSelectorKeyManger implements X509KeyManager {

    private final X509KeyManager original;
    private final String alias;

    public AliasSelectorKeyManger(X509KeyManager original, String certName) {
        this.original = original;
        this.alias = certName;
    }

    @Override
    public String[] getClientAliases(String string, Principal[] prncpls) {
        return original.getClientAliases(string, prncpls);
    }

    @Override
    public String chooseClientAlias(String[] keyTypes, Principal[] issuers, Socket socket) {
//        PrivateKeyCallback sc = PrivateKeyCallback.getCurrent();
        if (alias == null) {
            return original.chooseClientAlias(keyTypes, issuers, socket);
        }

        if (keyTypes == null) {
            return null;
        }
//        final String name = alias != null ? alias : sc.getAlias();
        for (final String keyType : keyTypes) {
            final String[] aliases = getClientAliases(keyType, issuers);
            if ((aliases != null) && (aliases.length > 0)) {
                for (String v : aliases) {
                    if (v.equals(alias)) {
                        return v;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String[] getServerAliases(String string, Principal[] prncpls) {
        return original.getServerAliases(string, prncpls);
    }

    @Override
    public String chooseServerAlias(String string, Principal[] prncpls, Socket socket) {
        return original.chooseServerAlias(string, prncpls, socket);
    }

    @Override
    public X509Certificate[] getCertificateChain(String string) {
        return original.getCertificateChain(string);
    }

    @Override
    public PrivateKey getPrivateKey(String string) {
        return original.getPrivateKey(string);
    }

}
