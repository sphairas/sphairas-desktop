/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units;

import com.google.common.eventbus.EventBus;
import java.util.HashMap;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.services.util.Signees;

/**
 *
 * @author boris.heithecker
 */
public class RemoteSignees {

    private static final HashMap<String, RemoteSignees> MAP = new HashMap<>();
    private final Signees signees;
    private final HashMap<Signee, RemoteSignee> map = new HashMap<>();
    final EventBus events = new EventBus();

    private RemoteSignees(Signees sig) {
        this.signees = sig;
    }

    public static RemoteSignees get(final Signees sig) {
        synchronized (MAP) {
            return MAP.computeIfAbsent(sig.getProviderUrl(), rl -> new RemoteSignees(sig));
        }
    }

    public static RemoteSignee find(final Signees signees, final Signee sid) {
        synchronized (MAP) {
            return MAP.computeIfAbsent(signees.getProviderUrl(), rl -> new RemoteSignees(signees)).find(sid);
        }
    }

    public RemoteSignee find(Signee sid) {
        synchronized (map) {
            return map.computeIfAbsent(sid, s -> new RemoteSignee(signees, s, events));
        }
    }

    public EventBus getEventBus() {
        return events;
    }
}
