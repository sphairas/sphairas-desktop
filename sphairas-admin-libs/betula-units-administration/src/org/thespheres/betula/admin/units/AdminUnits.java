/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.admin.units.util.Util;

/**
 *
 * @author boris.heithecker
 */
public class AdminUnits {

    private final static Map<String, AdminUnits> INSTANCES = new HashMap<>();
    public static final int RP_THROUGHPUT = 256;
    private final Cache<UnitId, AdminUnit> CACHE2 = CacheBuilder.newBuilder()
            .weakValues()
            .concurrencyLevel(Util.RP_THROUGHPUT)
            .initialCapacity(6000)
            .build();
    private final String provider;
    private final RequestProcessor rp;

    public static AdminUnits get(final String provider) {
        synchronized (INSTANCES) {
            return INSTANCES.computeIfAbsent(provider, AdminUnits::new);
        }
    }

    private AdminUnits(final String p) {
        provider = p;
        rp = new RequestProcessor(AdminUnits.class.getSimpleName() + "#" + p, RP_THROUGHPUT, true);
    }

    public AdminUnit getUnit(final UnitId unit) {
        final AdminUnit get;
        try {
            get = CACHE2.get(unit, () -> {
                try {
                    return create(unit);
                } catch (Exception e) {
                    throw new ExecutionException(e);
                }
            });
        } catch (ExecutionException ex) {
            final IOException ioex = new IOException(ex.getCause());
            throw new IllegalStateException(ioex);
        }
        return get;
    }

    public void removeTargetAssessmentDocument(final UnitId unit) {
        CACHE2.invalidate(unit);
    }

    private AdminUnit create(final UnitId uid) {
        return new AdminUnit(provider, uid);
    }

    public RequestProcessor getRequestProcessor() {
        return rp;
    }

}
