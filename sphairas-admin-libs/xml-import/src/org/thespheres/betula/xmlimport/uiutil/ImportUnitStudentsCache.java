/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.uiutil;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.services.util.Units;

/**
 *
 * @author boris.heithecker
 */
public class ImportUnitStudentsCache {

    public static final int RP_THROUGHPUT = 64;
    private final static Map<String, ImportUnitStudentsCache> INSTANCES = new HashMap<>();
    private final Cache<UnitId, Holder> cache = CacheBuilder.newBuilder()
            .concurrencyLevel(NbPreferences.forModule(ImportUnitStudentsCache.class).getInt("ImportUnitStudentsCache.RP_THROUGHPUT", RP_THROUGHPUT))
            .initialCapacity(100)
            .build();
    private final String provider;

    private ImportUnitStudentsCache(final String provider) {
        this.provider = provider;
    }

    public static StudentId[] get(final String provider, final UnitId unit) throws IOException {
        final ImportUnitStudentsCache c;
        synchronized (INSTANCES) {
            c = INSTANCES.computeIfAbsent(provider, ImportUnitStudentsCache::new);
        }
        try {
            return c.get(unit);
        } catch (final ExecutionException ex) {
            final Throwable cause = ex.getCause();
            if (IOException.class.isAssignableFrom(cause.getClass())) {
                throw (IOException) cause;
            } else {
                throw new IOException(ex);
            }
        }
    }

    public static void resetAll() {
        synchronized (INSTANCES) {
            INSTANCES.forEach((p, c) -> c.cache.invalidateAll());
        }
    }

    private StudentId[] get(final UnitId unit) throws ExecutionException {
        return cache.get(unit, () -> fetch(unit)).getContent();
    }

    private Holder fetch(final UnitId uid) throws ExecutionException {
        final Units units = Units.get(provider)
                .orElseThrow(() -> new ProviderUnitsNotFoundException(provider));
        final Holder ret;
        if (units.hasUnit(uid)) {
            try {
                final StudentId[] arr = units.fetchParticipants(uid, null).getStudents();
                ret = new Holder(arr);
            } catch (IOException ex) {
                throw new ExecutionException(ex);
            }
        } else {
            ret = new Holder(null);
        }
        return ret;
    }

    private static class Holder {

        private final StudentId[] content;

        Holder(StudentId[] content) {
            this.content = content;
        }

        StudentId[] getContent() {
            return content;
        }

    }

    @Messages({"ProviderUnitsNotFoundException.message=Gruppen (units) des Mandanten {0} nicht gefunden."})
    static class ProviderUnitsNotFoundException extends ExecutionException {

        public ProviderUnitsNotFoundException(final String provider) {
            super(NbBundle.getMessage(ProviderUnitsNotFoundException.class, "ProviderUnitsNotFoundException.message", provider));
        }

    }
}
