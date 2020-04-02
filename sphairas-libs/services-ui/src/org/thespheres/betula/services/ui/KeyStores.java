/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.Date;
import java.util.prefs.Preferences;
import javax.security.auth.x500.X500Principal;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import org.netbeans.api.keyring.Keyring;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.thespheres.betula.services.ui.ks.CreateUserCertificateImpl;

/**
 *
 * @author boris.heithecker
 */
@Messages({"KeyStores.keyStore.password.description=Sphairas Schlüsselspeicher-Passwort {0,date}."})
public final class KeyStores {

    public static final String KEYRING_KEYSTORE_PASSWORD_KEY = KeyStores.class.getName() + ".keystore";
    public static final String MODULE_PREFERENCES_KEYSTORE_LOCATION_KEY = "user.keystore.location";
    public static final String MODULE_PREFERENCES_TRUSTSTORE_LOCATION_KEY = "user.trusted.certificates.location";
    public static final String MODULE_PREFERENCES_OPENSC_CONFIG_LOCATION_KEY = "opensc.sunpkcs11.config.location";
    public static final String ALIAS_APPLICATION_SECRET = "application.secret";
//    private final String KEYSTORE_LOCATION;
//    private final String TRUSTSTORE_LOCATION;
    private static KeyStores instance;

    private Preferences node;

    private KeyStores() {
    }

    private static KeyStores instance() {
        synchronized (KeyStores.class) {
            if (instance == null) {
                instance = new KeyStores();//new InstallKeyStores()
            }
        }
        return instance;
    }

//    public static void init() throws IllegalStateException {        //if not user initialized, after a fresh install, this will not happen (not open projects, domains deactivated)
//        initPKCS11();
////        try {
////            System.setProperty("javax.net.ssl.keyStore", instance().keystore());
////            System.setProperty("javax.net.ssl.trustStore", instance().truststore());
////            System.setProperty("javax.net.ssl.keyStoreType", getKeystoreType());
////            System.setProperty("javax.net.ssl.trustStoreType", getTruststoreType());
////        } catch (Exception e) { //NullPointerException if locations is null
////            PlatformUtil.getCodeNameBaseLogger(KeyStores.class).log(Level.INFO, e.getLocalizedMessage(), e);
////            throw new IllegalStateException(e);
////        }
//    }
    public static String getKeystore() {
        return instance().keystore();
//        return instance().KEYSTORE_LOCATION;
    }

    private String keystore() {
        return node().get(MODULE_PREFERENCES_KEYSTORE_LOCATION_KEY, null);
    }

    public static String getKeystoreType() {
        return "jceks";
    }

    public static String getTruststore() {
        return instance().truststore();
//        return instance().TRUSTSTORE_LOCATION;
    }

    private String truststore() {
        return node().get(MODULE_PREFERENCES_TRUSTSTORE_LOCATION_KEY, null);
//        return instance().TRUSTSTORE_LOCATION;
    }

    public static String getTruststoreType() {
        return "jks";
    }

    private Preferences node() {
        if (node == null) {
            node = NbPreferences.forModule(KeyStores.class);
        }
        return node;
    }

    public static void storeKeyStore(final KeyStore ks, final Path location, final char[] password) throws IOException {
        synchronized (KeyStores.class) {
            Path backup = null;
            if (Files.exists(location)) {
                String bak = location.toFile().getName() + ".bak";
                backup = location.resolveSibling(bak);
                Files.copy(location, backup, StandardCopyOption.REPLACE_EXISTING);
            }
            final FileChannel channel = FileChannel.open(location, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            FileLock lock = channel.lock();
            try (OutputStream os = Channels.newOutputStream(channel)) {
                ks.store(os, password);
            } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException ex) {
                throw new IOException(ex);
            } finally {
                Arrays.fill(password, '0');
                if (lock.isValid()) {
                    lock.release();
                }
            }
            if (backup != null) {
                Files.deleteIfExists(backup);
            }
        }
    }

