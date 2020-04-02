/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.ks;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;
import java.util.logging.Level;
import org.openide.modules.OnStop;
import org.openide.util.NbPreferences;
import org.thespheres.betula.services.ui.KeyStores;
import org.thespheres.betula.ui.util.PlatformUtil;

/**
 *
 * @author boris.heithecker
 */
@OnStop
public class UpdateTrustStore implements Runnable {

    @Override
    public void run() {
        final LocalDateTime moduleTime = InstallKeyStores.getTrustStoreModuleDateTime();
        final LocalDateTime installTime = Optional.ofNullable(NbPreferences.forModule(KeyStores.class).get("cacerts.date", null))
                .map(t -> LocalDateTime.parse(t, InstallKeyStores.DTF))
                .orElse(LocalDateTime.of(2014, Month.JANUARY, 1, 0, 0));
        if (moduleTime.isAfter(installTime)) {
            PlatformUtil.getCodeNameBaseLogger(InstallKeyStores.class).log(Level.INFO, "Truststore must be updated.");
            try {
                //TODO: User dialog, logging ....., hook on shutdown, lock cacerts.jks
                InstallKeyStores.updateTrustStoreInstance();
                PlatformUtil.getCodeNameBaseLogger(InstallKeyStores.class).log(Level.INFO, "Truststore successfully updated.");
            } catch (IOException ex) {
                PlatformUtil.getCodeNameBaseLogger(InstallKeyStores.class).log(Level.SEVERE, "An error has occurred updating the truststore.", ex);
            }
        }
    }
}
