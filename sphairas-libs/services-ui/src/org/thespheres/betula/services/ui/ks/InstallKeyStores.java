/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.ks;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.KeyStore;
import java.security.KeyStore.SecretKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.swing.Icon;
import org.apache.commons.lang3.RandomStringUtils;
import org.netbeans.api.keyring.Keyring;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.WizardDescriptor;
import org.openide.awt.NotificationDisplayer;
import org.openide.modules.OnStart;
import org.openide.modules.Places;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;
import org.thespheres.betula.services.ui.KeyStores;
import org.thespheres.betula.services.ui.Privacy;
import org.thespheres.betula.ui.util.LogLevel;
import org.thespheres.betula.ui.util.PlatformUtil;

/**
 *
 * @author boris.heithecker
 */
@OnStart
public class InstallKeyStores implements Runnable {

    public static final Path KEYSTORE_PLACE = Places.getUserDirectory().toPath().resolve("keystore.jks");
    public static final Path CACERTS_PLACE = Places.getUserDirectory().toPath().resolve("cacerts.jks");
    public static final Path OPENSC_CONFIG_PLACE = Places.getUserDirectory().toPath().resolve("opensc.config");
    static final String DEFAULT_PASSWORD = "changeit";
//    public static final String[] DEFAULT_KEY_ALIASES = new String[]{"s1as", "glassfish-instance"};
    static DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd' 'HH:mm");

    private static void createKeyStore() throws IOException {
        boolean existsKeyStore = Files.exists(KEYSTORE_PLACE);
        if (!existsKeyStore) {
            char[] password = Keyring.read(KeyStores.KEYRING_KEYSTORE_PASSWORD_KEY);
            if (password == null) {
                password = KeyStores.showUserKeyStorePasswordDialog();
            }
            createKeyStoreInstance(password);
//            Arrays.fill(password, '0');
        }
    }

    static void createKeyStoreInstance(char[] password) throws IOException {
        final KeyStore ks;
        try {
            ks = KeyStore.getInstance(KeyStores.getKeystoreType());
        } catch (KeyStoreException ex) {
            throw new IOException(ex);
        }
        try {
            ks.load(null, DEFAULT_PASSWORD.toCharArray());
        } catch (NoSuchAlgorithmException | CertificateException ex) {
            throw new IOException(ex);
        }
//        try (OutputStream os = Files.newOutputStream(KEYSTORE_PLACE, StandardOpenOption.CREATE_NEW)) {
//            ks.store(os, password);
//            Arrays.fill(password, '0');
//        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException ex) {
//            throw new IOException(ex);
//        }
        KeyStores.storeKeyStore(ks, KEYSTORE_PLACE, password);
        NbPreferences.forModule(KeyStores.class).put(KeyStores.MODULE_PREFERENCES_KEYSTORE_LOCATION_KEY, KEYSTORE_PLACE.toString());
    }

    private static void createTrustStore() throws IOException {
        boolean existsTrustStore = Files.exists(CACERTS_PLACE);
        if (!existsTrustStore) {
            char[] password = Keyring.read(KeyStores.KEYRING_KEYSTORE_PASSWORD_KEY);
            if (password == null) {
                password = KeyStores.showUserKeyStorePasswordDialog();
            }
            createTrustStoreInstance(password);
//            Arrays.fill(password, '0');
        }
    }

