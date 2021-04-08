/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.web;

import java.util.Collections;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.jdesktop.swingx.JXLoginPane;
import org.jdesktop.swingx.auth.LoginService;
import org.netbeans.api.keyring.Keyring;
import org.openide.util.Mutex;
import org.openide.windows.WindowManager;
import org.thespheres.betula.services.ProviderRegistry;
import org.thespheres.betula.services.util.ProviderUtilities;
import org.thespheres.betula.services.web.ContextCredentials;

/**
 *
 * @author boris.heithecker
 */
public final class ContextCredentialsSupport implements ContextCredentials.Provider {

    private final String usernameKey;
    private final String passwordKey;
    private ContextCredentials credentials;
    private final String provider;

    private ContextCredentialsSupport(String provider, String usernameKey, String passwordKey) {
        this.provider = provider;
        this.usernameKey = usernameKey;
        this.passwordKey = passwordKey;
    }

    public static ContextCredentialsSupport createContextCredentialsSupport(String provider, String usernameKey, String passwordKeySuffix) {
        final String pwKey = ProviderRegistry.getDefault().get(provider).getURL() + "." + passwordKeySuffix;
        return new ContextCredentialsSupport(provider, usernameKey, pwKey);
    }

    @Override
    public synchronized ContextCredentials getContextCredentials() {
        if (credentials == null) {
            credentials = new Credentials();
        }
        return credentials;
    }

    private final class Credentials extends LoginService implements ContextCredentials, PreferenceChangeListener {

        private final Store store = new Store();
        private final Preferences preferences;

        @SuppressWarnings("LeakingThisInConstructor")
        private Credentials() {
            preferences = ProviderUtilities.findPreferences(provider);
//            NbPreferences.forModule(provider.getClass()).addPreferenceChangeListener(this);
            preferences.addPreferenceChangeListener(this);
        }

        @Override
        public String getUsername() {
            final String user = preferences.get(usernameKey, null);
            if (user != null) {
                return user;
            }
            boolean showUserLogin;
            synchronized (store) {
                showUserLogin = store.un == null;
            }
            if (showUserLogin) {
                displayLogin(user);
            }
            synchronized (store) {
                return store.un;
            }
        }

        private void displayLogin(final String user) {
            final JXLoginPane panel = new JXLoginPane(this);
            final String display = ProviderRegistry.getDefault().get(provider).getDisplayName();
            panel.setBannerText(display);
            synchronized (store) {
                if (store.un != null) {
                    panel.setUserName(store.un);
                }
                if (store.pw != null) {
                    panel.setPassword(store.pw.toCharArray());
                }
            }
            panel.setServers(Collections.singletonList(user));
            final JXLoginPane.Status status = Mutex.EVENT.writeAccess(() -> JXLoginPane.showLoginDialog(WindowManager.getDefault().getMainWindow(), panel));
            if (status.equals(JXLoginPane.Status.SUCCEEDED)) {
            }
        }

        @Override
        public char[] getPassword() {
            char[] ret = Keyring.read(passwordKey);
            if (ret != null) {
                return ret;
            }
            boolean showUserLogin;
            synchronized (store) {
                showUserLogin = store.pw == null;
            }
            if (showUserLogin) {
                final String display = ProviderRegistry.getDefault().get(provider).getDisplayName();
                displayLogin(display);
            }
            synchronized (store) {
                return store.pw != null ? store.pw.toCharArray() : "".toCharArray();
            }
        }

        @Override
        public boolean authenticate(String name, char[] password, String server) throws Exception {
            synchronized (store) {
                store.set(name, password);
            }
            return true;
        }

        @Override
        public void onFailure(String message, Exception ex) {
            synchronized (store) {
                store.clear();
            }
        }

        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            synchronized (store) {
                store.clear();
            }
        }

        private final class Store {

            private transient volatile String un;
            private transient volatile String pw;

            private void set(String u, char[] pw) {
                this.un = u;
                this.pw = new String(pw);
            }

            private void clear() {
                this.un = null;
                this.pw = null;
            }
        }
    }
}
