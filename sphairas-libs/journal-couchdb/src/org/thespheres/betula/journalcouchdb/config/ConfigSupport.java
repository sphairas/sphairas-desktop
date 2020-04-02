/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journalcouchdb.config;

import java.util.Base64;
import java.util.Properties;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;

/**
 *
 * @author boris.heithecker
 */
public class ConfigSupport extends CouchDbRepositorySupport<Config> {

    public ConfigSupport(CouchDbConnector db) {
        super(Config.class, db);
    }

    public void updateProperties(Properties prop) {
        if (!contains(Config.CONFIG_DOCUMENT_ID)) {
            add(Config.create());
        }
        final Config cfg = get(Config.CONFIG_DOCUMENT_ID);
        final String updatesUrl = prop != null ? prop.getProperty("updates.href") : null;
        if (updatesUrl == null) {
            return;
        }
        cfg.setUpdatesHref(updatesUrl);
        String un = null; //org.openide.util.NbPreferences
        char[] pw = null; //org.netbeans.api.keyring.Keyring
        if (un != null && pw != null) {
            byte[] concat = (un + ":" + new String(pw)).getBytes();
            String encoded = Base64.getEncoder().encodeToString(concat);
            cfg.setUpdatesCreds(encoded);
        }
        update(cfg);
    }
}