    static void updateTrustStoreInstance() throws IOException {
        char[] password = Keyring.read(KeyStores.KEYRING_KEYSTORE_PASSWORD_KEY);
        if (password == null) {
            password = KeyStores.showUserKeyStorePasswordDialog();
        }
        final LocalDateTime date = getTrustStoreModuleDateTime();
        //
        final KeyStore updateKS;
        try {
            updateKS = KeyStore.getInstance(KeyStores.getTruststoreType());
        } catch (KeyStoreException ex) {
            throw new IOException(ex);
        }
        try (final InputStream is = InstallKeyStores.class.getResourceAsStream("/org/thespheres/betula/services/ui/resources/cacerts.jks")) {
            updateKS.load(is, DEFAULT_PASSWORD.toCharArray());
        } catch (NoSuchAlgorithmException | CertificateException ex) {
            throw new IOException(ex);
        }
        //
        final Path tspath = Paths.get(KeyStores.getTruststore());
        final KeyStore installedKS;
        try {
            installedKS = KeyStore.getInstance(KeyStores.getTruststoreType());
        } catch (KeyStoreException ex) {
            throw new IOException(ex);
        }
        try (final InputStream is = Files.newInputStream(tspath, StandardOpenOption.READ)) {
            installedKS.load(is, password);
        } catch (NoSuchAlgorithmException | CertificateException ex) {
            throw new IOException(ex);
        }
        //
        Enumeration<String> aliases;
        try {
            aliases = updateKS.aliases();
        } catch (KeyStoreException ex) {
            throw new IOException(ex);
        }
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            try {
                final Certificate e = updateKS.getCertificate(alias);
                installedKS.setCertificateEntry(alias, e);
            } catch (KeyStoreException ex) {
                throw new IOException(ex);
            }
        }
        KeyStores.storeKeyStore(updateKS, CACERTS_PLACE, password);
        NbPreferences.forModule(KeyStores.class).put(KeyStores.MODULE_PREFERENCES_TRUSTSTORE_LOCATION_KEY, CACERTS_PLACE.toString());
        NbPreferences.forModule(KeyStores.class).put("cacerts.date", date.format(DTF));
    }

    static void createTrustStoreInstance(final char[] password) throws IOException {
        final LocalDateTime date = getTrustStoreModuleDateTime();
        try (InputStream is = InstallKeyStores.class.getResourceAsStream("/org/thespheres/betula/services/ui/resources/cacerts.jks")) {
            Files.copy(is, CACERTS_PLACE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ioex) {
            throw ioex;
        }
        final KeyStore ks;
        try {
            ks = KeyStore.getInstance(KeyStores.getTruststoreType());
        } catch (KeyStoreException ex) {
            throw new IOException(ex);
        }
        try (InputStream is = Files.newInputStream(CACERTS_PLACE)) {
            ks.load(is, DEFAULT_PASSWORD.toCharArray());
        } catch (NoSuchAlgorithmException | CertificateException ex) {
            throw new IOException(ex);
        }
        KeyStores.storeKeyStore(ks, CACERTS_PLACE, password);
        NbPreferences.forModule(KeyStores.class).put(KeyStores.MODULE_PREFERENCES_TRUSTSTORE_LOCATION_KEY, CACERTS_PLACE.toString());
        NbPreferences.forModule(KeyStores.class).put("cacerts.date", date.format(DTF));
    }

    static LocalDateTime getTrustStoreModuleDateTime() {
        final String dateProp = NbBundle.getMessage(KeyStores.class, "cacerts.date");
        return LocalDateTime.parse(dateProp, DTF);
    }

    @Override
    public void run() {
        try {
            KeyStores.check();
            Privacy.okay();
        } catch (IllegalStateException illex) {
            WindowManager.getDefault().invokeWhenUIReady(this::initUserDialog);
        }
    }

    @Messages({"InstallKeyStores.userDialog.title=Datenschutz und Passwort"})
    private void initUserDialog() {
        //TODO: wizard, 1. show user privacy (private computer, shared), combobox agreement
        //2. generate keystore, optional store password if 

        WizardDescriptor wiz = new WizardDescriptor(new UserAgreementWizardIterator());
        // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
        // {1} will be replaced by WizardDescriptor.Iterator.name()
        wiz.setTitleFormat(new MessageFormat("{0} ({1})"));
        wiz.setTitle(NbBundle.getMessage(InstallKeyStores.class, "InstallKeyStores.userDialog.title"));
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            try {
                createTrustStore();
            } catch (IOException ex) {
                logError(ex);
            }
            try {
                createKeyStore();
            } catch (IOException ex) {
                logError(ex);
            }
            try {
                createDefaultAppSecret();
            } catch (IOException ex) {
                logError(ex);

            }
//            try {
//                copyKeys(DEFAULT_KEY_ALIASES, InstallKeyStores.class
//                        .getResource("/org/thespheres/betula/services/ui/resources/keystore.jks"));
//            } catch (IOException ex) {
//                logError(ex);
//
//            }
        } else {
            LifecycleManager lcm = Lookup.getDefault().lookup(LifecycleManager.class);
            lcm.exit();
        }
    }

    @Messages({"InstallKeystores.initUserDialog=Beim Initialisieren der Schlüsselspeicher ist ein Fehler aufgetreten (siehe Log-Dateien).",
        "InstallKeystores.initUserDialog.title=Fehler"})
    static void logError(IOException ex) {
        PlatformUtil.getCodeNameBaseLogger(InstallKeyStores.class).log(LogLevel.INFO_WARNING, ex.getMessage(), ex);
        final Icon ic = ImageUtilities.loadImageIcon("org/thespheres/betula/ui/resources/exclamation-red-frame.png", true);
        final String title = NbBundle.getMessage(InstallKeyStores.class,
                "InstallKeystores.initUserDialog.title");
        final String message = NbBundle.getMessage(InstallKeyStores.class,
                "InstallKeystores.initUserDialog");
        NotificationDisplayer.getDefault()
                .notify(title, ic, message, null, NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.WARNING);
    }

    private static void createDefaultAppSecret() throws IOException {
        char[] password = Keyring.read(KeyStores.KEYRING_KEYSTORE_PASSWORD_KEY);
        if (password == null) {
            password = KeyStores.showUserKeyStorePasswordDialog();
        }
        createDefaultAppSecretInstance(password);
//        Arrays.fill(password, '0');
    }

    static void createDefaultAppSecretInstance(char[] password) throws IOException {
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
        final KeyStore ks;
        try {
            ks = KeyStore.getInstance(KeyStores.getKeystoreType());
        } catch (KeyStoreException ex) {
            throw new IOException(ex);
        }
        final Path p = Paths.get(KeyStores.getKeystore());
        final SecretKey skey = keyGenerator.generateKey();
        final KeyStore.PasswordProtection sProtection = new KeyStore.PasswordProtection(password);
        try (final InputStream is = Files.newInputStream(p)) {
            ks.load(is, password);
        } catch (NoSuchAlgorithmException | CertificateException ex) {
            throw new IOException(ex);
        }
        final SecretKeyEntry entry = new SecretKeyEntry(skey);
        try {
            ks.setEntry(KeyStores.ALIAS_APPLICATION_SECRET, entry, sProtection);
        } catch (KeyStoreException ex) {
            throw new IOException(ex);
        }
        KeyStores.storeKeyStore(ks, p, password);
    }

