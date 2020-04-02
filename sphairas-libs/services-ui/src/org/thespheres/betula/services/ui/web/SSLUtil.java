/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.web;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import org.netbeans.api.keyring.Keyring;
import org.openide.util.NbPreferences;
import org.thespheres.betula.services.ui.KeyStores;
import static org.thespheres.betula.services.ui.KeyStores.MODULE_PREFERENCES_OPENSC_CONFIG_LOCATION_KEY;
import org.thespheres.betula.services.ws.api.AliasSelectorKeyManger;
import org.thespheres.betula.services.ws.api.BetulaServiceClient;

/**
 *
 * @author boris.heithecker
 */
public class SSLUtil {

    private static void initPKCS11() {
        final String cfg = NbPreferences.forModule(KeyStores.class).get(MODULE_PREFERENCES_OPENSC_CONFIG_LOCATION_KEY, null);
        if (cfg != null) {
            try {//start configName with -- -> inlineconfig, lines... \\n
//                Provider pr = new sun.security.pkcs11.SunPKCS11(cfg);
//                Security.addProvider(pr);
//JAVA 9
                final Provider prototype = Security.getProvider("SunPKCS11");
                final Provider pr = prototype.configure(cfg);
                Security.addProvider(pr);
            } catch (Exception ex) {
                Logger.getLogger(KeyStores.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    //TODO: optionally use system cacerts.
    public static SSLContext createSSLContext(final String certName) throws IllegalStateException {
//        KeyStores.init();
        initPKCS11();
        char[] password = null;
        SSLContext ctx = null;
        try {
            ctx = SSLContext.getInstance("TLSv1.2"); //.getInstance("SSLv3");  //TLSv1.2
            final KeyManagerFactory kstorefac = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            final Path kspath = Paths.get(KeyStores.getKeystore());
            final KeyStore kstore = KeyStore.getInstance(KeyStores.getKeystoreType());
            password = Keyring.read(KeyStores.KEYRING_KEYSTORE_PASSWORD_KEY);
            if (password == null) {
                password = KeyStores.showUserKeyStorePasswordDialog();
            }
            kstore.load(Files.newInputStream(kspath, StandardOpenOption.READ), password);
            kstorefac.init(kstore, password);
            final KeyManager[] kms = kstorefac.getKeyManagers();
            if (certName != null) {
                for (int i = 0; i < kms.length; i++) {
                    if (kms[i] instanceof X509KeyManager) {
                        kms[i] = new AliasSelectorKeyManger((X509KeyManager) kms[i], certName);
                    }
                }
            }
            final TrustManagerFactory tstorefac = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            final Path tspath = Paths.get(KeyStores.getTruststore());
            final KeyStore tstore = KeyStore.getInstance(KeyStores.getTruststoreType()); //KeyStore.getDefaultType()
            tstore.load(Files.newInputStream(tspath, StandardOpenOption.READ), password);
            tstorefac.init(tstore);
            ctx.init(kms, tstorefac.getTrustManagers(), new SecureRandom());
        } catch (NoSuchAlgorithmException | KeyStoreException | IOException | CertificateException | UnrecoverableKeyException | KeyManagementException ex) {
            //TODO handle UIExceptions, retry etc.
            Logger.getLogger(BetulaServiceClient.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (password != null) {
                Arrays.fill(password, '0');
            }
        }
        return ctx;
    }

}
