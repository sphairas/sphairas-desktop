/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.iserv.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.prefs.Preferences;
import org.apache.commons.io.IOUtils;
import org.netbeans.api.keyring.Keyring;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.util.ProviderUtilities;

/**
 *
 * @author boris.heithecker
 */
public abstract class DefaultIservCredentialsController extends IservCredentialsController {

    private final ProviderInfo pi;
    private final Preferences preferences;

    protected DefaultIservCredentialsController(ProviderInfo pi) {
        this.pi = pi;
        preferences = ProviderUtilities.findPreferences(pi.getURL());
    }

    public ProviderInfo getProviderInfo() {
        return pi;
    }

    @Override
    public Lookup getLookup() {
        return Lookups.singleton(this);
    }

    @Override
    public String loadIservUser() {
        return preferences.get(getUsernamePreferencesKey(), null);
    }

    @Override
    public void storeIservUser(String user) {
        if (user != null) {
            preferences.put(getUsernamePreferencesKey(), user);
        } else {
            preferences.remove(getUsernamePreferencesKey());
        }
    }

    @Override
    public boolean hasStoredPassword() {
        return Keyring.read(getPasswordKeyringkey()) != null;
    }

    @Override
    public void storeIservPassword(char[] pw) {
        if (pw != null && pw.length != 0) {
            Keyring.save(getPasswordKeyringkey(), pw, null);
        } else {
            Keyring.delete(getPasswordKeyringkey());
        }
    }

    protected String getPasswordKeyringkey() {
//        return pi.getClass().getName() + "." + getPasswordKeyringKeySuffix();
        return pi.getURL() + "." + getPasswordKeyringKeySuffix();
    }

    @Override
    public String getHtmlPrivacyMessage() {
        InputStream is = DefaultIservCredentialsController.class.getResourceAsStream("iservPrivacyMessage.html");
        if (is != null) {
            try {
                return IOUtils.toString(is);
            } catch (IOException ex) {
            }
        }
        return null;
    }

    protected abstract String getUsernamePreferencesKey();

    protected abstract String getPasswordKeyringKeySuffix();

}
