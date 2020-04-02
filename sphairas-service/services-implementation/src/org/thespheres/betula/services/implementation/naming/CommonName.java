/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.naming;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.services.util.Units;

/**
 *
 * @author boris.heithecker
 */
class CommonName extends StaticName implements Runnable {

    static final int RELOAD_INTERVAL = 10000;
    static final int WAIT_TIME = 1500;
    private Object resolved;
    private final UnitId ag;
    protected final String provider;
    static final Map<String, CommonNamesProvider> RPMAP = new HashMap<>();
    static final int AGNAME_RP_THROUGHPUT = 8;
    private final RequestProcessor.Task init;

    @SuppressWarnings({"LeakingThisInConstructor"})
    CommonName(UnitId ag, String provider) {
        super(ag.getId());
        this.provider = provider;
        this.ag = ag;
        final RequestProcessor rp = RPMAP.computeIfAbsent(provider, CommonNamesProvider::new).rp;
        init = rp.post(this);
    }

    @Override
    public synchronized String getResolvedName(Object... params) {
        try {
            init.waitFinished(WAIT_TIME);
        } catch (InterruptedException ex) {
            Logger.getLogger(CommonName.class.getName()).log(Level.INFO, "Reloading " + ag.toString() + " from " + provider, ex);
            init.schedule(RELOAD_INTERVAL);
        }
        if (resolved instanceof IOException) {
            return super.getResolvedName(params);
        } else if (resolved instanceof String) {
            return (String) resolved;
        }
        return ag.getId();
    }

    @Override
    public void run() {
        final Optional<Units> u = Units.get(provider);
        if (u.isPresent()) {
            try {
                resolved = u.get().fetchUnitInfo(ag, null, null).getResponseUnitEntry().getCommonUnitName();
            } catch (IOException | IllegalArgumentException ex) {
                resolved = ex;
            }
        }
        resolved = ag.getId();
    }

    static class CommonNamesProvider { // implements JMSListener

        private final RequestProcessor rp;
        private final String provider;

        CommonNamesProvider(final String prov) {
            this.provider = prov;
            this.rp = new RequestProcessor(CommonName.class.getName() + ":" + provider, AGNAME_RP_THROUGHPUT);
        }
    }
}