//    private static void copyKeys(String[] aliases, URL url) throws IOException {
//        char[] password = Keyring.read(KeyStores.KEYRING_KEYSTORE_PASSWORD_KEY);
//        if (password == null) {
//            password = KeyStores.showUserKeyStorePasswordDialog();
//        }
//        copyDefaultKeys(aliases, url, password);
////        Arrays.fill(password, '0');
//    }
    static void copyDefaultKeys(String[] aliases, URL url, char[] password) throws IOException {
        final KeyStore ks;
        try {
            ks = KeyStore.getInstance(KeyStore.getDefaultType());
        } catch (KeyStoreException ex) {
            throw new IOException(ex);
        }
        try (InputStream is = url.openStream()) {
            ks.load(is, DEFAULT_PASSWORD.toCharArray());
        } catch (NoSuchAlgorithmException | CertificateException ex) {
            throw new IOException(ex);
        }

        final Map<String, KeyStore.Entry> keys = new HashMap<>();
        for (String alias : aliases) {
            final KeyStore.Entry e;
            try {
                e = ks.getEntry(alias, new KeyStore.PasswordProtection(DEFAULT_PASSWORD.toCharArray()));
                keys.put(alias, e);
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
        Path p = Paths.get(KeyStores.getKeystore());
        try (InputStream is = Files.newInputStream(p)) {
            ks2.load(is, password);
            for (Map.Entry<String, KeyStore.Entry> e : keys.entrySet()) {
                ks2.setEntry(e.getKey(), e.getValue(), new KeyStore.PasswordProtection(password));
            }
        } catch (NoSuchAlgorithmException | CertificateException | KeyStoreException ex) {
            throw new IOException(ex);
        } finally {
            keys.clear();
        }
        KeyStores.storeKeyStore(ks2, p, password);
    }

    static String findRandomPassword() {
        return RandomStringUtils.randomAlphanumeric(24).toUpperCase();
    }
}
