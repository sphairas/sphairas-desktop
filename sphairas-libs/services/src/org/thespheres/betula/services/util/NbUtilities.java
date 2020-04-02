/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.util;

import java.io.IOException;
import java.util.function.Consumer;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author boris.heithecker
 */
public class NbUtilities {

    private NbUtilities() {
    }

    public static <T> T waitForLookup(final Lookup lookup, final Class<T> type, final long maxTime) throws IOException {
        final Lookup.Result<T> editorRes = lookup.lookupResult(type);
        final Object[] arr = new Object[1];

        class LkpResult implements LookupListener {

            @Override
            public void resultChanged(LookupEvent ev) {
                if (!editorRes.allInstances().isEmpty()) {
                    synchronized (arr) {
                        arr[0] = editorRes.allInstances().iterator().next();
                        arr.notifyAll();
                    }
                }
            }
        }
        LkpResult listener = new LkpResult();
        listener.resultChanged(null);
        if (arr[0] == null) {
            synchronized (arr) {
                editorRes.addLookupListener(listener);

                try {
                    if (maxTime < 0) {
                        arr.wait();
                    } else {
                        arr.wait(maxTime);
                    }
                } catch (InterruptedException ex) {
                    throw new IOException(ex);
                }
            }
        }
        editorRes.removeLookupListener(listener);
        if (arr[0] == null) {
            throw new IOException("Could not lookup " + type.getCanonicalName() + " in " + maxTime + "ms.");
        }
        return type.cast(arr[0]);
    }

    public static <T> void waitAndThen(final Lookup lookup, Class<T> type, Consumer<T> andThen) {
        final Lookup.Result<T> result = lookup.lookupResult(type);

        class LkpResult implements LookupListener {

            @Override
            public synchronized void resultChanged(LookupEvent ev) {
                if (!result.allInstances().isEmpty()) {
                    result.removeLookupListener(this);
                    result.allInstances().stream()
                            .map(type::cast)
                            .forEach(andThen::accept);
                }
            }
        }
        LkpResult listener = new LkpResult();
        result.addLookupListener(listener);
        listener.resultChanged(null);
    }
}
