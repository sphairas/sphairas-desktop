/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

/**
 *
 * @author boris.heithecker
 */
public class MessageUtil {

    private static final List SUPPRESSED = new ArrayList();
    private static final List<Dispatch> SUSPENDED = new ArrayList<>();

    private MessageUtil() {
    }

    public static <R> R suppressMessageDelivery(Callable<R> run) throws Exception {
        final Object lock = new Object();
        try {
            synchronized (SUPPRESSED) {
                SUPPRESSED.add(lock);
            }
            return run.call();
        } finally {
            synchronized (SUPPRESSED) {
                final boolean empty = SUPPRESSED.remove(lock) && SUPPRESSED.isEmpty();
                if (empty) {
                    final Iterator<Dispatch> it = SUSPENDED.iterator();
                    while (it.hasNext()) {
                        final Dispatch d = it.next();
                        d.dispatch();
                        it.remove();
                    }
                }
            }
        }
    }

    public static boolean addDispatch(final Dispatch dispatch) {
        synchronized (SUPPRESSED) {
            if (!SUPPRESSED.isEmpty()) {
                SUSPENDED.add(dispatch);
                return true;
            }
        }
        return false;
    }

    public static abstract class Dispatch {

        protected abstract void dispatch();
    }
}
