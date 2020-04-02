/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.couchdb.ui;

import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.api.keyring.Keyring;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.ProviderRegistry;

/**
 *
 * @author boris.heithecker
 */
public abstract class DefaultCouchDBCredentialsController extends CouchDBCredentialsController {

    private final ProviderInfo pi;
    private final Preferences preferences;

    protected DefaultCouchDBCredentialsController(ProviderInfo pi) {
        this.pi = pi;
        preferences = ProviderRegistry.getDefault().findPreferences(pi.getURL()); //NbPreferences.forModule(pi.getClass());
    }

    public ProviderInfo getProviderInfo() {
        return pi;
    }

    @Override
    public Lookup getLookup() {
        return Lookups.singleton(this);
    }

    @Override
    public String loadCouchDBDatabase() {
        return preferences.get(getDatabasePreferencesKey(), null);
    }

    @Override
    public void storeCouchDBDatabase(String db) {
        if (db != null) {
            preferences.put(getDatabasePreferencesKey(), db);
        } else {
            preferences.remove(getDatabasePreferencesKey());
        }
    }

    @Override
    public String loadCouchDBUser() {
        return preferences.get(getUsernamePreferencesKey(), null);
    }

    @Override
    public void storeCouchDBUser(String user) {
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
    public void storeCouchDBPassword(char[] pw) {
        if (pw != null && pw.length != 0) {
            Keyring.save(getPasswordKeyringkey(), pw, null);
        } else {
            Keyring.delete(getPasswordKeyringkey());
        }
    }

    protected String getPasswordKeyringkey() {
//        return pi.getClass().getName() + "." + getPasswordKeyringKeySuffix();
        return ProviderRegistry.getDefault().getCodeNameBase(pi.getURL()) + "." + getPasswordKeyringKeySuffix();
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return super.getComponent(masterLookup);
    }

    protected abstract String getDatabasePreferencesKey();

    protected abstract String getUsernamePreferencesKey();

    protected abstract String getPasswordKeyringKeySuffix();

}