    //never returns null
    @Messages({"KeyStores.showUserKeyStorePasswordDialog.title=Schlüsselbund-Password",
        "KeyStores.showUserKeyStorePasswordDialog.label=Password:"})
    public static char[] showUserKeyStorePasswordDialog() throws IOException {
        final JPanel panel = new JPanel();
        final JPasswordField passwordField = new JPasswordField();
        passwordField.setPreferredSize(new java.awt.Dimension(250, 25));
        final JLabel label = new JLabel();
        label.setText(NbBundle.getMessage(KeyStores.class, "KeyStores.showUserKeyStorePasswordDialog.label"));
        final GroupLayout layout = new GroupLayout(panel);
        final String storePasswordText = NbBundle.getBundle("org.thespheres.betula.services.ui.ks.Bundle").getString("PasswordSettingsVisualPanel.storeKeyStorePasswordCheckBox.text");
        final JCheckBox checkBox = new JCheckBox(storePasswordText);
        panel.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        //                        .addGroup(layout.createSequentialGroup()
                        //                                .addContainerGap()
                        //                                .addComponent(label)
                        //                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        //                                .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        //                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        //        );
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(label)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(checkBox))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(label))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(checkBox)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        final DialogDescriptor dd = new DialogDescriptor(
                panel,
                NbBundle.getMessage(KeyStores.class, "KeyStores.showUserKeyStorePasswordDialog.title"),
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                null);
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
        if (DialogDescriptor.OK_OPTION.equals(dd.getValue())) {
            char[] value = passwordField.getPassword();
            if (value != null && value.length != 0) {
                if (checkBox.isSelected()) {
                    final String description = NbBundle.getMessage(KeyStores.class, "KeyStores.keyStore.password.description", new Date());
                    Keyring.save(KeyStores.KEYRING_KEYSTORE_PASSWORD_KEY, Arrays.copyOf(value, value.length), description);
                }
                return value;
            }
        }
        throw new IOException();
    }

    //never returns null
    @Messages({"KeyStores.showUserPasswordDialog.title=Password für „{0}“",
        "KeyStores.showUserPasswordDialog.hint=Hinweis: {0}"})
    public static char[] showUserPasswordDialog(final String product, final String hint, final boolean returnNullIfCancelled) throws IOException {
        final String title = NbBundle.getMessage(KeyStores.class, "KeyStores.showUserPasswordDialog.title", product);
        final JPanel panel = new JPanel();
        final JPasswordField passwordField = new JPasswordField();
        passwordField.setPreferredSize(new java.awt.Dimension(250, 25));
        final JLabel label = new JLabel();
        label.setText(NbBundle.getMessage(KeyStores.class, "KeyStores.showUserKeyStorePasswordDialog.label"));
        final String message = hint == null ? "" : NbBundle.getMessage(KeyStores.class, "KeyStores.showUserPasswordDialog.hint", hint);
        final JLabel info = new JLabel(message);
        final GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        //                        .addGroup(layout.createSequentialGroup()
                        //                                .addContainerGap()
                        //                                .addComponent(label)
                        //                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        //                                .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        //                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        //        );
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(label)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(info))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(label))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(info)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        final DialogDescriptor dd = new DialogDescriptor(
                panel, title,
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                null);
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
        if (DialogDescriptor.OK_OPTION.equals(dd.getValue())) {
            char[] value = passwordField.getPassword();
            if (value != null && value.length != 0) {
                return value;
            }
        } else if (DialogDescriptor.CANCEL_OPTION.equals(dd.getValue()) || returnNullIfCancelled) {
            return null;
        }
        throw new IOException();
    }

    public static void addCertificate(final byte[] file, final String hostName, final boolean force) throws IOException, CertificateException, KeyStoreException {
        final Path tspath = Paths.get(KeyStores.getTruststore());
        final KeyStore trustStore;
        try {
            trustStore = KeyStore.getInstance(KeyStores.getTruststoreType());
        } catch (KeyStoreException ex) {
            throw new IOException(ex);
        }
        char[] password = Keyring.read(KeyStores.KEYRING_KEYSTORE_PASSWORD_KEY);
        if (password == null) {
            password = KeyStores.showUserKeyStorePasswordDialog();
        }
        try (final InputStream is = Files.newInputStream(tspath, StandardOpenOption.READ)) {
            trustStore.load(is, password);
        } catch (NoSuchAlgorithmException ex) {
            throw new IOException(ex);
        }

        if (trustStore.containsAlias(hostName) && !force) {
            return;
        }

        final CertificateFactory cf = CertificateFactory.getInstance("X.509");
        final Certificate cert;
        try (final InputStream is = new ByteArrayInputStream(file)) {
            cert = cf.generateCertificate(is);
        }

        trustStore.setCertificateEntry(hostName, cert);

        KeyStores.storeKeyStore(trustStore, tspath, password);
    }

    public static void addCertificate(final Certificate cert, final String hostName, final boolean force) throws IOException, CertificateException, KeyStoreException {
        final Path tspath = Paths.get(KeyStores.getTruststore());
        final KeyStore trustStore;
        try {
            trustStore = KeyStore.getInstance(KeyStores.getTruststoreType());
        } catch (KeyStoreException ex) {
            throw new IOException(ex);
        }
        char[] password = Keyring.read(KeyStores.KEYRING_KEYSTORE_PASSWORD_KEY);
        if (password == null) {
            password = KeyStores.showUserKeyStorePasswordDialog();
        }
        try (final InputStream is = Files.newInputStream(tspath, StandardOpenOption.READ)) {
            trustStore.load(is, password);
        } catch (NoSuchAlgorithmException ex) {
            throw new IOException(ex);
        }

        if (trustStore.containsAlias(hostName) && !force) {
            return;
        }

        trustStore.setCertificateEntry(hostName, cert);

        KeyStores.storeKeyStore(trustStore, tspath, password);
    }

    public static String createSelfSignedUserCertificate(final String cn, final String hostName, final Path csrOut, final boolean addToKeyStore) throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException {
        //"CN=Duke, OU=JavaSoft, O=Sun Microsystems, C=US"
        final X500Principal principal = new X500Principal("CN=user");
        final CreateUserCertificateImpl cuci = new CreateUserCertificateImpl(principal);

        if (addToKeyStore) {
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
            try (final InputStream is = Files.newInputStream(p)) {
                ks2.load(is, password);
                ks2.setEntry(hostName, cuci.getEntry(), new KeyStore.PasswordProtection(password));
            } catch (KeyStoreException ex) {
                throw new IOException(ex);
            }
            KeyStores.storeKeyStore(ks2, p, password);
        }

        if (csrOut != null) {
            cuci.writeCertRequest(csrOut);
        }

        return CreateUserCertificateImpl.PKCS10ToString(cuci.getCertRequest());
    }

    public static void addSignedKeyCertificate(final Path cer, final byte[] caCertBytes, final String hostName) throws IOException, CertificateException, KeyStoreException {
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
//        final Path tspath = Paths.get(KeyStores.getTruststore());
//        final KeyStore trustStore;
//        try {
//            trustStore = KeyStore.getInstance(KeyStores.getTruststoreType());
//        } catch (KeyStoreException ex) {
//            throw new IOException(ex);
//        }
//        try (final InputStream is = Files.newInputStream(tspath, StandardOpenOption.READ)) {
//            trustStore.load(is, password);
//        } catch (NoSuchAlgorithmException ex) {
//            throw new IOException(ex);
//        }
        final CertificateFactory cf = CertificateFactory.getInstance("X.509");
        final Certificate cert;
        try (final InputStream is = Files.newInputStream(cer)) {
            cert = cf.generateCertificate(is);
        }
        final Certificate caCert;
        try (final InputStream is = new ByteArrayInputStream(caCertBytes)) {
            caCert = cf.generateCertificate(is);
        }
        Path p = Paths.get(KeyStores.getKeystore());
        try (final InputStream is = Files.newInputStream(p)) {
            ks2.load(is, password);
            final KeyStore.PrivateKeyEntry e = (KeyStore.PrivateKeyEntry) ks2.getEntry(hostName, new KeyStore.PasswordProtection(password));
            final PrivateKey key = e.getPrivateKey();
            ks2.setKeyEntry(hostName, key, password, new Certificate[]{cert, caCert});
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException ex) {
            throw new IOException(ex);
        }
        KeyStores.storeKeyStore(ks2, p, password);
    }
}
