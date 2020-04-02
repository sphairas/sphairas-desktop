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
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import javax.crypto.SecretKey;
import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.encryption.XMLEncryptionException;
import org.netbeans.api.keyring.Keyring;
import org.thespheres.betula.services.ui.KeyStores;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author boris.heithecker
 */
public class BackupUtilImpl {

    static {
        org.apache.xml.security.Init.init();
//        com.sun.org.apache.xml.internal.security.Init.init();
    }

    public void cypherXMLDocument(Document document, boolean encrypt) throws IOException {
        final Element rootElement = document.getDocumentElement();
        XMLCipher xmlCipher;
        try {
            xmlCipher = XMLCipher.getInstance(XMLCipher.AES_128);
        } catch (XMLEncryptionException ex) {
            throw new IOException(ex);
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
        SecretKey sk;
        try (InputStream is = Files.newInputStream(p)) {
            ks2.load(is, password);
            sk = (SecretKey) ks2.getKey(KeyStores.ALIAS_APPLICATION_SECRET, password);
        } catch (NoSuchAlgorithmException | CertificateException | KeyStoreException | UnrecoverableKeyException | ClassCastException ex) {
            throw new IOException(ex);
        } finally {
            Arrays.fill(password, '0');
        }
        try {
            final int mode = encrypt ? XMLCipher.ENCRYPT_MODE : XMLCipher.DECRYPT_MODE;
            /* Initialize cipher with given secret key and operational mode */
            xmlCipher.init(mode, sk);
        } catch (XMLEncryptionException ex) {
            throw new IOException(ex);
        }

        try {
            /* Process the contents of document */
            xmlCipher.doFinal(document, rootElement, true);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }
}
