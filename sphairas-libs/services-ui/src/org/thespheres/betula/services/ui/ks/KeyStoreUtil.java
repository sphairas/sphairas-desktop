/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.ks;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.netbeans.api.keyring.Keyring;
import org.thespheres.betula.services.ui.KeyStores;

/**
 *
 * @author boris.heithecker
 */
class KeyStoreUtil {

    static String[] copyPKCS12EntriesToSystemKeyStore(final Path file, char[] pkcs12pw) throws IOException {
        final KeyStore ks;
        try {
            ks = KeyStore.getInstance("PKCS12");
        } catch (KeyStoreException ex) {
            throw new IOException(ex);
        }
        try (InputStream is = Files.newInputStream(file)) {
            ks.load(is, pkcs12pw);
        } catch (NoSuchAlgorithmException | CertificateException ex) {
            throw new IOException(ex);
        }
        final List<String> ret = new ArrayList<>();
        final Map<String, KeyStore.PrivateKeyEntry> keys = new HashMap<>();
        final Map<String, Certificate[]> certs = new HashMap<>();
        Enumeration<String> aliases;
        try {
            aliases = ks.aliases();
        } catch (KeyStoreException ex) {
            throw new IOException(ex);
        }
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            final KeyStore.Entry e;
            try {
                e = ks.getEntry(alias, new KeyStore.PasswordProtection(pkcs12pw));
                if (e instanceof KeyStore.PrivateKeyEntry) {
                    keys.put(alias, (KeyStore.PrivateKeyEntry) e);
                }
            } catch (NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException ex) {
                throw new IOException(ex);
            }
        }
        final KeyStore ks2;
        try {
            ks2 = KeyStore.getInstance(KeyStores.getKeystoreType());
        } catch (KeyStoreException ex) {
            throw new IOException(ex);
        }
        char[] password = Keyring.read(KeyStores.KEYRING_KEYSTORE_PASSWORD_KEY);
        if (password == null) {
            password = KeyStores.showUserKeyStorePasswordDialog();
        }
        Path p = Paths.get(KeyStores.getKeystore());
        try (InputStream is = Files.newInputStream(p)) {
            ks2.load(is, password);
            for (Map.Entry<String, KeyStore.PrivateKeyEntry> e : keys.entrySet()) {
                final String name = e.getKey();
                certs.put(name, e.getValue().getCertificateChain());
                ks2.setEntry(name, e.getValue(), new KeyStore.PasswordProtection(password));
                ret.add(name);
            }
        } catch (NoSuchAlgorithmException | CertificateException | KeyStoreException ex) {
            throw new IOException(ex);
        } finally {
            keys.clear();
//            Arrays.fill(password, '0');
        }

        final Path tspath = Paths.get(KeyStores.getTruststore());
        final KeyStore trustStore;
        try {
            trustStore = KeyStore.getInstance(KeyStores.getTruststoreType());
        } catch (KeyStoreException ex) {
            throw new IOException(ex);
        }
        try (final InputStream is = Files.newInputStream(tspath, StandardOpenOption.READ)) {
            trustStore.load(is, password);
            for (Map.Entry<String, Certificate[]> e : certs.entrySet()) {
                final String name = e.getKey();
                int count = 0;
                for (final Certificate c : e.getValue()) {
                    String alias;
                    while (trustStore.containsAlias(alias = (count == 0 ? name : name + Integer.toString(count)))) {
                        count++;
                    }
                    trustStore.setCertificateEntry(alias, c);
                }
            }
        } catch (NoSuchAlgorithmException | CertificateException | KeyStoreException ex) {
            throw new IOException(ex);
        }
//        char[] pw2 = Keyring.read(KeyStores.KEYRING_KEYSTORE_PASSWORD_KEY);
//        if (pw2 == null) {
//            pw2 = KeyStores.showUserKeyStorePasswordDialog();
//        }
        final char[] cp = Arrays.copyOf(password, password.length);
        KeyStores.storeKeyStore(ks2, p, password);
        KeyStores.storeKeyStore(trustStore, tspath, cp);
        return ret.toArray(String[]::new);
    }

    static void updateCertificate(String csrAlias, String certs, String destAlias) throws CertificateException, IOException, KeyStoreException, UnrecoverableKeyException {
//        BufferedInputStream bis = new BufferedInputStream(certIn);
        final InputStream in = IOUtils.toInputStream(certs, "UTF-8");

        final CertificateFactory cf = CertificateFactory.getInstance("X.509");
        final Certificate[] chain = cf.generateCertificates(in).stream()
                .toArray(Certificate[]::new);
        final KeyStore ks2;
        try {
            ks2 = KeyStore.getInstance(KeyStores.getKeystoreType());
        } catch (KeyStoreException ex) {
            throw new IOException(ex);
        }
        char[] password = Keyring.read(KeyStores.KEYRING_KEYSTORE_PASSWORD_KEY);
        if (password == null) {
            password = KeyStores.showUserKeyStorePasswordDialog();
        }
        final Path p = Paths.get(KeyStores.getKeystore());
        try (InputStream is = Files.newInputStream(p)) {
            ks2.load(is, password);
            final PrivateKey key = (PrivateKey) ks2.getKey(csrAlias, password);
            ks2.setKeyEntry(destAlias, key, password, chain);
            ks2.deleteEntry(csrAlias);
        } catch (NoSuchAlgorithmException | CertificateException | ClassCastException ex) {
            throw new IOException(ex);
        } finally {
            Arrays.fill(password, '0');
        }
    }

    //keytool –keystore clientkeystore –certreq –alias client –keyalg rsa 
//–file client.csr
}
